/*
 * Copyright 2020 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import play.twirl.api.Html
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.businessAccountingMethodKey
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.IncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.{InvalidJson, UnexpectedStatusFailure}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessAccountingMethodForm._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.FormUtil._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.AccountingMethodModel
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.AuthService
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.business_accounting_method
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class BusinessAccountingMethodController @Inject()(mcc: MessagesControllerComponents,
                                                   incomeTaxSubscriptionConnector: IncomeTaxSubscriptionConnector,
                                                   authService: AuthService)
                                                  (implicit val ec: ExecutionContext, val appConfig: AppConfig)
  extends FrontendController(mcc) with I18nSupport {

  def view(businessAccountingMethodForm: Form[AccountingMethodModel])(implicit request: Request[AnyContent]): Html =
    business_accounting_method(
      businessAccountingMethodForm = businessAccountingMethodForm,
      postAction = uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.BusinessAccountingMethodController.submit(),
      backUrl = backUrl()
    )

  def show(): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      incomeTaxSubscriptionConnector.getSelfEmployments[AccountingMethodModel](businessAccountingMethodKey).map {
        case Right(accountingMethod) =>
          Ok(view(businessAccountingMethodForm.fill(accountingMethod)))
        case Left(UnexpectedStatusFailure(_@status)) =>
          throw new InternalServerException(s"[BusinessAccountingMethodController][show] - Unexpected status: $status")
        case Left(InvalidJson) =>
          throw new InternalServerException("[BusinessAccountingMethodController][show] - Invalid Json")
      }
    }
  }

  def submit(): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      businessAccountingMethodForm.bindFromRequest.fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors))),
        businessAccountingMethod =>
          incomeTaxSubscriptionConnector.saveSelfEmployments(businessAccountingMethodKey, businessAccountingMethod) map (
            _ => Redirect(appConfig.subscriptionFrontendRoutingController))
      )
    }
  }

  def backUrl(): String =
    routes.BusinessListCYAController.show().url
}
