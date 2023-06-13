/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent

import play.api.mvc._
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.businessAccountingMethodKey
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.IncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.utils.ReferenceRetrieval
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{AccountingMethodModel, SelfEmploymentsCYAModel}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, MultipleSelfEmploymentsService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.SelfEmployedCYA
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.ClientDetails._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelfEmployedCYAController @Inject()(val checkYourAnswersView: SelfEmployedCYA,
                                          val authService: AuthService,
                                          val incomeTaxSubscriptionConnector: IncomeTaxSubscriptionConnector,
                                          multipleSelfEmploymentsService: MultipleSelfEmploymentsService,
                                          mcc: MessagesControllerComponents)
                                         (implicit val appConfig: AppConfig, val ec: ExecutionContext)
  extends FrontendController(mcc) with ReferenceRetrieval {


  def show(id: String, isEditMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withReference { reference =>
        withSelfEmploymentCYAModel(reference, id) { selfEmploymentCYAModel =>
          Future.successful(Ok(checkYourAnswersView(
            answers = selfEmploymentCYAModel,
            postAction = routes.SelfEmployedCYAController.submit(id),
            backUrl = backUrl(isEditMode),
            clientDetails = request.getClientDetails
          )))
        }
      }
    }
  }

  def submit(id: String): Action[AnyContent] = Action.async { implicit request =>
    withReference { reference =>
      withSelfEmploymentCYAModel(reference, id) { selfEmploymentCYAModel =>
        if (selfEmploymentCYAModel.isComplete) {
          multipleSelfEmploymentsService.confirmBusiness(reference, id) map {
            case Right(_) =>
              Redirect(appConfig.clientTaskListUrl)
            case Left(_) =>
              throw new InternalServerException("[SelfEmployedCYAController][submit] - Could not confirm self employment business")
          }
        } else {
          Future.successful(Redirect(appConfig.clientTaskListUrl))
        }
      }
    }
  }

  private def withSelfEmploymentCYAModel(reference: String, id: String)(f: SelfEmploymentsCYAModel => Future[Result])
                                        (implicit hc: HeaderCarrier): Future[Result] =
    for {
      accountingMethod <- fetchAccountMethod(reference)
      business <- fetchSelfEmployment(reference, id)
      result <- f(SelfEmploymentsCYAModel(id, business, accountingMethod, 0)) //todo: set business count to actual value when agent side is implemented
    } yield result

  private def fetchAccountMethod(reference: String)(implicit hc: HeaderCarrier) = {
    incomeTaxSubscriptionConnector.getSubscriptionDetails[AccountingMethodModel](reference, businessAccountingMethodKey) map {
      case Left(_) =>
        throw new InternalServerException("[SelfEmployedCYAController][withSelfEmploymentCYAModel] - Failure retrieving accounting method")
      case Right(accountingMethod) => accountingMethod
    }
  }

  private def fetchSelfEmployment(reference: String, id: String)(implicit hc: HeaderCarrier) = {
    multipleSelfEmploymentsService.fetchBusiness(reference, id) map {
      case Left(_) =>
        throw new InternalServerException("[SelfEmployedCYAController][withSelfEmploymentCYAModel] - Failure retrieving self employment data")
      case Right(business) => business
    }
  }

  def backUrl(isEditMode: Boolean): Option[String] = {
    if (isEditMode) {
      Some(appConfig.clientTaskListUrl)
    } else {
      None
    }
  }

}
