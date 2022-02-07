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

package connectors

import connectors.stubs.AddressLookupConnectorStub._
import helpers.{ComponentSpecBase, IntegrationTestConstants}
import helpers.IntegrationTestConstants._
import org.mockito.Mockito
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.RequestHeader
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.AddressLookupConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup.PostAddressLookupHttpParser.PostAddressLookupSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{Address, BusinessAddressModel}


class AddressLookupConnectorISpec extends ComponentSpecBase {

  lazy val connector: AddressLookupConnector = app.injector.instanceOf[AddressLookupConnector]
  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  val businessAddressModel: BusinessAddressModel = BusinessAddressModel(auditRef = "1",
    Address(lines = Seq("line1", "line2", "line3"), postcode = "TF3 4NT"))

  val successJson: JsObject = Json.obj("auditRef"-> "1",
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

  "Initialise AddressLookup journey" when {
    implicit val mockRequestHeader: RequestHeader = mock[RequestHeader]
    Mockito.when(mockRequestHeader.rawQueryString).thenReturn(IntegrationTestConstants.referrerQueryString)
    Mockito.when(mockRequestHeader.path).thenReturn(IntegrationTestConstants.referrerPath)
    "the user is an agent" should {
      "Return PostSubscriptionDetailsSuccessResponse" in {
        stubInitializeAddressLookup(Json.parse(testAddressLookupConfigClient("testUrl")))("testLocation", ACCEPTED)

        val res = connector.initialiseAddressLookup("testUrl", isAgent = true)

        await(res) mustBe Right(PostAddressLookupSuccessResponse(Some("testLocation")))
      }

      "Return UnexpectedStatusFailure(status)" in {
        stubInitializeAddressLookup(Json.parse(testAddressLookupConfigClient("testUrl")))("testLocation", INTERNAL_SERVER_ERROR)

        val res = connector.initialiseAddressLookup("testUrl", isAgent = true)

        await(res) mustBe Left(PostAddressLookupHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR))
      }
    }
    "the user is individual" should {
      "Return PostSubscriptionDetailsSuccessResponse" in {
        stubInitializeAddressLookup(Json.parse(testAddressLookupConfig("testUrl")))("testLocation", ACCEPTED)

        val res = connector.initialiseAddressLookup("testUrl", isAgent = false)

        await(res) mustBe Right(PostAddressLookupSuccessResponse(Some("testLocation")))
      }

      "Return UnexpectedStatusFailure(status)" in {
        stubInitializeAddressLookup(Json.parse(testAddressLookupConfig("testUrl")))("testLocation", INTERNAL_SERVER_ERROR)

        val res = connector.initialiseAddressLookup("testUrl", isAgent = false)

        await(res) mustBe Left(PostAddressLookupHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR))
      }
    }
  }
}
