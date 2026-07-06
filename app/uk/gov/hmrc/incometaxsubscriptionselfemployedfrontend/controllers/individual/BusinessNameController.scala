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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.individual

import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.twirl.api.Html
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.utils.ReferenceRetrieval
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessNameForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, MultipleSelfEmploymentsService, SessionDataService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.individual.BusinessName
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.language.LanguageUtils

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessNameController @Inject()(businessNameView: BusinessName,
                                       mcc: MessagesControllerComponents,
                                       multipleSelfEmploymentsService: MultipleSelfEmploymentsService,
                                       authService: AuthService)
                                      (val sessionDataService: SessionDataService,
                                       val languageUtils: LanguageUtils,
                                       val appConfig: AppConfig)
                                      (implicit val ec: ExecutionContext)
  extends FrontendController(mcc) with ReferenceRetrieval with I18nSupport {

  def show(id: String, isEditMode: Boolean, isGlobalEdit: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withIndividualReference { reference =>
        populateFormFromSavedDetails(reference, id) map { form =>
          Ok(view(
            businessNameForm = form,
            id = id,
            isEditMode = isEditMode,
            isGlobalEdit = isGlobalEdit
          ))
        }
      }
    }
  }

  def submit(id: String, isEditMode: Boolean, isGlobalEdit: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withIndividualReference { reference =>
        BusinessNameForm.businessNameForm.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, id, isEditMode, isGlobalEdit))),
          name =>
            multipleSelfEmploymentsService.isDuplicateBusinessName(reference, id, name) flatMap {
              case true =>
                Future.successful(BadRequest(view(
                  BusinessNameForm.businessNameForm
                    .fill(name)
                    .withError(BusinessNameForm.businessName, "individual.error.duplicate-business"),
                  id, isEditMode, isGlobalEdit
                )))
              case false =>
                saveAndContinue(reference, id, name, isEditMode, isGlobalEdit)
            }
        )
      }
    }
  }

  private def populateFormFromSavedDetails(reference: String, id: String)
                                          (implicit hc: HeaderCarrier): Future[Form[String]] =
    multipleSelfEmploymentsService.fetchStreamlineData(reference, id) map {
      case Some(streamlineBusiness) =>
        BusinessNameForm.businessNameForm
          .bind(BusinessNameForm.createBusinessNameData(streamlineBusiness.name))
          .discardingErrors
      case None =>
        BusinessNameForm.businessNameForm
    }

  private def saveAndContinue(reference: String, id: String, name: String, isEditMode: Boolean, isGlobalEdit: Boolean)
                             (implicit hc: HeaderCarrier): Future[Result] =
    multipleSelfEmploymentsService.saveBusinessName(reference, id, name) map {
      case Right(_) =>
        if (isEditMode || isGlobalEdit) {
          Redirect(routes.SelfEmployedCYAController.show(id, isEditMode, isGlobalEdit))
        } else {
          Redirect(routes.BusinessStartDateBeforeLimitController.show(id, isEditMode, isGlobalEdit))
        }
      case Left(_) =>
        throw new InternalServerException("[BusinessNameController][submit] - Could not save business name")
    }

  private def view(businessNameForm: Form[String], id: String, isEditMode: Boolean, isGlobalEdit: Boolean)
                  (implicit request: Request[AnyContent]): Html =
    businessNameView(
      businessNameForm = businessNameForm,
      postAction = routes.BusinessNameController.submit(id, isEditMode, isGlobalEdit),
      isEditMode = isEditMode || isGlobalEdit
    )

}