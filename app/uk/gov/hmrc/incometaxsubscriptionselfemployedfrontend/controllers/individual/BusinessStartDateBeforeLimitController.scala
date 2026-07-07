/*
 * Copyright 2026 HM Revenue & Customs
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
import play.api.mvc.*
import play.twirl.api.Html
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.utils.ReferenceRetrieval
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessStartDateBeforeLimitForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{DuplicateDetails, No, Yes, YesNo}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, MultipleSelfEmploymentsService, SessionDataService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ImplicitDateFormatter
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.individual.BusinessStartDateBeforeLimit
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.language.LanguageUtils

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessStartDateBeforeLimitController @Inject()(businessStartDateBeforeLimit: BusinessStartDateBeforeLimit,
                                                       mcc: MessagesControllerComponents,
                                                       multipleSelfEmploymentsService: MultipleSelfEmploymentsService,
                                                       authService: AuthService
                                                      )(
                                                        val sessionDataService: SessionDataService,
                                                        val languageUtils: LanguageUtils,
                                                        val appConfig: AppConfig
                                                      )(
                                                        implicit val ec: ExecutionContext
                                                      ) extends FrontendController(mcc)
  with ReferenceRetrieval
  with I18nSupport
  with ImplicitDateFormatter {

  def show(id: String, isEditMode: Boolean, isGlobalEdit: Boolean): Action[AnyContent] = Action.async {
    implicit request =>
      authService.authorised() {
        withIndividualReference { reference =>
          populateFormFromSavedDetails(reference, id).map { form =>
            Ok(view(
              businessStartDateBeforeLimitForm = form,
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
        BusinessStartDateBeforeLimitForm.businessStartDateBeforeLimitForm.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(
              BadRequest(view(formWithErrors, id, isEditMode, isGlobalEdit))
            ),
          startDateBeforeLimit =>
            saveAndContinue(
              reference = reference,
              id = id,
              startDateBeforeLimit = startDateBeforeLimit match {
                case Yes => true
                case No => false
              },
              isEditMode = isEditMode,
              isGlobalEdit = isGlobalEdit
            )
        )
      }
    }
  }

  private def populateFormFromSavedDetails(reference: String, id: String)(implicit hc: HeaderCarrier): Future[Form[YesNo]] = {
    multipleSelfEmploymentsService.fetchBusiness(reference, id).map {
      case Right(Some(business)) =>
        BusinessStartDateBeforeLimitForm.businessStartDateBeforeLimitForm.bind(
          BusinessStartDateBeforeLimitForm.createStartDateBeforeLimitData(
            maybeStartDate = business.startDate,
            maybeStartDateBeforeLimit = business.startDateBeforeLimit
          )
        ).discardingErrors
      case Right(None) =>
        BusinessStartDateBeforeLimitForm.businessStartDateBeforeLimitForm
      case Left(_) =>
        throw new InternalServerException(
          s"[BusinessStartDateBeforeLimitController][populateFormFromSavedDetails] Cannot retrieve business (ref = $reference, id = $id).)"
        )
    }
  }

  private def saveAndContinue(reference: String,
                              id: String,
                              startDateBeforeLimit: Boolean,
                              isEditMode: Boolean,
                              isGlobalEdit: Boolean
                             )(implicit hc: HeaderCarrier): Future[Result] = {
    multipleSelfEmploymentsService.saveStartDateBeforeLimit(
      reference = reference,
      businessId = id,
      startDateBeforeLimit = startDateBeforeLimit
    ).map {
      case Right(_) =>
        if (!startDateBeforeLimit) {
          Redirect(routes.BusinessStartDateController.show(id, isEditMode, isGlobalEdit))
        } else if (isEditMode || isGlobalEdit) {
          Redirect(routes.SelfEmployedCYAController.show(id, isEditMode, isGlobalEdit))
        } else {
          Redirect(routes.AddressLookupRoutingController.checkAddressLookupJourney(id, isEditMode))
        }
      case Left(_) => throw new InternalServerException("[BusinessStartDateBeforeLimitController][submit] - Could not save start date before limit")
    }
  }

  private def view(businessStartDateBeforeLimitForm: Form[_],
                   id: String,
                   isEditMode: Boolean,
                   isGlobalEdit: Boolean
                  )(implicit request: Request[AnyContent]): Html = {

    businessStartDateBeforeLimit(
      businessStartDateBeforeLimitForm = businessStartDateBeforeLimitForm,
      postAction = routes.BusinessStartDateBeforeLimitController.submit(id, isEditMode, isGlobalEdit),
      isEditMode = isEditMode || isGlobalEdit
    )
  }
}