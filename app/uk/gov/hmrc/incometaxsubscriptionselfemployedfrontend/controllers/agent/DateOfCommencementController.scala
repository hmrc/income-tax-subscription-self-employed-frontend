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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent

import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import play.twirl.api.Html
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.IncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.DateOfCommencementForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.DateOfCommencementForm.dateOfCommencementForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.BusinessStartDate
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.AuthService
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ImplicitDateFormatter
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.language.LanguageUtils
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.date_of_commencement
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.FormUtil._



import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DateOfCommencementController @Inject()(mcc: MessagesControllerComponents,
                                              incomeTaxSubscriptionConnector: IncomeTaxSubscriptionConnector,                                              authService: AuthService,
                                              val languageUtils: LanguageUtils)
                                             (implicit val ec: ExecutionContext, val appConfig: AppConfig) extends FrontendController(mcc)
  with I18nSupport with ImplicitDateFormatter {

  def view(dateOfCommencementForm: Form[BusinessStartDate], isEditMode: Boolean)(implicit request: Request[AnyContent]): Html = {
    date_of_commencement(
      dateOfCommencementForm = dateOfCommencementForm,
      postAction = uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.routes.DateOfCommencementController.submit(isEditMode),
      isEditMode,
      backUrl = backUrl(isEditMode)
    )
  }

  def show(isEditMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      incomeTaxSubscriptionConnector.getSelfEmployments[BusinessStartDate](DateOfCommencementController.businessStartDateKey).map {
        case Right(businessStartDateData) => Ok(view(form.fill(businessStartDateData),isEditMode))
        case error => throw new InternalServerException(error.toString)
      }
    }
  }


  def submit(isEditMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      form.bindFromRequest.fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors,isEditMode))),
        businessStartDateData =>
          incomeTaxSubscriptionConnector.saveSelfEmployments(DateOfCommencementController.businessStartDateKey, businessStartDateData) map (_ =>
            if (isEditMode) {
              Redirect(uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.routes.DateOfCommencementController.show())
            } else {
              Redirect(uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.routes.DateOfCommencementController.show())
            }
            )
      )
    }
  }

  def backUrl(isEditMode: Boolean): String = {
    if (isEditMode) {
      uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.routes.DateOfCommencementController.show().url
    } else {
      appConfig.incomeTaxSubscriptionFrontendBaseUrl + "/client/income"
    }
  }

  def form(implicit request: Request[_]): Form[BusinessStartDate] = {
    dateOfCommencementForm(DateOfCommencementForm.minStartDate.toLongDate)
  }


object DateOfCommencementController {
  val businessStartDateKey: String = "BusinessStartDate"
}

}