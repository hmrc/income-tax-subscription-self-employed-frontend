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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.generic

import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.businessAccountingMethodKey
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitch.SaveAndRetrieve
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.AddressLookupConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup.GetAddressLookupDetailsHttpParser
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup.GetAddressLookupDetailsHttpParser.InvalidJson
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.utils.ReferenceRetrieval
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{AccountingMethodModel, BusinessAddressModel}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future

trait AddressLookupRoutingControllerGeneric extends FrontendController with FeatureSwitching with ReferenceRetrieval {

  def initialiseAddressLookupJourney(businessId: String, id: Option[String] = None, isEditMode: Boolean): Action[AnyContent]

  def addressLookupContinueUrl(businessId: String, id: Option[String] = None, isEditMode: Boolean): String

  def addressLookupRedirect(businessId: String, id: Option[String] = None, isEditMode: Boolean): Action[AnyContent]

  def isSaveAndRetrieve: Boolean = isEnabled(SaveAndRetrieve)

  val getAddressLookupConnector: AddressLookupConnector

  def fetchAddress(id: Option[String])(implicit hc: HeaderCarrier): Future[BusinessAddressModel] = id match {
    case None =>
      throw new InternalServerException(s"[AddressLookupRoutingController][fetchAddress] - Id not returned from address service")
    case Some(addressId) => getAddressLookupConnector.getAddressDetails(addressId) map {
      case Right(Some(addressDetails)) => addressDetails
      case Right(None) =>
        throw new InternalServerException(s"[AddressLookupRoutingController][fetchAddress] - No address details found with id: $addressId")
      case Left(InvalidJson) =>
        throw new InternalServerException(s"[AddressLookupRoutingController][fetchAddress] - Invalid json response")
      case Left(GetAddressLookupDetailsHttpParser.UnexpectedStatusFailure(status)) =>
        throw new InternalServerException(s"[AddressLookupRoutingController][fetchAddress] - Unexpected response, status: $status")
    }
  }

  def fetchAccountMethod(reference: String)(implicit hc: HeaderCarrier): Future[Option[AccountingMethodModel]] = {
    incomeTaxSubscriptionConnector.getSubscriptionDetails[AccountingMethodModel](reference, businessAccountingMethodKey) map {
      case Left(_) =>
        throw new InternalServerException("[AddressLookupRoutingController][fetchAccountMethod] - Failure retrieving accounting method")
      case Right(accountingMethod) => accountingMethod
    }
  }


}
