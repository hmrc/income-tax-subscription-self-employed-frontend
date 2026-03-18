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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors

import play.api.mvc.RequestHeader
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitch.EnableUseRealAddressLookup
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.{AddressLookupConfig, AppConfig}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup.GetAddressLookupDetailsHttpParser.GetAddressLookupDetailsResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup.PostAddressLookupHttpParser.PostAddressLookupResponse

import java.net.URL
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue


@Singleton
class AddressLookupConnector @Inject()(val appConfig: AppConfig,
                                       addressLookupConfig: AddressLookupConfig,
                                       http: HttpClientV2)(implicit ec: ExecutionContext) extends FeatureSwitching {

  private def addressLookupInitializeUrl: URL =
    url"${appConfig.addressLookupUrl}/api/v2/init"

  private def stubbedAddressLookupInitializeUrl: URL = {
    url"${appConfig.stubAddressLookupUrl}/api/v2/init"
  }

  private def getAddressDetailsUrl(id: String): URL = {
    url"${appConfig.addressLookupUrl}/api/v2/confirmed?id=$id"
  }

  private def getStubbedAddressDetailsUrl(id: String): URL = {
    url"${appConfig.stubAddressLookupUrl}/api/v2/confirmed?id=$id"
  }

  def initialiseAddressLookup(continueUrl: String, isAgent: Boolean)(implicit hc: HeaderCarrier, request: RequestHeader): Future[PostAddressLookupResponse] = {
    val url = if (isEnabled(EnableUseRealAddressLookup)) {
      addressLookupInitializeUrl
    } else {
      stubbedAddressLookupInitializeUrl
    }
    http.post(url).withBody(addressLookupConfig.config(continueUrl, isAgent))
      .execute[PostAddressLookupResponse]
  }

  def getAddressDetails(id: String)(implicit hc: HeaderCarrier): Future[GetAddressLookupDetailsResponse] = {
    val url = if (isEnabled(EnableUseRealAddressLookup)) {
      getAddressDetailsUrl(id)
    } else {
      getStubbedAddressDetailsUrl(id)
    }
    http.get(url).execute[GetAddressLookupDetailsResponse]
  }
}
