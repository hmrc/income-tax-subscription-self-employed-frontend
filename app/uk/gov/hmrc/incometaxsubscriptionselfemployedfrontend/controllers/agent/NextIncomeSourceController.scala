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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.NextIncomeSourceForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{ClientDetails, DateModel}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, ClientDetailsRetrieval, MultipleSelfEmploymentsService, SessionDataService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ImplicitDateFormatter
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.NextIncomeSource
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.language.LanguageUtils

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class NextIncomeSourceController @Inject()(nextIncomeSource: NextIncomeSource,
                                           clientDetailsRetrieval: ClientDetailsRetrieval,
                                           mcc: MessagesControllerComponents,
                                           multipleSelfEmploymentsService: MultipleSelfEmploymentsService,
                                           authService: AuthService)
                                          (val sessionDataService: SessionDataService,
                                           val languageUtils: LanguageUtils,
                                           val appConfig: AppConfig)
                                          (implicit val ec: ExecutionContext)
  extends FrontendController(mcc) with ReferenceRetrieval with I18nSupport with ImplicitDateFormatter {

  def show(id: String, isEditMode: Boolean, isGlobalEdit:Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withAgentReference { reference =>
        clientDetailsRetrieval.getClientDetails flatMap { clientDetails =>
          multipleSelfEmploymentsService.fetchStreamlineBusiness(reference, id) map {
            case Right(streamlineBusiness) =>
              if (streamlineBusiness.isFirstBusiness) {
                Redirect(routes.FirstIncomeSourceController.show(id, isEditMode, isGlobalEdit))
              } else {
                Ok(view(
                  nextIncomeSourceForm = form.bind(NextIncomeSourceForm.createNextIncomeSourceData(
                    maybeTradeName = streamlineBusiness.trade,
                    maybeBusinessName = streamlineBusiness.name,
                    maybeStartDate = streamlineBusiness.startDate
                  )).discardingErrors,
                  id = id,
                  isEditMode = isEditMode,
                  clientDetails = clientDetails,
                  isGlobalEdit
                ))
              }
            case Left(_) =>
              throw new InternalServerException(s"[SecondIncomeSourceController][show] - Unexpected error, fetching streamline business details")
          }
        }
      }
    }
  }

  def submit(id: String, isEditMode: Boolean, isGlobalEdit:Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withAgentReference { reference =>
        form.bindFromRequest().fold(
          formWithErrors => clientDetailsRetrieval.getClientDetails map { clientDetails =>
            BadRequest(view(
              formWithErrors, id, isEditMode, clientDetails, isGlobalEdit
            ))
          }, {
            case (trade, name, startDate) =>
              multipleSelfEmploymentsService.saveNextIncomeSource(
                reference = reference,
                businessId = id,
                trade = trade,
                name = name,
                startDate = startDate
              ) map {
                case Right(_) =>
                  if (isEditMode || isGlobalEdit) {
                    Redirect(routes.SelfEmployedCYAController.show(id, isEditMode, isGlobalEdit).url)
                  } else {
                    Redirect(routes.AddressLookupRoutingController.checkAddressLookupJourney(id, isEditMode).url)
                  }
                case Left(_) =>
                  throw new InternalServerException("[SecondIncomeSourceController][submit] - Could not save first income source")
              }
          }
        )

      }
    }
  }

  def backUrl(id: String, isEditMode: Boolean, isGlobalEdit: Boolean): String = {

    if (isEditMode || isGlobalEdit) routes.SelfEmployedCYAController.show(id, isEditMode, isGlobalEdit = isGlobalEdit).url
    else appConfig.clientYourIncomeSourcesUrl
  }

  private def view(nextIncomeSourceForm: Form[(String, String, DateModel)], id: String,
                   isEditMode: Boolean, clientDetails: ClientDetails, isGlobalEdit: Boolean)
                  (implicit request: Request[AnyContent]): Html =
    nextIncomeSource(
      nextIncomeSourceForm = nextIncomeSourceForm,
      postAction = routes.NextIncomeSourceController.submit(id, isEditMode, isGlobalEdit),
      backUrl = backUrl(id, isEditMode, isGlobalEdit),
      isEditMode = isEditMode,
      clientDetails = clientDetails
    )

  private def form(implicit request: Request[_]): Form[(String, String, DateModel)] = {
    NextIncomeSourceForm.nextIncomeSourceForm(_.toLongDate())
  }

}



