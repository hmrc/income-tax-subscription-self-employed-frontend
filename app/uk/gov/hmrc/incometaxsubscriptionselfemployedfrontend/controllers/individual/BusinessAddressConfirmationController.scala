/*
 * Copyright 2023 HM Revenue & Customs
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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.IncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.utils.{ReferenceRetrieval, SessionRetrievals}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessAddressConfirmationForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, MultipleSelfEmploymentsService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.individual.BusinessAddressConfirmation
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessAddressConfirmationController @Inject()(mcc: MessagesControllerComponents,
                                                      authService: AuthService,
                                                      multipleSelfEmploymentsService: MultipleSelfEmploymentsService,
                                                      businessAddressConfirmation: BusinessAddressConfirmation,
                                                      appConfig: AppConfig)
                                                     (val incomeTaxSubscriptionConnector: IncomeTaxSubscriptionConnector)
                                                     (implicit val ec: ExecutionContext)
  extends FrontendController(mcc) with ReferenceRetrieval with SessionRetrievals with I18nSupport {

  val confirmationForm: Form[YesNo] = BusinessAddressConfirmationForm.businessAddressConfirmationForm

  def backUrl(id: String): String = controllers.routes.BusinessTradeNameController.show(id).url

  def show(id: String): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withReference { reference =>
        withFirstAddress(reference, id) { address =>
          Future.successful(Ok(view(confirmationForm, id, address.address)))
        }
      }
    }
  }

  def submit(id: String): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      handleForm(id)(
        onYes = Redirect(controllers.routes.SelfEmployedCYAController.show(id)),
        onNo = Redirect(controllers.routes.AddressLookupRoutingController.initialiseAddressLookupJourney(id))
      )
    }
  }

  private def view(form: Form[YesNo], id: String, address: Address)(implicit request: Request[AnyContent]): Html = {
    businessAddressConfirmation(
      confirmationForm = form,
      postAction = routes.BusinessAddressConfirmationController.submit(id),
      backUrl = backUrl(id),
      address = address
    )
  }

  private def handleForm(id: String)(onYes: Result, onNo: Result)
                        (implicit request: Request[AnyContent]): Future[Result] = {
    withReference { reference =>
      withFirstAddress(reference, id) { address =>
        confirmationForm.bindFromRequest().fold(
          hasError => Future.successful(BadRequest(view(hasError, id, address.address))),
          {
            case Yes =>
              saveBusinessAddress(reference, id, address) {
                onYes
              }
            case No =>
              Future.successful(onNo)
          }
        )
      }
    }

  }

  private def saveBusinessAddress(reference: String, id: String, address: BusinessAddressModel)
                                 (onSaveSuccessful: => Result)
                                 (implicit request: Request[AnyContent]): Future[Result] = {
    multipleSelfEmploymentsService.saveBusinessAddress(reference, id, address) map {
      case Right(_) => onSaveSuccessful
      case Left(_) => throw new InternalServerException("[BusinessAddressConfirmationController][saveBusinessAddress] - Unable to save business address")
    }
  }

  private def withFirstAddress(reference: String, id: String)
                              (onSuccessfulRetrieval: BusinessAddressModel => Future[Result])
                              (implicit request: Request[AnyContent]): Future[Result] = {
    multipleSelfEmploymentsService.fetchAllBusinesses(reference) flatMap {
      case Left(_) => throw new InternalServerException("[BusinessAddressConfirmationController][withFirstBusiness] - Unable to retrieve businesses")
      case Right(businesses) => businesses.flatMap(_.businessAddress).headOption match {
        case Some(address) => onSuccessfulRetrieval(address)
        case None => Future.successful(Redirect(controllers.routes.AddressLookupRoutingController.initialiseAddressLookupJourney(id)))
      }
    }
  }

}
