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

import play.api.mvc._
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitch.EnableAgentStreamline
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.AddressLookupConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup.GetAddressLookupDetailsHttpParser.InvalidJson
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup.PostAddressLookupHttpParser.PostAddressLookupSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.utils.ReferenceRetrieval
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{AccountingMethod, Address}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, MultipleSelfEmploymentsService, SessionDataService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddressLookupRoutingController @Inject()(mcc: MessagesControllerComponents,
                                               authService: AuthService,
                                               addressLookupConnector: AddressLookupConnector,
                                               multipleSelfEmploymentsService: MultipleSelfEmploymentsService)
                                              (val sessionDataService: SessionDataService,
                                               val appConfig: AppConfig)
                                              (implicit val ec: ExecutionContext)
  extends FrontendController(mcc) with ReferenceRetrieval with FeatureSwitching {

  private def addressLookupContinueUrl(businessId: String, isEditMode: Boolean, isGlobalEdit: Boolean): String =
    appConfig.incomeTaxSubscriptionSelfEmployedFrontendBaseUrl +
      routes.AddressLookupRoutingController.addressLookupRedirect(businessId, isEditMode = isEditMode, isGlobalEdit = isGlobalEdit)

  def checkAddressLookupJourney(businessId: String, isEditMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    withAgentReference { reference =>
      multipleSelfEmploymentsService.fetchFirstAddress(reference) map {
        case Right(Some(_)) =>
          Redirect(routes.BusinessAddressConfirmationController.show(businessId))
        case Right(_) =>
          Redirect(routes.AddressLookupRoutingController.initialiseAddressLookupJourney(businessId, isEditMode))
        case Left(_) =>
          throw new InternalServerException("[AddressLookupRoutingController][checkAddressLookupJourney] - Error when retrieving any address")
      }
    }
  }

  def initialiseAddressLookupJourney(businessId: String, isEditMode: Boolean, isGlobalEdit: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      addressLookupConnector.initialiseAddressLookup(
        continueUrl = addressLookupContinueUrl(businessId, isEditMode, isGlobalEdit),
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

  def addressLookupRedirect(businessId: String, addressId: Option[String], isEditMode: Boolean, isGlobalEdit: Boolean): Action[AnyContent] =
    Action.async { implicit request =>
      authService.authorised() {
        withAgentReference { reference =>
          for {
            addressDetails <- fetchAddress(addressId)
            accountingMethod <- fetchAccountMethod(reference)
            saveResult <- multipleSelfEmploymentsService.saveAddress(reference, businessId, addressDetails)
          } yield {
            saveResult match {
              case Right(_) =>
                if (isEditMode || isGlobalEdit) {
                  Redirect(routes.SelfEmployedCYAController.show(businessId, isEditMode = true, isGlobalEdit))
                } else {
                  if (isEnabled(EnableAgentStreamline) || accountingMethod.isDefined) {
                    Redirect(routes.SelfEmployedCYAController.show(businessId))
                  } else {
                    Redirect(routes.BusinessAccountingMethodController.show(businessId))
                  }
                }
              case Left(_) =>
                throw new InternalServerException("[AddressLookupRoutingController][addressLookupRedirect] - Could not save business address")
            }
          }
        }
      }
    }

  private def fetchAccountMethod(reference: String)(implicit hc: HeaderCarrier): Future[Option[AccountingMethod]] = {
    multipleSelfEmploymentsService.fetchAccountingMethod(reference) map {
      case Left(_) =>
        throw new InternalServerException("[AddressLookupRoutingController][fetchAccountMethod] - Failure retrieving accounting method")
      case Right(accountingMethod) => accountingMethod
    }
  }

  private def fetchAddress(id: Option[String])(implicit hc: HeaderCarrier): Future[Address] = id match {
    case None =>
      throw new InternalServerException(s"[AddressLookupRoutingController][fetchAddress] - Id not returned from address service")
    case Some(addressId) => addressLookupConnector.getAddressDetails(addressId) map {
      case Right(Some(addressDetails)) => addressDetails
      case Right(None) =>
        throw new InternalServerException(s"[AddressLookupRoutingController][fetchAddress] - No address details found with id: $addressId")
      case Left(InvalidJson) =>
        throw new InternalServerException(s"[AddressLookupRoutingController][fetchAddress] - Invalid json response")
      case Left(GetAddressLookupDetailsHttpParser.UnexpectedStatusFailure(status)) =>
        throw new InternalServerException(s"[AddressLookupRoutingController][fetchAddress] - Unexpected response, status: $status")
    }
  }
}
