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
import play.api.mvc._
import play.twirl.api.Html
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.actions.IdentifierAction
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.StreamlineIncomeSourceForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.requests.agent.IdentifierRequest
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{DuplicateDetails, No, Yes}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{MultipleSelfEmploymentsService, SessionDataService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.FullIncomeSource
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FullIncomeSourceController @Inject()(identify: IdentifierAction,
                                           fullIncomeSource: FullIncomeSource,
                                           mcc: MessagesControllerComponents,
                                           multipleSelfEmploymentsService: MultipleSelfEmploymentsService,
                                           sessionDataService: SessionDataService,
                                           appConfig: AppConfig)
                                          (implicit val ec: ExecutionContext)

  extends FrontendController(mcc) with I18nSupport {

  def show(id: String, isEditMode: Boolean, isGlobalEdit: Boolean): Action[AnyContent] = identify.async { implicit request =>
    populateFormFromSavedDetails(request.reference, id) map { form =>
      Ok(view(
        fullIncomeSourceForm = form,
        id = id,
        isEditMode = isEditMode,
        isGlobalEdit = isGlobalEdit
      ))
    }
  }

  def submit(id: String, isEditMode: Boolean, isGlobalEdit: Boolean): Action[AnyContent] = identify.async { implicit request =>
    StreamlineIncomeSourceForm.fullIncomeSourceForm.bindFromRequest().fold(
      formWithErrors => {
        Future.successful(BadRequest(view(formWithErrors, id, isEditMode, isGlobalEdit)))
      }, {
        case (trade, name, startDateBeforeLimit) =>
          isDuplicateBusiness(reference = request.reference, id = id, name = name, trade = trade) flatMap {
            case true =>
              saveDuplicateDetailsAndContinue(
                id = id,
                duplicateDetails = DuplicateDetails(
                  id = id,
                  name = name,
                  trade = trade,
                  startDateBeforeLimit = startDateBeforeLimit match {
                    case Yes => true
                    case No => false
                  }
                ),
                isEditMode = isEditMode,
                isGlobalEdit = isGlobalEdit
              )
            case false =>
              saveValidDetailsAndContinue(
                reference = request.reference,
                id = id,
                trade = trade,
                name = name,
                startDateBeforeLimit = startDateBeforeLimit match {
                  case Yes => true
                  case No => false
                },
                isEditMode = isEditMode,
                isGlobalEdit = isGlobalEdit
              )
          }
      }
    )
  }


  private def populateFormFromSavedDetails(reference: String, id: String)(implicit hc: HeaderCarrier): Future[Form[_]] = {
    multipleSelfEmploymentsService.fetchStreamlineData(reference, id) flatMap {
      case Some(streamlineBusiness) =>
        Future.successful(StreamlineIncomeSourceForm.fullIncomeSourceForm.bind(
          StreamlineIncomeSourceForm.createIncomeSourceData(
            maybeTradeName = streamlineBusiness.trade,
            maybeBusinessName = streamlineBusiness.name,
            maybeStartDate = streamlineBusiness.startDate,
            maybeStartDateBeforeLimit = streamlineBusiness.startDateBeforeLimit
          )
        ).discardingErrors)
      case None =>
        sessionDataService.getDuplicateDetails(id) map {
          case Some(duplicateDetails) =>
            StreamlineIncomeSourceForm.fullIncomeSourceForm.bind(
              StreamlineIncomeSourceForm.createIncomeSourceData(
                maybeTradeName = Some(duplicateDetails.trade),
                maybeBusinessName = Some(duplicateDetails.name),
                maybeStartDate = None,
                maybeStartDateBeforeLimit = Some(duplicateDetails.startDateBeforeLimit)
              )
            ).discardingErrors
          case None =>
            StreamlineIncomeSourceForm.fullIncomeSourceForm
        }
    }
  }

  private def saveValidDetailsAndContinue(reference: String,
                                          id: String,
                                          trade: String,
                                          name: String,
                                          startDateBeforeLimit: Boolean,
                                          isEditMode: Boolean,
                                          isGlobalEdit: Boolean)
                                         (implicit hc: HeaderCarrier): Future[Result] = {
    multipleSelfEmploymentsService.saveStreamlinedIncomeSource(
      reference = reference,
      businessId = id,
      trade = trade,
      name = name,
      startDateBeforeLimit = startDateBeforeLimit
    ) map {
      case Right(_) =>
        if (!startDateBeforeLimit) {
          Redirect(routes.BusinessStartDateController.show(id, isEditMode, isGlobalEdit))
        } else if (isEditMode || isGlobalEdit) {
          Redirect(routes.SelfEmployedCYAController.show(id, isEditMode, isGlobalEdit))
        } else {
          Redirect(routes.AddressLookupRoutingController.checkAddressLookupJourney(id, isEditMode))
        }
      case Left(_) => throw new InternalServerException("[FullIncomeSourceController][submit] - Could not save sole trader full income source")
    }
  }

  private def saveDuplicateDetailsAndContinue(id: String, duplicateDetails: DuplicateDetails, isEditMode: Boolean, isGlobalEdit: Boolean)
                                             (implicit hc: HeaderCarrier): Future[Result] = {
    sessionDataService.saveDuplicateDetails(duplicateDetails) map { _ =>
      Redirect(routes.DuplicateDetailsController.show(id, isEditMode, isGlobalEdit))
    }
  }

  private def isDuplicateBusiness(reference: String, id: String, name: String, trade: String)(implicit hc: HeaderCarrier): Future[Boolean] = {
    multipleSelfEmploymentsService.fetchSoleTraderBusinesses(reference) map {
      case Right(Some(soleTraderBusinesses)) =>
        soleTraderBusinesses.businesses.filterNot(_.id == id).exists(business => business.name.contains(name) && business.trade.contains(trade))
      case Right(None) => false
      case Left(error) => throw new InternalServerException(s"[FullIncomeSourceController][isDuplicateBusiness] - Unable to fetch all businesses - $error")
    }
  }

  private def view(fullIncomeSourceForm: Form[_], id: String, isEditMode: Boolean, isGlobalEdit: Boolean)
                  (implicit request: IdentifierRequest[AnyContent]): Html = {
    fullIncomeSource(
      fullIncomeSourceForm = fullIncomeSourceForm,
      postAction = routes.FullIncomeSourceController.submit(id, isEditMode, isGlobalEdit),
      backUrl = backUrl(id, isEditMode, isGlobalEdit),
      isEditMode = isEditMode,
      clientDetails = request.clientDetails
    )
  }

  def backUrl(id: String, isEditMode: Boolean, isGlobalEdit: Boolean): String = {
    if (isEditMode || isGlobalEdit) {
      routes.SelfEmployedCYAController.show(id, isEditMode, isGlobalEdit).url
    } else {
      appConfig.clientYourIncomeSourcesUrl
    }
  }

}
