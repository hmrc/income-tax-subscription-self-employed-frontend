/*
 * Copyright 2020 HM Revenue & Customs
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

import javax.inject.Inject
import play.api.i18n.Lang
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.{AddressLookupConfig, AppConfig}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup.GetAddressLookupDetailsHttpParser.InvalidJson
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup.PostAddressLookupHttpParser.PostAddressLookupSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.{AddressLookupConnector, IncomeTaxSubscriptionConnector}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.AuthService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.ExecutionContext

class AddressLookupRoutingController @Inject()(mcc: MessagesControllerComponents,
                                               authService: AuthService,
                                               addressLookupConnector: AddressLookupConnector,
                                               addressLookupConfig: AddressLookupConfig,
                                               incomeTaxSubscriptionConnector: IncomeTaxSubscriptionConnector
                                              )(implicit val ec: ExecutionContext, val appConfig: AppConfig) extends FrontendController(mcc) {

  def initialiseAddressLookupJourney(): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      implicit val lang: Lang = mcc.messagesApi.preferred(request).lang
      val continueUrl =
        appConfig.incomeTaxSubscriptionSelfEmployedFrontendBaseUrl + routes.AddressLookupRoutingController.addressLookupRedirect(None).url
      addressLookupConnector.initialiseAddressLookup(addressLookupConfig.agentConfig(continueUrl)) map (
        response =>
          response match {
            case Right(PostAddressLookupSuccessResponse(Some(location))) => Redirect(location)
            case Left(PostAddressLookupHttpParser.UnexpectedStatusFailure(status)) => throw new InternalServerException(
              s"[AddressLookupRoutingController][initialiseAddressLookupJourney] - Unexpected response, status: $status")
          }
        )
    }
  }

  def addressLookupRedirect(id: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      if (!id.isDefined) {
        throw new InternalServerException(
          s"[AddressLookupRoutingController][addressLookupRedirect] - Id not returned from address service")
      } else {
        addressLookupConnector.getAddressDetails(id.get) flatMap {
          addressDetailsResponse =>
            addressDetailsResponse match {
              case Right(Some(addressDetails)) =>
                incomeTaxSubscriptionConnector.saveSelfEmployments(AddressLookupRoutingController.businessAddress, addressDetails).map(_ =>
                  Redirect(routes.BusinessTradeNameController.show()))
              case Right(None) => throw new InternalServerException(
                s"[AddressLookupRoutingController][addressLookupRedirect] - No address details found with id: $id")
              case Left(InvalidJson) => throw new InternalServerException(
                s"[AddressLookupRoutingController][addressLookupRedirect] - Invalid json response")
              case Left(GetAddressLookupDetailsHttpParser.UnexpectedStatusFailure(status)) => throw new InternalServerException(
                s"[AddressLookupRoutingController][addressLookupRedirect] - Unexpected response, status: $status")
            }
        }
      }
    }
  }
}

object AddressLookupRoutingController {
  val businessAddress: String = "BusinessAddress"
}
