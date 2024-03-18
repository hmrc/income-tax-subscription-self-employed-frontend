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
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.utils.{ReferenceRetrieval, SessionRetrievals}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessNameConfirmationForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{No, Yes, YesNo}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, MultipleSelfEmploymentsService, SessionDataService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.individual.BusinessNameConfirmation
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessNameConfirmationController @Inject()(mcc: MessagesControllerComponents,
                                                   authService: AuthService,
                                                   multipleSelfEmploymentsService: MultipleSelfEmploymentsService,
                                                   businessNameConfirmation: BusinessNameConfirmation)
                                                  (val sessionDataService: SessionDataService,
                                                   val appConfig: AppConfig)
                                                  (implicit val ec: ExecutionContext)
  extends FrontendController(mcc) with ReferenceRetrieval with SessionRetrievals with I18nSupport {

  val confirmationForm: Form[YesNo] = BusinessNameConfirmationForm.businessNameConfirmationForm

  val backUrl: String = appConfig.yourIncomeSourcesUrl

  def show(id: String): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withIndividualReference { reference =>
        withBusinessOrUsersName(id, reference) { (name, isBusinessName) =>
          Future.successful(Ok(view(confirmationForm, id, name, isBusinessName)))
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


  private def view(form: Form[YesNo], id: String, name: String, isBusinessName: Boolean)
                  (implicit request: Request[AnyContent]): Html = {
    businessNameConfirmation(
      confirmationForm = form,
      postAction = routes.BusinessNameConfirmationController.submit(id),
      backUrl = backUrl,
      name = name,
      isBusinessName = isBusinessName
    )
  }

  private def handleForm(id: String)(onYes: Result, onNo: Result)
                        (implicit request: Request[AnyContent]): Future[Result] = {
    withIndividualReference { reference =>
      withBusinessOrUsersName(id, reference) { (name, isBusinessName) =>
        confirmationForm.bindFromRequest().fold(
          hasError => Future.successful(BadRequest(view(hasError, id, name, isBusinessName))),
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

  private def withBusinessOrUsersName(id: String, reference: String)
                                     (f: (String, Boolean) => Future[Result])
                                     (implicit request: Request[AnyContent]): Future[Result] = {
    multipleSelfEmploymentsService.fetchFirstBusinessName(reference).flatMap { result =>
      result.getOrElse(
        throw new InternalServerException("[BusinessNameConfirmationController][withBusinessOrUsersName] - Unable to retrieve businesses")
      ) match {
        case Some(businessName) => f(businessName, true)
        case None => fetchUsersName match {
          case Some(name) => f(name, false)
          case None => Future.successful(Redirect(routes.BusinessNameController.show(id)))
        }
      }
    }
  }

}

