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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent

import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import play.twirl.api.Html
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitch.SaveAndRetrieve
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.IncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.{InvalidJson, UnexpectedStatusFailure}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.BusinessAccountingMethodController.businessAccountingMethodKey
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.utils.ReferenceRetrieval
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.BusinessAccountingMethodForm._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.FormUtil._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.AccountingMethodModel
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.AuthService
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.BusinessAccountingMethod
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessAccountingMethodController @Inject()(businessAccountingMethod: BusinessAccountingMethod,
                                                   mcc: MessagesControllerComponents,
                                                   val incomeTaxSubscriptionConnector: IncomeTaxSubscriptionConnector,
                                                   authService: AuthService)
                                                  (implicit val ec: ExecutionContext, val appConfig: AppConfig)
  extends FrontendController(mcc) with I18nSupport with FeatureSwitching with ReferenceRetrieval {

  def view(businessAccountingMethodForm: Form[AccountingMethodModel], id: Option[String], isEditMode: Boolean)
          (implicit request: Request[AnyContent]): Html =
    businessAccountingMethod(
      businessAccountingMethodForm = businessAccountingMethodForm,
      postAction = uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.routes.BusinessAccountingMethodController.submit(id, isEditMode),
      backUrl = backUrl(id, isEditMode),
      isEditMode = isEditMode
    )

  def show(id: Option[String], isEditMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withReference { reference =>
        incomeTaxSubscriptionConnector.getSubscriptionDetails[AccountingMethodModel](reference, businessAccountingMethodKey).map {
          case Right(accountingMethod) =>
            Ok(view(businessAccountingMethodForm.fill(accountingMethod), id, isEditMode))
          case Left(UnexpectedStatusFailure(_@status)) =>
            throw new InternalServerException(s"[BusinessAccountingMethodController][show] - Unexpected status: $status")
          case Left(InvalidJson) =>
            throw new InternalServerException("[BusinessAccountingMethodController][show] - Invalid Json")
        }
      }
    }
  }

  def submit(id: Option[String], isEditMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withReference { reference =>
        businessAccountingMethodForm.bindFromRequest.fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, id, isEditMode))),
          businessAccountingMethod =>
            incomeTaxSubscriptionConnector.saveSubscriptionDetails(reference, businessAccountingMethodKey, businessAccountingMethod) map { _ =>
              (id, isEditMode, isEnabled(SaveAndRetrieve)) match {
                case (Some(id), _, true) =>
                  Redirect(routes.SelfEmployedCYAController.show(id, isEditMode = isEditMode))
                case _ =>
                  Redirect(s"${appConfig.subscriptionFrontendClientRoutingController}?editMode=$isEditMode")
              }
            }
        )
      }
    }
  }

  def backUrl(id: Option[String], isEditMode: Boolean): Option[String] = {
    (id, isEditMode, isEnabled(SaveAndRetrieve)) match {
      case (Some(id), true, true) => Some(routes.SelfEmployedCYAController.show(id).url)
      case (_, false, true) => None
      case (_, true, false) => Some(appConfig.subscriptionFrontendFinalCYAController)
      case _ => Some(routes.BusinessListCYAController.show.url)
    }
  }

}

object BusinessAccountingMethodController {
  val businessAccountingMethodKey: String = "BusinessAccountingMethod"
}



