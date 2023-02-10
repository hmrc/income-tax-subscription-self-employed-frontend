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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors

import play.api.libs.json.JsValue
import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitch.EnableUseRealAddressLookup
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.{AddressLookupConfig, AppConfig}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup.GetAddressLookupDetailsHttpParser.GetAddressLookupDetailsResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup.PostAddressLookupHttpParser.PostAddressLookupResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddressLookupConnector @Inject()(val appConfig: AppConfig,
                                       addressLookupConfig: AddressLookupConfig,
                                       http: HttpClient)(implicit ec: ExecutionContext) extends FeatureSwitching {

  def addressLookupInitializeUrl: String = {
    s"${appConfig.addressLookupUrl}/api/v2/init"
  }

  def stubbedAddressLookupInitializeUrl: String = {
    s"${appConfig.stubAddressLookupUrl}/api/v2/init"
  }

  def getAddressDetailsUrl(id: String): String = {
    s"${appConfig.addressLookupUrl}/api/v2/confirmed?id=$id"
  }

  def getStubbedAddressDetailsUrl(id: String): String = {
    s"${appConfig.stubAddressLookupUrl}/api/v2/confirmed?id=$id"
  }

  def initialiseAddressLookup(continueUrl: String, isAgent: Boolean)(implicit hc: HeaderCarrier, request: RequestHeader): Future[PostAddressLookupResponse] = {
    if (isEnabled(EnableUseRealAddressLookup)) {
      http.POST[JsValue, PostAddressLookupResponse](
        url = addressLookupInitializeUrl,
        body = if (isAgent) addressLookupConfig.agentConfig(continueUrl) else addressLookupConfig.config(continueUrl)
      )
    } else {
      http.POST[JsValue, PostAddressLookupResponse](
        url = stubbedAddressLookupInitializeUrl,
        body = if (isAgent) addressLookupConfig.agentConfig(continueUrl) else addressLookupConfig.config(continueUrl)
      )
    }
  }

  def getAddressDetails(id: String)(implicit hc: HeaderCarrier): Future[GetAddressLookupDetailsResponse] = {
    if (isEnabled(EnableUseRealAddressLookup)) {
      http.GET[GetAddressLookupDetailsResponse](getAddressDetailsUrl(id))
    } else {
      http.GET[GetAddressLookupDetailsResponse](getStubbedAddressDetailsUrl(id))
    }
  }

}
