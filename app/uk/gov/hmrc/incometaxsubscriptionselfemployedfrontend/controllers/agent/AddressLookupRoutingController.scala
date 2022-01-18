/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.mvc._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup.PostAddressLookupHttpParser.PostAddressLookupSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.{AddressLookupConnector, IncomeTaxSubscriptionConnector}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, MultipleSelfEmploymentsService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.generic.AddressLookupRoutingControllerGeneric
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
  extends FrontendController(mcc) with AddressLookupRoutingControllerGeneric {

  override val getAddressLookupConnector: AddressLookupConnector = addressLookupConnector

  def addressLookupContinueUrl(businessId: String, id: Option[String] = None, isEditMode: Boolean): String =
    appConfig.incomeTaxSubscriptionSelfEmployedFrontendBaseUrl +
      routes.AddressLookupRoutingController.addressLookupRedirect(businessId, isEditMode = isEditMode)

  def initialiseAddressLookupJourney(businessId: String, id: Option[String] = None, isEditMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      addressLookupConnector.initialiseAddressLookup(
        continueUrl = addressLookupContinueUrl(businessId, isEditMode = isEditMode),
        isAgent = true
      ) map {
        case Right(PostAddressLookupSuccessResponse(Some(location))) =>
          Redirect(location)
        case Right(PostAddressLookupSuccessResponse(None)) =>
          throw new InternalServerException(s"[AddressLookupRoutingController][initialiseAddressLookupJourney] - Unexpected response, success, but no location returned")
        case Left(PostAddressLookupHttpParser.UnexpectedStatusFailure(status)) =>
          throw new InternalServerException(s"[AddressLookupRoutingController][initialiseAddressLookupJourney] - Unexpected response, status: $status")
      }
    }
  }

  def addressLookupRedirect(businessId: String, addressId: Option[String], isEditMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withReference { reference =>
        for {
          addressDetails <- fetchAddress(addressId)
          accountingMethod <- fetchAccountMethod(reference)
          _ <- multipleSelfEmploymentsService.saveBusinessAddress(reference, businessId, addressDetails)
        } yield {
          (isEditMode, isSaveAndRetrieve) match {
            case (false, true) if accountingMethod.isDefined => Redirect(routes.SelfEmployedCYAController.show(businessId))
            case (false, true) => Redirect(routes.BusinessAccountingMethodController.show(Some(businessId)))
            case (true, true) => Redirect(routes.SelfEmployedCYAController.show(businessId, isEditMode = true))
            case (_, false) => Redirect(routes.BusinessListCYAController.show)
          }
        }
      }
    }
  }

}
