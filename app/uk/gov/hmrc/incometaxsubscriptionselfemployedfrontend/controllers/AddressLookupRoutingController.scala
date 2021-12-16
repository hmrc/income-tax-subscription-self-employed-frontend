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
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.businessAccountingMethodKey
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitch.SaveAndRetrieve
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup.GetAddressLookupDetailsHttpParser.InvalidJson
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup.PostAddressLookupHttpParser.PostAddressLookupSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.{AddressLookupConnector, IncomeTaxSubscriptionConnector}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.utils.ReferenceRetrieval
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{AccountingMethodModel, BusinessAddressModel}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, MultipleSelfEmploymentsService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddressLookupRoutingController @Inject()(mcc: MessagesControllerComponents,
                                               authService: AuthService,
                                               addressLookupConnector: AddressLookupConnector,
                                               val incomeTaxSubscriptionConnector: IncomeTaxSubscriptionConnector,
                                               multipleSelfEmploymentsService: MultipleSelfEmploymentsService
                                              )(implicit val ec: ExecutionContext, val appConfig: AppConfig)
  extends FrontendController(mcc) with FeatureSwitching with ReferenceRetrieval {

  private def isSaveAndRetrieve: Boolean = isEnabled(SaveAndRetrieve)

  private def addressLookupContinueUrl(businessId: String, isEditMode: Boolean): String = {
    appConfig.incomeTaxSubscriptionSelfEmployedFrontendBaseUrl + routes.AddressLookupRoutingController.addressLookupRedirect(businessId, None, isEditMode)
  }

  def initialiseAddressLookupJourney(businessId: String, isEditMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      addressLookupConnector.initialiseAddressLookup(
        continueUrl = addressLookupContinueUrl(businessId, isEditMode),
        isAgent = false
      ) map {
        case Right(PostAddressLookupSuccessResponse(Some(location))) =>
          Redirect(location)
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

  private def fetchAccountMethod(reference: String)(implicit hc: HeaderCarrier): Future[Option[AccountingMethodModel]] = {
    incomeTaxSubscriptionConnector.getSubscriptionDetails[AccountingMethodModel](reference, businessAccountingMethodKey) map {
      case Left(_) =>
        throw new InternalServerException("[AddressLookupRoutingController][fetchAccountMethod] - Failure retrieving accounting method")
      case Right(accountingMethod) => accountingMethod
    }
  }

  private def fetchAddress(id: Option[String])(implicit hc: HeaderCarrier): Future[BusinessAddressModel] = {
    id match {
      case None =>
        throw new InternalServerException(s"[AddressLookupRoutingController][fetchAddress] - Id not returned from address service")
      case Some(addressId) =>
        addressLookupConnector.getAddressDetails(addressId) map {
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
}
