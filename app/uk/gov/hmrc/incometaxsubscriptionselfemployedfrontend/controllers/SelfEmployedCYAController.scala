/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException, NotFoundException}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitch.SaveAndRetrieve
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.IncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{AccountingMethodModel, SelfEmploymentsCYAModel}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, MultipleSelfEmploymentsService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.SelfEmployedCYA
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelfEmployedCYAController @Inject()(val checkYourAnswersView: SelfEmployedCYA,
                                          val authService: AuthService,
                                          incomeTaxSubscriptionConnector: IncomeTaxSubscriptionConnector,
                                          multipleSelfEmploymentsService: MultipleSelfEmploymentsService,
                                          mcc: MessagesControllerComponents)
                                         (implicit val appConfig: AppConfig, val ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport with FeatureSwitching {


  def show(id: String, isEditMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    if (isEnabled(SaveAndRetrieve)) {
      withSelfEmploymentCYAModel(id) { selfEmploymentCYAModel =>
        Future.successful(Ok(checkYourAnswersView(
          answers = selfEmploymentCYAModel,
          postAction = uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.SelfEmployedCYAController.submit(id),
          backUrl = backUrl(isEditMode)
        )))
      }
    } else {
      Future.failed(new NotFoundException("[SelfEmployedCYAController][show] - The save and retrieve feature switch is disabled"))
    }
  }

  def submit(id: String): Action[AnyContent] = Action.async { implicit request =>
    if (isEnabled(SaveAndRetrieve)) {
      withSelfEmploymentCYAModel(id) { selfEmploymentCYAModel =>
        if (selfEmploymentCYAModel.isComplete) {
          multipleSelfEmploymentsService.confirmBusiness(id)  map {
            case Left(_)  =>
              throw new InternalServerException("[SelfEmployedCYAController][submit] - Failure to save self employment data")
            case Right(_) => Redirect(appConfig.taskListUrl)
          }
        } else {
          Future.successful(Redirect(routes.SelfEmployedCYAController.show(id)))
        }
      }
    } else {
      Future.failed(new NotFoundException("[SelfEmployedCYAController][submit] - The save and retrieve feature switch is disabled"))
    }
  }

  private def withSelfEmploymentCYAModel(id: String)(f: SelfEmploymentsCYAModel => Future[Result])(implicit hc: HeaderCarrier): Future[Result] =
    for {
      accountingMethod <- fetchAccountMethod()
      business <- fetchSelfEmployment(id)
      result <- f(SelfEmploymentsCYAModel(id, business, accountingMethod))
    } yield result

  private def fetchAccountMethod()(implicit hc: HeaderCarrier) = {
    incomeTaxSubscriptionConnector.getSelfEmployments[AccountingMethodModel](SelfEmploymentDataKeys.businessAccountingMethodKey) map {
      case Left(_) =>
        throw new InternalServerException("[SelfEmployedCYAController][withSelfEmploymentCYAModel] - Failure retrieving accounting method")
      case Right(accountingMethod) => accountingMethod
    }
  }

  private def fetchSelfEmployment(id: String)(implicit hc: HeaderCarrier) = {
    multipleSelfEmploymentsService.fetchBusiness(id) map {
      case Left(_)  =>
        throw new InternalServerException("[SelfEmployedCYAController][withSelfEmploymentCYAModel] - Failure retrieving self employment data")
      case Right(business) => business
    }
  }

  def backUrl(isEditMode: Boolean): Option[String] = {
    if (isEditMode) {
      Some(appConfig.taskListUrl)
      } else {
      None
      }
    }

}
