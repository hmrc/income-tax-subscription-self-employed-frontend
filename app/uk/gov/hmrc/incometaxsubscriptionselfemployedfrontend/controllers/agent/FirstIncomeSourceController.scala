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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import play.twirl.api.Html
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.utils.ReferenceRetrieval
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.FirstIncomeSourceForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{AccountingMethod, ClientDetails, DateModel}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, ClientDetailsRetrieval, MultipleSelfEmploymentsService, SessionDataService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ImplicitDateFormatter
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.FirstIncomeSource
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.language.LanguageUtils

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class FirstIncomeSourceController @Inject()(firstIncomeSource: FirstIncomeSource,
                                            clientDetailsRetrieval: ClientDetailsRetrieval,
                                            mcc: MessagesControllerComponents,
                                            multipleSelfEmploymentsService: MultipleSelfEmploymentsService,
                                            authService: AuthService)
                                           (val sessionDataService: SessionDataService,
                                            val languageUtils: LanguageUtils,
                                            val appConfig: AppConfig)
                                           (implicit val ec: ExecutionContext)
  extends FrontendController(mcc) with ReferenceRetrieval with I18nSupport with ImplicitDateFormatter {

  def show(id: String, isEditMode: Boolean, isGlobalEdit: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withAgentReference { reference =>
        clientDetailsRetrieval.getClientDetails flatMap { clientDetails =>
          multipleSelfEmploymentsService.fetchStreamlineBusiness(reference, id) map {
            case Right(streamlineBusiness) =>
              if (streamlineBusiness.isFirstBusiness) {
                Ok(view(
                  firstIncomeSourceForm = form.bind(FirstIncomeSourceForm.createFirstIncomeSourceData(
                    maybeTradeName = streamlineBusiness.trade,
                    maybeBusinessName = streamlineBusiness.name,
                    maybeStartDate = streamlineBusiness.startDate,
                    maybeAccountingMethod = streamlineBusiness.accountingMethod
                  )).discardingErrors,
                  id = id,
                  isEditMode = isEditMode,
                  isGlobalEdit = isGlobalEdit,
                  clientDetails = clientDetails
                ))
              } else {
                Redirect(routes.NextIncomeSourceController.show(id, isEditMode))
              }
            case Left(_) =>
              throw new InternalServerException(s"[FirstIncomeSourceController][show] - Unexpected error fetching streamline business details")
          }
        }
      }
    }
  }

  def submit(id: String, isEditMode: Boolean, isGlobalEdit: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withAgentReference { reference =>
        form.bindFromRequest().fold(
          formWithErrors => clientDetailsRetrieval.getClientDetails map { clientDetails =>
            BadRequest(view(
              formWithErrors, id, isEditMode, isGlobalEdit, clientDetails
            ))
          }, {
            case (trade, name, startDate, accountingMethod) =>
              multipleSelfEmploymentsService.saveFirstIncomeSource(
                reference = reference,
                businessId = id,
                trade = trade,
                name = name,
                startDate = startDate,
                accountingMethod = accountingMethod
              ) map {
                case Right(_) =>
                  if (isEditMode || isGlobalEdit) {
                    Redirect(routes.SelfEmployedCYAController.show(id, isEditMode, isGlobalEdit))
                  } else {
                    Redirect(routes.AddressLookupRoutingController.checkAddressLookupJourney(id).url)
                  }
                case Left(_) =>
                  throw new InternalServerException("[FirstIncomeSourceController][submit] - Could not save first sole trader income source")
              }
          }
        )
      }
    }
  }

  def backUrl(id: String, isEditMode: Boolean, isGlobalEdit: Boolean): String = {
    if (isEditMode || isGlobalEdit) routes.SelfEmployedCYAController.show(id, isEditMode, isGlobalEdit).url
    else appConfig.clientYourIncomeSourcesUrl
  }

  private def view(firstIncomeSourceForm: Form[(String, String, DateModel, AccountingMethod)], id: String,
                   isEditMode: Boolean, isGlobalEdit: Boolean, clientDetails: ClientDetails)
                  (implicit request: Request[AnyContent]): Html =
    firstIncomeSource(
      firstIncomeSourceForm = firstIncomeSourceForm,
      postAction = routes.FirstIncomeSourceController.submit(id, isEditMode, isGlobalEdit),
      backUrl = backUrl(id, isEditMode, isGlobalEdit),
      isEditMode = isEditMode,
      clientDetails = clientDetails
    )

  private def form(implicit request: Request[_]): Form[(String, String, DateModel, AccountingMethod)] = {
    FirstIncomeSourceForm.firstIncomeSourceForm(_.toLongDate())
  }

}



