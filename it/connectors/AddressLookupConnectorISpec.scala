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

package connectors

import connectors.stubs.AddressLookupConnectorStub._
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import play.api.i18n.Lang
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.AddressLookupConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup.PostAddressLookupHttpParser.PostAddressLookupSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{Address, BusinessAddressModel}


class AddressLookupConnectorISpec extends ComponentSpecBase {

  lazy val connector: AddressLookupConnector = app.injector.instanceOf[AddressLookupConnector]
  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()
  private implicit val lang: Lang = Lang("en")

  val businessAddressModel = BusinessAddressModel(auditRef = "1",
    Address(lines = Seq("line1", "line2", "line3"), postcode = "TF3 4NT"))

  val successJson = Json.obj("auditRef"-> "1",
    "address" -> Json.obj("lines" -> Seq("line1", "line2", "line3"), "postcode" -> "TF3 4NT"))

  "GetAddressLookupDetails" should {
    "Return TestModel" in {

      stubGetAddressLookupDetails("1")(OK, successJson)

      val res = connector.getAddressDetails("1")

      await(res) mustBe Right(Some(businessAddressModel))
    }

    "Return InvalidJson" in {
      stubGetAddressLookupDetails("2")(OK, Json.obj())

      val res = connector.getAddressDetails("2")

      await(res) mustBe Left(GetAddressLookupDetailsHttpParser.InvalidJson)
    }

    "Return None" in {
      stubGetAddressLookupDetails("3")(NOT_FOUND, Json.obj())

      val res = connector.getAddressDetails("3")

      await(res) mustBe Right(None)

    }
    "Return UnexpectedStatusFailure" in {
      stubGetAddressLookupDetails("4")(INTERNAL_SERVER_ERROR, Json.obj())

      val res = connector.getAddressDetails("4")

      await(res) mustBe Left(GetAddressLookupDetailsHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR))

    }
  }

  "Initialise AddressLookup journey" should {
    "Return PostSelfEmploymentsSuccessResponse" in {
      stubInitializeAddressLookup(Json.parse(testAddressLookupConfig("testUrl")))("testLocation", ACCEPTED)

      val res = connector.initialiseAddressLookup(testAddressLookupConfig("testUrl"))

      await(res) mustBe Right(PostAddressLookupSuccessResponse(Some("testLocation")))
    }

    "Return UnexpectedStatusFailure(status)" in {
      stubInitializeAddressLookup(Json.parse(testAddressLookupConfig("test")))("testLocation", INTERNAL_SERVER_ERROR)

      val res = connector.initialiseAddressLookup(testAddressLookupConfig("test"))

      await(res) mustBe Left(PostAddressLookupHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR))
    }
  }
}
