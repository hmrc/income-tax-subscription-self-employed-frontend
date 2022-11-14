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

package connectors.stubs

import helpers.servicemocks.WireMockMethods
import play.api.libs.json.{JsValue, Json}

object AddressLookupConnectorStub extends WireMockMethods {

  private def addressLookupInitializeUrl = s"/api/v2/init"

  private def getAddressDetailsUrl(id: String) = s"/api/v2/confirmed\\?id=$id"
  private def getAddressDetailsUrlNoId = s"/api/v2/confirmed"

  def stubGetAddressLookupDetails(id: String)(responseStatus: Int, responseBody: JsValue = Json.obj()): Unit = {
    when(
      method = GET,
      uri = getAddressDetailsUrl(id)
    ).thenReturn(responseStatus, responseBody)
  }

  def getAddressDetailsUrlNoId(responseStatus: Int, responseBody: JsValue = Json.obj()): Unit = {
    when(
      method = GET,
      uri = getAddressDetailsUrlNoId
    ).thenReturn(responseStatus, responseBody)
  }



  def stubInitializeAddressLookup(body: JsValue = Json.obj())(locationHeader: String, responseStatus: Int, responseBody: JsValue = Json.obj()): Unit = {
    when(
      method = POST,
      uri = addressLookupInitializeUrl,
      body = body
    ).thenReturn(responseStatus, Map("Location" -> locationHeader), responseBody)
  }
}
