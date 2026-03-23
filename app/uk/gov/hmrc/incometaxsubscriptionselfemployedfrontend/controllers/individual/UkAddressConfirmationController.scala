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
import play.api.mvc.*
import play.twirl.api.Html
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.utils.ReferenceRetrieval
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.UkAddressConfirmationForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.*
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, MultipleSelfEmploymentsService, SessionDataService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.individual.UkAddressConfirmation
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UkAddressConfirmationController @Inject()(mcc: MessagesControllerComponents,
                                                authService: AuthService,
                                                multipleSelfEmploymentsService: MultipleSelfEmploymentsService,
                                                ukAddressConfirmation: UkAddressConfirmation)
                                               (val sessionDataService: SessionDataService,
                                                val appConfig: AppConfig)
                                               (implicit val ec: ExecutionContext)
  extends FrontendController(mcc) with ReferenceRetrieval with I18nSupport {

  val confirmationForm: Form[YesNo] = UkAddressConfirmationForm.ukAddressConfirmationForm

  def show(id: String, isEditMode: Boolean, isGlobalEdit: Boolean): Action[AnyContent] = Action.async { implicit request =>
    withIndividualReference { reference =>
      authService.authorised() {
        multipleSelfEmploymentsService.fetchBusiness(reference, id) map {
          case Right(business) =>
            val form = business match {
              case Some(value) => value.hasUkAddress match {
                case Some(true) => confirmationForm.fill(Yes)
                case Some(false) => confirmationForm.fill(No)
                case None => confirmationForm
              }
              case None => confirmationForm
            }
            Ok(view(form, id, business.map(_.name.getOrElse("")).getOrElse(""), isEditMode, isGlobalEdit))
          case _ => throw new InternalServerException("Cannot get business name")
        }
      }
    }
  }

  def submit(id: String, isEditMode: Boolean, isGlobalEdit: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      handleForm(id, isEditMode, isGlobalEdit)
    }
  }

  private def view(form: Form[YesNo], id: String, name: String, isEditMode: Boolean, isGlobalEdit: Boolean)(implicit request: Request[AnyContent]): Html = {
    ukAddressConfirmation(
      confirmationForm = form,
      name = name,
      postAction = routes.UkAddressConfirmationController.submit(id, isEditMode, isGlobalEdit)
    )
  }

  private def handleForm(id: String, isEditMode: Boolean, isGlobalEdit: Boolean)(implicit request: Request[AnyContent]): Future[Result] = {
    confirmationForm.bindFromRequest().fold(
      hasError =>
        withIndividualReference { reference =>
          multipleSelfEmploymentsService.fetchBusiness(reference, id) map {
            case Right(business) => BadRequest(view(hasError, id, business.map(_.name.getOrElse("")).getOrElse(""), isEditMode, isGlobalEdit))
            case _ => throw new InternalServerException("Cannot get business name")
          }
        },
      answer => Future.successful(
        Redirect(routes.AddressLookupRoutingController.initialiseAddressLookupJourney(id, answer == Yes, isEditMode, isGlobalEdit))
      )
    )
  }
}
