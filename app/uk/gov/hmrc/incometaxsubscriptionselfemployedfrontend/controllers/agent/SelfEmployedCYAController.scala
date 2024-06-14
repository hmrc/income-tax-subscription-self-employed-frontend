/*
 * Copyright 2024 HM Revenue & Customs
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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.utils.ReferenceRetrieval
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.ClientDetails._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.SoleTraderBusiness
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, MultipleSelfEmploymentsService, SessionDataService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.SelfEmployedCYA
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelfEmployedCYAController @Inject()(checkYourAnswersView: SelfEmployedCYA,
                                          authService: AuthService,
                                          multipleSelfEmploymentsService: MultipleSelfEmploymentsService,
                                          mcc: MessagesControllerComponents)
                                         (val sessionDataService: SessionDataService,
                                          val appConfig: AppConfig)
                                         (implicit val ec: ExecutionContext)
  extends FrontendController(mcc) with ReferenceRetrieval with FeatureSwitching {

  def show(id: String, isEditMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withAgentReference { reference =>
        withSoleTraderBusiness(reference, id) { soleTraderBusiness =>
          Future.successful(Ok(checkYourAnswersView(
            answers = soleTraderBusiness,
            postAction = routes.SelfEmployedCYAController.submit(id),
            backUrl = backUrl(id, isEditMode),
            clientDetails = request.getClientDetails
          )))
        }
      }
    }
  }

  def submit(id: String): Action[AnyContent] = Action.async { implicit request =>
    withAgentReference { reference =>
      withSoleTraderBusiness(reference, id) { soleTraderBusiness =>
        if (soleTraderBusiness.isComplete) {
          multipleSelfEmploymentsService.confirmBusiness(reference, id) map {
            case Right(_) =>
              Redirect(continueUrl)
            case Left(_) =>
              throw new InternalServerException("[SelfEmployedCYAController][submit] - Could not confirm self employment business")
          }
        } else {
          Future.successful(Redirect(continueUrl))
        }
      }
    }
  }

  private def continueUrl: String = {
    appConfig.clientYourIncomeSourcesUrl
  }

  private def withSoleTraderBusiness(reference: String, id: String)(f: SoleTraderBusiness => Future[Result])
                                    (implicit hc: HeaderCarrier): Future[Result] = {
    multipleSelfEmploymentsService.fetchSoleTraderBusiness(reference, id) flatMap {
      case Right(optBusiness) =>
        val soleTraderBusiness = optBusiness match {
          case Some(business) => business // return the real business
          case None => SoleTraderBusiness(id) // return an empty business
        }
        f(soleTraderBusiness)
      case Left(_) => throw new FetchSoleTraderBusinessException
    }
  }

  def backUrl(id: String, isEditMode: Boolean): String = {
    if (isEditMode) {
      continueUrl
    } else {
      routes.BusinessAccountingMethodController.show(id, isEditMode).url
    }
  }

  private class FetchSoleTraderBusinessException extends InternalServerException(
    "[SelfEmployedCYAController][withSoleTraderBusiness] - Failed to retrieve sole trader business"
  )

}
