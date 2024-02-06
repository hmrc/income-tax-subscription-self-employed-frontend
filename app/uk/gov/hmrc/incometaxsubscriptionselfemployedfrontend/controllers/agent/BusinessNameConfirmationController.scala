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
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.IncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.utils.ReferenceRetrieval
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.BusinessNameConfirmationForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.ClientDetails.ClientInfoRequestUtil
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, MultipleSelfEmploymentsService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.BusinessNameConfirmation
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessNameConfirmationController @Inject()(mcc: MessagesControllerComponents,
                                                   authService: AuthService,
                                                   multipleSelfEmploymentsService: MultipleSelfEmploymentsService,
                                                   businessNameConfirmation: BusinessNameConfirmation,
                                                   appConfig: AppConfig)
                                                  (val incomeTaxSubscriptionConnector: IncomeTaxSubscriptionConnector)
                                                  (implicit val ec: ExecutionContext)
  extends FrontendController(mcc) with ReferenceRetrieval with I18nSupport {

  val confirmationForm: Form[YesNo] = BusinessNameConfirmationForm.businessNameConfirmationForm

  val backUrl: String = appConfig.clientYourIncomeSourcesUrl

  def show(id: String): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withReference { reference =>
        withBusinessOrClientsName(reference) { (name, isBusinessName) =>
          Future.successful(Ok(view(
            form = confirmationForm,
            id = id,
            clientDetails = request.getClientDetails,
            displayName = name,
            isBusinessName = isBusinessName
          )))
        }
      }
    }
  }

  def submit(id: String): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      handleForm(id)(
        onYes = Redirect(routes.BusinessStartDateController.show(id)),
        onNo = Redirect(routes.BusinessNameController.show(id))
      )
    }
  }

  private def view(form: Form[YesNo],
                   id: String,
                   clientDetails: ClientDetails,
                   displayName: String,
                   isBusinessName: Boolean)(implicit request: Request[AnyContent]): Html = {
    businessNameConfirmation(
      confirmationForm = form,
      postAction = routes.BusinessNameConfirmationController.submit(id),
      backUrl = backUrl,
      clientDetails = clientDetails,
      displayName = displayName,
      isBusinessName = isBusinessName
    )
  }

  private def handleForm(id: String)(onYes: Result, onNo: Result)
                        (implicit request: Request[AnyContent]): Future[Result] = {
    withReference { reference =>
      withBusinessOrClientsName(reference) { (name, isBusinessName) =>
        val clientDetails: ClientDetails = request.getClientDetails
        confirmationForm.bindFromRequest().fold(
          hasError =>
            Future.successful(BadRequest(view(
              form = hasError,
              id = id,
              clientDetails = clientDetails,
              displayName = name,
              isBusinessName = isBusinessName
            ))),
          {
            case Yes =>
              saveBusinessName(reference, id, name) {
                onYes
              }
            case No =>
              Future.successful(onNo)
          }
        )
      }
    }
  }

  private def saveBusinessName(reference: String, id: String, name: String)
                              (onSaveSuccessful: => Result)
                              (implicit request: Request[AnyContent]): Future[Result] = {
    multipleSelfEmploymentsService.saveName(reference, id, name) map {
      case Right(_) => onSaveSuccessful
      case Left(_) => throw new InternalServerException("[BusinessNameConfirmationController][submit] - Unable to save business name")
    }
  }

  private def withBusinessOrClientsName(reference: String)
                                       (f: (String, Boolean) => Future[Result])
                                       (implicit request: Request[AnyContent]): Future[Result] = {
    multipleSelfEmploymentsService.fetchFirstBusinessName(reference).flatMap { result =>
      result.getOrElse(
        throw new InternalServerException("[BusinessNameConfirmationController][withBusinessOrClientsName] - Unable to retrieve businesses")
      ) match {
        case Some(name) => f(name, true)
        case None => f(request.getClientDetails.name, false)
      }
    }
  }

}
