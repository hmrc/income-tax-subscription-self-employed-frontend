/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers

import play.api.mvc._
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.businessAccountingMethodKey
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.IncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.utils.ReferenceRetrieval
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{AccountingMethodModel, SelfEmploymentsCYAModel}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, MultipleSelfEmploymentsService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.SelfEmployedCYA
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

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
            backUrl = backUrl(isEditMode)
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
              Redirect(appConfig.taskListUrl)
            case Left(_) =>
              throw new InternalServerException("[SelfEmployedCYAController][submit] - Could not confirm self employment business")
          }
        } else {
          Future.successful(Redirect(appConfig.taskListUrl))
        }
      }
    }
  }

  private def withSelfEmploymentCYAModel(reference: String, id: String)(f: SelfEmploymentsCYAModel => Future[Result])
                                        (implicit hc: HeaderCarrier): Future[Result] =
    for {
      accountingMethod <- fetchAccountMethod(reference)
      businesses <- fetchSelfEmployments(reference)
      business = businesses.find(_.id == id)
      result <- f(SelfEmploymentsCYAModel(id, business, accountingMethod, businesses.length))
    } yield result

  private def fetchAccountMethod(reference: String)(implicit hc: HeaderCarrier) = {
    incomeTaxSubscriptionConnector.getSubscriptionDetails[AccountingMethodModel](reference, businessAccountingMethodKey)
      .map(_.getOrElse(throw new FetchAccountingMethodException))
  }

  private class FetchAccountingMethodException extends InternalServerException(
    "[SelfEmployedCYAController][fetchAccountingMethod] - Failed to retrieve accounting method"
  )

  private def fetchSelfEmployments(reference: String)(implicit hc: HeaderCarrier) = {
    multipleSelfEmploymentsService.fetchAllBusinesses(reference)
      .map(_.getOrElse(throw new FetchAllBusinessesException))
  }

  private class FetchAllBusinessesException extends InternalServerException(
    "[SelfEmployedCYAController][fetchSelfEmployments] - Failed to retrieve all self employments"
  )

  def backUrl(isEditMode: Boolean): Option[String] = {
    if (isEditMode) {
      Some(appConfig.taskListUrl)
    } else {
      None
    }
  }

}
