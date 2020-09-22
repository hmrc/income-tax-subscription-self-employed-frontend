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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.BusinessStartDateForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.BusinessStartDateForm.businessStartDateForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.FormUtil._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{BusinessStartDate, SelfEmploymentData}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, MultipleSelfEmploymentsService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ImplicitDateFormatter
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.business_start_date
import uk.gov.hmrc.play.bootstrap.controller.FrontendController
import uk.gov.hmrc.play.language.LanguageUtils

import scala.concurrent.ExecutionContext


@Singleton
class BusinessStartDateController @Inject()(mcc: MessagesControllerComponents,
                                            multipleSelfEmploymentsService: MultipleSelfEmploymentsService,
                                            authService: AuthService,
                                            val languageUtils: LanguageUtils)
                                           (implicit val ec: ExecutionContext, val appConfig: AppConfig) extends FrontendController(mcc)
  with I18nSupport with ImplicitDateFormatter {

  def view(businessStartDateForm: Form[BusinessStartDate], id: String, businesses: Seq[SelfEmploymentData], isEditMode: Boolean)
          (implicit request: Request[AnyContent]): Html = {
    business_start_date(
      businessStartDateForm = businessStartDateForm,
      postAction = uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.BusinessStartDateController.submit(id, isEditMode),
      isEditMode,
      backUrl = backUrl(id, businesses, isEditMode)
    )
  }


  def show(id: String, isEditMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      multipleSelfEmploymentsService.fetchAllBusinesses.flatMap {
        case Right(businesses) =>
          multipleSelfEmploymentsService.fetchBusinessStartDate(id).map {
            case Right(businessStartDateData) => Ok(view(form.fill(businessStartDateData), id, businesses, isEditMode))
            case Left(error) => throw new InternalServerException(error.toString)
          }
        case Left(error) => throw new InternalServerException(error.toString)
      }
    }
  }


  def submit(id: String, isEditMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      form.bindFromRequest.fold(
        formWithErrors => {
          multipleSelfEmploymentsService.fetchAllBusinesses.map {
            case Right(businesses) => BadRequest(view(formWithErrors, id, businesses, isEditMode))
            case Left(error) => throw new InternalServerException(error.toString)
          }
        },
        businessStartDateData =>
          multipleSelfEmploymentsService.saveBusinessStartDate(id, businessStartDateData).map(_ =>
            if (isEditMode) {
              Redirect(uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.BusinessListCYAController.show())
            } else {
              Redirect(uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.BusinessNameController.show(id))
            }
          )
      )
    }
  }

  def backUrl(id: String, businesses: Seq[SelfEmploymentData], isEditMode: Boolean): String = {
    val isFirstCompleteBusiness: Boolean = businesses.find(_.isComplete).exists(_.id == id)
    if (isEditMode || !isFirstCompleteBusiness) {
      uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.BusinessListCYAController.show().url
    } else {
      appConfig.incomeTaxSubscriptionFrontendBaseUrl + "/business/what-year-to-sign-up"
    }
  }

  def form(implicit request: Request[_]): Form[BusinessStartDate] = {
    businessStartDateForm(BusinessStartDateForm.minStartDate.toLongDate)
  }

}
