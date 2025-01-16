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

import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.Results.Redirect
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import play.twirl.api.Html
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.utils.ReferenceRetrieval
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.BusinessStartDateForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.BusinessStartDateForm.businessStartDateForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.FormUtil._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{ClientDetails, DateModel}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, ClientDetailsRetrieval, MultipleSelfEmploymentsService, SessionDataService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ImplicitDateFormatter
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.{BusinessStartDate => BusinessStartDateView}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.language.LanguageUtils

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class BusinessStartDateController @Inject()(mcc: MessagesControllerComponents,
                                            clientDetailsRetrieval: ClientDetailsRetrieval,
                                            multipleSelfEmploymentsService: MultipleSelfEmploymentsService,
                                            authService: AuthService,
                                            businessStartDate: BusinessStartDateView)
                                           (val sessionDataService: SessionDataService,
                                            val languageUtils: LanguageUtils,
                                            val appConfig: AppConfig)
                                           (implicit val ec: ExecutionContext)
  extends FrontendController(mcc) with ReferenceRetrieval with I18nSupport with ImplicitDateFormatter {

  def view(businessStartDateForm: Form[DateModel], id: String, isEditMode: Boolean, clientDetails: ClientDetails)
          (implicit request: Request[AnyContent]): Html = {
    businessStartDate(
      businessStartDateForm = businessStartDateForm,
      postAction = routes.BusinessStartDateController.submit(id, isEditMode),
      isEditMode,
      backUrl = backUrl(id, isEditMode),
      clientDetails = clientDetails
    )
  }

  def show(id: String, isEditMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withAgentReference { reference =>
        multipleSelfEmploymentsService.fetchStartDate(reference, id) flatMap {
          case Right(businessStartDateData) =>
            clientDetailsRetrieval.getClientDetails map { clientDetails =>
              Ok(view(form.fill(businessStartDateData), id, isEditMode, clientDetails))
            }
          case Left(error) => throw new InternalServerException(error.toString)
        }
      }
    }
  }

  def submit(id: String, isEditMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withAgentReference { reference =>
        form.bindFromRequest().fold(
          formWithErrors => clientDetailsRetrieval.getClientDetails map { clientDetails =>
            BadRequest(view(formWithErrors, id, isEditMode, clientDetails))
          },
          businessStartDateData =>
            multipleSelfEmploymentsService.saveStartDate(reference, id, businessStartDateData) map {
              case Right(_) =>
                next(id, isEditMode)
              case Left(_) =>
                throw new InternalServerException("[BusinessStartDateController][submit] - Could not save business start date")
            }
        )
      }
    }
  }

  private def next(id: String, isEditMode: Boolean) = Redirect(
    if (isEditMode) {
      routes.SelfEmployedCYAController.show(id, isEditMode = isEditMode)
    } else {
      routes.BusinessStartDateController.show(id)
    }
  )

  def backUrl(id: String, isEditMode: Boolean): String = if (isEditMode) {
    routes.SelfEmployedCYAController.show(id, isEditMode = isEditMode).url
  } else {
    routes.BusinessStartDateController.show(id).url
  }

  def form(implicit request: Request[_]): Form[DateModel] = {
    businessStartDateForm(BusinessStartDateForm.minStartDate, BusinessStartDateForm.maxStartDate, d => d.plusDays(1).toLongDate())
  }
}
