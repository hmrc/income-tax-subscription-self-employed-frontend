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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitch.StartDateBeforeLimit
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.utils.{ReferenceRetrieval, SessionRetrievals}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessAddressConfirmationForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, MultipleSelfEmploymentsService, SessionDataService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.individual.BusinessAddressConfirmation
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessAddressConfirmationController @Inject()(mcc: MessagesControllerComponents,
                                                      authService: AuthService,
                                                      multipleSelfEmploymentsService: MultipleSelfEmploymentsService,
                                                      businessAddressConfirmation: BusinessAddressConfirmation)
                                                     (val sessionDataService: SessionDataService,
                                                      val appConfig: AppConfig)
                                                     (implicit val ec: ExecutionContext)
  extends FrontendController(mcc) with ReferenceRetrieval with SessionRetrievals with I18nSupport with FeatureSwitching {

  val confirmationForm: Form[YesNo] = BusinessAddressConfirmationForm.businessAddressConfirmationForm

  def backUrl(id: String): String = {
    if(isEnabled(StartDateBeforeLimit)) {
      routes.FullIncomeSourceController.show(id).url
    } else {
      routes.BusinessTradeNameController.show(id).url
    }
  }

  def show(id: String): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withIndividualReference { reference =>
        withFirstAddress(reference, id) { address =>
          Future.successful(Ok(view(confirmationForm, id, address)))
        }
      }
    }
  }

  def submit(id: String): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      handleForm(id)(
        onYes = Redirect(routes.SelfEmployedCYAController.show(id)),
        onNo = Redirect(routes.AddressLookupRoutingController.initialiseAddressLookupJourney(id))
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
    withIndividualReference { reference =>
      withFirstAddress(reference, id) { address =>
        confirmationForm.bindFromRequest().fold(
          hasError => Future.successful(BadRequest(view(hasError, id, address))),
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

  private def saveBusinessAddress(reference: String, id: String, address: Address)
                                 (onSaveSuccessful: => Result)
                                 (implicit request: Request[AnyContent]): Future[Result] = {
    multipleSelfEmploymentsService.saveAddress(reference, id, address) map {
      case Right(_) => onSaveSuccessful
      case Left(_) => throw new InternalServerException("[BusinessAddressConfirmationController][saveBusinessAddress] - Unable to save business address")
    }
  }

  private def withFirstAddress(reference: String, id: String)
                              (onSuccessfulRetrieval: Address => Future[Result])
                              (implicit request: Request[AnyContent]): Future[Result] = {
    multipleSelfEmploymentsService.fetchFirstAddress(reference).flatMap { result =>
      result.getOrElse(
        throw new InternalServerException("[BusinessAddressConfirmationController][withFirstBusiness] - Unable to retrieve businesses")
      ) match {
        case Some(address) => onSuccessfulRetrieval(address)
        case None => Future.successful(Redirect(routes.AddressLookupRoutingController.initialiseAddressLookupJourney(id)))
      }
    }
  }

}
