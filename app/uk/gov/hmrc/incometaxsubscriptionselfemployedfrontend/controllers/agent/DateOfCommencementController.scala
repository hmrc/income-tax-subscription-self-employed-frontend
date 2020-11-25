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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.DateOfCommencementForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.DateOfCommencementForm.dateOfCommencementForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.BusinessStartDate
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, MultipleSelfEmploymentsService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ImplicitDateFormatter
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.language.LanguageUtils
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.date_of_commencement
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.FormUtil._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DateOfCommencementController @Inject()(mcc: MessagesControllerComponents,
                                             multipleSelfEmploymentsService: MultipleSelfEmploymentsService,
                                             authService: AuthService,
                                             val languageUtils: LanguageUtils)
                                            (implicit val ec: ExecutionContext, val appConfig: AppConfig) extends FrontendController(mcc)
  with I18nSupport with ImplicitDateFormatter {

  def view(dateOfCommencementForm: Form[BusinessStartDate],  id: String, isEditMode: Boolean)(implicit request: Request[AnyContent]): Html = {
    date_of_commencement(
      dateOfCommencementForm = dateOfCommencementForm,
      postAction = uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.routes.DateOfCommencementController.submit(id,isEditMode),
      isEditMode,
      backUrl = backUrl(id, isEditMode)
    )
  }

  def show(id: String, isEditMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      multipleSelfEmploymentsService.fetchAllBusinesses.flatMap {
        case Right(businesses) =>
          multipleSelfEmploymentsService.fetchBusinessStartDate(id).map {
            case Right(businessStartDateData) => Ok(view(form.fill(businessStartDateData), id, isEditMode))
            case Left(error) => throw new InternalServerException(error.toString)
          }
        case Left(error) => throw new InternalServerException(error.toString)
      }
    }
  }


  def submit(id: String, isEditMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      form.bindFromRequest.fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors,id,isEditMode))),
        businessStartDateData =>
          multipleSelfEmploymentsService.saveBusinessStartDate(id, businessStartDateData).map(_ =>
            if (isEditMode) {
              Redirect(uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.routes.BusinessListCYAController.show())
            } else {
              Redirect(uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.routes.BusinessNameController.show(id))
            }
          )
      )
    }
  }

  def backUrl(id: String,isEditMode: Boolean): String = {
    if (isEditMode) {
      uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.routes.BusinessListCYAController.show().url
    } else {
      appConfig.incomeTaxSubscriptionFrontendBaseUrl + "/client/business/what-year-to-sign-up"
    }
  }

  def form(implicit request: Request[_]): Form[BusinessStartDate] = {
    dateOfCommencementForm(DateOfCommencementForm.minStartDate.toLongDate)
  }

}
