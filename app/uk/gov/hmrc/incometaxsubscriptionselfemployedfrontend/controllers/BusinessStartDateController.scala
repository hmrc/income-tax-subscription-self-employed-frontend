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

import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import play.twirl.api.Html
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.IncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.utils.ReferenceRetrieval
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessStartDateForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessStartDateForm.businessStartDateForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.FormUtil._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.BusinessStartDate
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, MultipleSelfEmploymentsService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ImplicitDateFormatter
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.{BusinessStartDate => BusinessStartDateView}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.language.LanguageUtils

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class BusinessStartDateController @Inject()(mcc: MessagesControllerComponents,
                                            multipleSelfEmploymentsService: MultipleSelfEmploymentsService,
                                            val incomeTaxSubscriptionConnector: IncomeTaxSubscriptionConnector,
                                            authService: AuthService,
                                            val languageUtils: LanguageUtils,
                                            businessStartDate: BusinessStartDateView)
                                           (implicit val ec: ExecutionContext, val appConfig: AppConfig)
  extends FrontendController(mcc)  with ImplicitDateFormatter with ReferenceRetrieval with I18nSupport {

  def view(businessStartDateForm: Form[BusinessStartDate], id: String, isEditMode: Boolean)
          (implicit request: Request[AnyContent]): Html = {
    businessStartDate(
      businessStartDateForm = businessStartDateForm,
      postAction = uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.BusinessStartDateController.submit(id, isEditMode),
      isEditMode,
      backUrl = backUrl(id, isEditMode)
    )
  }

  def show(id: String, isEditMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withReference { reference =>
        multipleSelfEmploymentsService.fetchBusinessStartDate(reference, id).map {
          case Right(businessStartDateData) => Ok(view(form.fill(businessStartDateData), id, isEditMode))
          case Left(error) => throw new InternalServerException(error.toString)
        }
      }
    }
  }

  def submit(id: String, isEditMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withReference { reference =>
        form.bindFromRequest.fold(
          formWithErrors => {
            Future.successful(BadRequest(view(formWithErrors, id, isEditMode)))
          },
          businessStartDateData => multipleSelfEmploymentsService.saveBusinessStartDate(reference, id, businessStartDateData)
            .map(_ => next(id, isEditMode))
        )
      }
    }
  }

  //save & retrieve on should have an order of: business name -> business start date (this) -> business trade
  //save & retrieve off should have an order of: business start date (this) -> business name -> business trade
  private def next(id: String, isEditMode: Boolean) = Redirect(
    if (isEditMode) {
      uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.SelfEmployedCYAController.show(id, isEditMode = isEditMode)
    } else {
      uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.BusinessTradeNameController.show(id)
    }
  )

  //save & retrieve on should have an order of: business name -> business start date (this) -> business trade
  //save & retrieve off should have an order of: business start date (this) -> business name -> business trade
  def backUrl(id: String, isEditMode: Boolean): String = if (isEditMode) {
    uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.SelfEmployedCYAController.show(id, isEditMode = isEditMode).url
  } else {
    uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.BusinessNameController.show(id, isEditMode = isEditMode).url
  }

  def form(implicit request: Request[_]): Form[BusinessStartDate] = {
    businessStartDateForm(BusinessStartDateForm.minStartDate, BusinessStartDateForm.maxStartDate, d => d.toLongDate)
  }

}
