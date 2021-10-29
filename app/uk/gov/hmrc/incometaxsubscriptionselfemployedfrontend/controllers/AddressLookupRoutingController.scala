/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers

import play.api.mvc._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.AddressLookupConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup.GetAddressLookupDetailsHttpParser.InvalidJson
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup.PostAddressLookupHttpParser.PostAddressLookupSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, MultipleSelfEmploymentsService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class AddressLookupRoutingController @Inject()(mcc: MessagesControllerComponents,
                                               authService: AuthService,
                                               addressLookupConnector: AddressLookupConnector,
                                               multipleSelfEmploymentsService: MultipleSelfEmploymentsService
                                              )(implicit val ec: ExecutionContext, val appConfig: AppConfig) extends FrontendController(mcc) {

  private def addressLookupContinueUrl(itsaId: String): String = {
    appConfig.incomeTaxSubscriptionSelfEmployedFrontendBaseUrl + routes.AddressLookupRoutingController.addressLookupRedirect(itsaId, None)
  }

  def initialiseAddressLookupJourney(itsaId: String): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      addressLookupConnector.initialiseAddressLookup(
        continueUrl = addressLookupContinueUrl(itsaId),
        isAgent = false
      ) flatMap {
        case Right(PostAddressLookupSuccessResponse(Some(location))) =>
          multipleSelfEmploymentsService.saveAddressRedirect(itsaId, location).map(_ => Redirect(location))
        case Left(PostAddressLookupHttpParser.UnexpectedStatusFailure(status)) =>
          throw new InternalServerException(s"[AddressLookupRoutingController][initialiseAddressLookupJourney] - Unexpected response, status: $status")
      }
    }
  }

  def addressLookupRedirect(itsaId: String, id: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      id match {
        case None =>
          throw new InternalServerException(s"[AddressLookupRoutingController][addressLookupRedirect] - Id not returned from address service")
        case Some(addressId) =>
          addressLookupConnector.getAddressDetails(addressId) flatMap {
            case Right(Some(addressDetails)) =>
              multipleSelfEmploymentsService.saveBusinessAddress(itsaId, addressDetails).map(_ => Redirect(routes.BusinessListCYAController.show()))
            case Right(None) =>
              throw new InternalServerException(s"[AddressLookupRoutingController][addressLookupRedirect] - No address details found with id: $addressId")
            case Left(InvalidJson) =>
              throw new InternalServerException(s"[AddressLookupRoutingController][addressLookupRedirect] - Invalid json response")
            case Left(GetAddressLookupDetailsHttpParser.UnexpectedStatusFailure(status)) =>
              throw new InternalServerException(s"[AddressLookupRoutingController][addressLookupRedirect] - Unexpected response, status: $status")
          }
      }
    }
  }
}
