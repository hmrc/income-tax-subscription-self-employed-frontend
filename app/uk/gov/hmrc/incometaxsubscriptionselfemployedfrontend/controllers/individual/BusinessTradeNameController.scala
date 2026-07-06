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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessTradeNameForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, MultipleSelfEmploymentsService, SessionDataService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.individual.BusinessTradeName
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.language.LanguageUtils

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessTradeNameController @Inject()(businessTradeNameView: BusinessTradeName,
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
            businessTradeNameForm = form,
            id = id,
            isEditMode = isEditMode,
            isGlobalEdit = isGlobalEdit
          ))
        }
      }
    }
  }

  private def isDuplicateBusiness(reference: String, id: String, trade: String)
                                 (implicit hc: HeaderCarrier): Future[Boolean] =
    multipleSelfEmploymentsService.fetchStreamlineData(reference, id) flatMap {
      case Some(streamlineBusiness) if streamlineBusiness.name.isDefined =>
        multipleSelfEmploymentsService.fetchSoleTraderBusinesses(reference) map {
          case Right(Some(soleTraderBusinesses)) =>
            soleTraderBusinesses.businesses.filterNot(_.id == id).exists(b =>
              b.name == streamlineBusiness.name && b.trade.contains(trade)
            )
          case Right(None) => false
          case Left(error) => throw new InternalServerException(
            s"[BusinessTradeNameController][isDuplicateBusiness] - Unable to fetch businesses - $error"
          )
        }
      case _ => Future.successful(false)
    }

  def submit(id: String, isEditMode: Boolean, isGlobalEdit: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withIndividualReference { reference =>
        BusinessTradeNameForm.businessTradeNameForm.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, id, isEditMode, isGlobalEdit))),
          trade =>
            isDuplicateBusiness(reference, id, trade) flatMap {
              case true =>
                Future.successful(BadRequest(view(
                  BusinessTradeNameForm.businessTradeNameForm
                    .fill(trade)
                    .withError(BusinessTradeNameForm.businessTradeName, "individual.error.full-income-source.business-trade.duplicate"),
                  id, isEditMode, isGlobalEdit
                )))
              case false =>
                saveAndContinue(reference, id, trade, isEditMode, isGlobalEdit)
            }
        )
      }
    }
  }

  private def populateFormFromSavedDetails(reference: String, id: String)
                                          (implicit hc: HeaderCarrier): Future[Form[String]] =
    multipleSelfEmploymentsService.fetchStreamlineData(reference, id) map {
      case Some(streamlineBusiness) =>
        BusinessTradeNameForm.businessTradeNameForm
          .bind(BusinessTradeNameForm.createBusinessTradeNameData(streamlineBusiness.trade))
          .discardingErrors
      case None =>
        BusinessTradeNameForm.businessTradeNameForm
    }

  private def saveAndContinue(reference: String, id: String, trade: String, isEditMode: Boolean, isGlobalEdit: Boolean)
                             (implicit hc: HeaderCarrier): Future[Result] =
    multipleSelfEmploymentsService.saveTradeName(reference, id, trade) map {
      case Right(_) =>
        if (isEditMode || isGlobalEdit) {
          Redirect(routes.SelfEmployedCYAController.show(id, isEditMode, isGlobalEdit))
        } else {
          Redirect(routes.BusinessNameController.show(id, isEditMode, isGlobalEdit))
        }
      case Left(_) =>
        throw new InternalServerException("[BusinessTradeNameController][submit] - Could not save business trade name")
    }

  private def view(businessTradeNameForm: Form[String], id: String, isEditMode: Boolean, isGlobalEdit: Boolean)
                  (implicit request: Request[AnyContent]): Html =
    businessTradeNameView(
      businessTradeNameForm = businessTradeNameForm,
      postAction = routes.BusinessTradeNameController.submit(id, isEditMode, isGlobalEdit),
      isEditMode = isEditMode || isGlobalEdit
    )

}