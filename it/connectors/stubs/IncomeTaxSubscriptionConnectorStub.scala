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

package connectors.stubs


import helpers.servicemocks.WireMockMethods
import play.api.libs.json.{JsValue, Json}

object IncomeTaxSubscriptionConnectorStub extends WireMockMethods {

  private def selfEmploymentsUri(id: String) = s"/income-tax-subscription/self-employments/id/$id"

  def stubGetSelfEmployments(id: String)(responseStatus: Int, responseBody: JsValue = Json.obj()): Unit = {
    when(
      method = GET,
      uri = selfEmploymentsUri(id)
    ) thenReturn(responseStatus, responseBody)
  }

  def stubSaveSelfEmployments(id: String, body: JsValue = Json.obj())(responseStatus: Int, responseBody: JsValue = Json.obj()): Unit = {
    when (
      method = POST,
      uri = selfEmploymentsUri(id),
      body = body
    ) thenReturn (responseStatus, responseBody)
  }




}
