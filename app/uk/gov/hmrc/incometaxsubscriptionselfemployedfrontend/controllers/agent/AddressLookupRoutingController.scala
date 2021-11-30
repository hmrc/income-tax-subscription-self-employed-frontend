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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup.GetAddressLookupDetailsHttpParser.InvalidJson
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup.PostAddressLookupHttpParser.PostAddressLookupSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.{AddressLookupConnector, IncomeTaxSubscriptionConnector}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.utils.ReferenceRetrieval
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, MultipleSelfEmploymentsService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class AddressLookupRoutingController @Inject()(mcc: MessagesControllerComponents,
                                               authService: AuthService,
                                               addressLookupConnector: AddressLookupConnector,
                                               val incomeTaxSubscriptionConnector: IncomeTaxSubscriptionConnector,
                                               multipleSelfEmploymentsService: MultipleSelfEmploymentsService)
                                              (implicit val ec: ExecutionContext, val appConfig: AppConfig)
  extends FrontendController(mcc) with ReferenceRetrieval {

  private def addressLookupContinueUrl(itsaId: String): String = {
    appConfig.incomeTaxSubscriptionSelfEmployedFrontendBaseUrl + routes.AddressLookupRoutingController.addressLookupRedirect(itsaId, None).url
  }

  def initialiseAddressLookupJourney(itsaId: String): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withReference { reference =>
        addressLookupConnector.initialiseAddressLookup(
          continueUrl = addressLookupContinueUrl(itsaId),
          isAgent = true
        ) flatMap {
          case Right(PostAddressLookupSuccessResponse(Some(location))) =>
            multipleSelfEmploymentsService.saveAddressRedirect(reference, itsaId, location).map(_ => Redirect(location))
          case Left(PostAddressLookupHttpParser.UnexpectedStatusFailure(status)) =>
            throw new InternalServerException(s"[AddressLookupRoutingController][initialiseAddressLookupJourney] - Unexpected response, status: $status")
        }
      }
    }
  }

  def addressLookupRedirect(itsaId: String, id: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withReference { reference =>
        id match {
          case None =>
            throw new InternalServerException(s"[AddressLookupRoutingController][addressLookupRedirect] - Id not returned from address service")
          case Some(addressId) =>
            addressLookupConnector.getAddressDetails(addressId) flatMap {
              case Right(Some(addressDetails)) =>
                multipleSelfEmploymentsService.saveBusinessAddress(reference, itsaId, addressDetails)
                  .map(_ => Redirect(routes.BusinessListCYAController.show()))
              case Right(None) =>
                throw new InternalServerException(s"[AddressLookupRoutingController][addressLookupRedirect] - No address details found with id: $id")
              case Left(InvalidJson) =>
                throw new InternalServerException(s"[AddressLookupRoutingController][addressLookupRedirect] - Invalid json response")
              case Left(GetAddressLookupDetailsHttpParser.UnexpectedStatusFailure(status)) =>
                throw new InternalServerException(s"[AddressLookupRoutingController][addressLookupRedirect] - Unexpected response, status: $status")
            }
        }
      }
    }
  }
}

