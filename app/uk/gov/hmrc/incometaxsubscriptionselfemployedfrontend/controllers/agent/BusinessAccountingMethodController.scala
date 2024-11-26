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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.{InvalidJson, UnexpectedStatusFailure}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.utils.ReferenceRetrieval
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.BusinessAccountingMethodForm._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.FormUtil._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{AccountingMethod, ClientDetails}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, ClientDetailsRetrieval, MultipleSelfEmploymentsService, SessionDataService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.BusinessAccountingMethod
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class BusinessAccountingMethodController @Inject()(businessAccountingMethod: BusinessAccountingMethod,
                                                   clientDetailsRetrieval: ClientDetailsRetrieval,
                                                   mcc: MessagesControllerComponents,
                                                   multipleSelfEmploymentsService: MultipleSelfEmploymentsService,
                                                   authService: AuthService)
                                                  (val sessionDataService: SessionDataService,
                                                   val appConfig: AppConfig)
                                                  (implicit val ec: ExecutionContext)
  extends FrontendController(mcc) with ReferenceRetrieval with I18nSupport {

  def view(businessAccountingMethodForm: Form[AccountingMethod], id: String, isEditMode: Boolean, clientDetails: ClientDetails)
          (implicit request: Request[AnyContent]): Html =
    businessAccountingMethod(
      businessAccountingMethodForm = businessAccountingMethodForm,
      postAction = uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.routes.BusinessAccountingMethodController.submit(id, isEditMode),
      backUrl = backUrl(id, isEditMode),
      isEditMode = isEditMode,
      clientDetails = clientDetails
    )

  def show(id: String, isEditMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withAgentReference { reference =>
        multipleSelfEmploymentsService.fetchAccountingMethod(reference) flatMap {
          case Right(accountingMethod) =>
            clientDetailsRetrieval.getClientDetails map { clientDetails =>
              Ok(view(businessAccountingMethodForm.fill(accountingMethod), id, isEditMode, clientDetails))
            }
          case Left(UnexpectedStatusFailure(_@status)) =>
            throw new InternalServerException(s"[BusinessAccountingMethodController][show] - Unexpected status: $status")
          case Left(InvalidJson) =>
            throw new InternalServerException("[BusinessAccountingMethodController][show] - Invalid Json")
        }
      }
    }
  }

  def submit(id: String, isEditMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withAgentReference { reference =>
        businessAccountingMethodForm.bindFromRequest().fold(
          formWithErrors =>
            clientDetailsRetrieval.getClientDetails map { clientDetails =>
              BadRequest(view(formWithErrors, id, isEditMode, clientDetails))
            },
          businessAccountingMethod =>
            multipleSelfEmploymentsService.saveAccountingMethod(reference, id, businessAccountingMethod) map {
              case Right(_) =>
                Redirect(routes.SelfEmployedCYAController.show(id, isEditMode = isEditMode))
              case Left(_) =>
                throw new InternalServerException("[BusinessAccountingMethodController][submit] - Could not save business accounting method")
            }
        )
      }
    }
  }

  def backUrl(id: String, isEditMode: Boolean): Option[String] = {
    if (isEditMode) Some(routes.SelfEmployedCYAController.show(id, isEditMode).url)
    else None
  }

}



