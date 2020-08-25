/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers

import connectors.stubs.AddressLookupConnectorStub._
import connectors.stubs.IncomeTaxSubscriptionConnectorStub.stubSaveSelfEmployments
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.AuthStub._
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.{addressIdKey, businessAccountingMethodKey}

class AddressLookupRoutingControllerISpec extends ComponentSpecBase {

  "GET /report-quarterly/income-and-expenses/sign-up/self-employments/address-lookup-initialise" when {

    "the Connector receives NO_CONTENT and location details in headers" should {
      "with location details in headers" in {
        Given("I setup the Wiremock stubs")
        val continueUrl = "http://localhost:9563/report-quarterly/income-and-expenses/sign-up/self-employments/details/address-lookup"
        stubAuthSuccess()
        stubInitializeAddressLookup(
          Json.parse(
            testAddressLookupConfig(continueUrl)))(s"$continueUrl?id=12345", ACCEPTED)

        When("GET /address-lookup-initialise is called")
        val res = getAddressLookupInitialise()

        Then("should return an SEE_OTHER with Address lookup location")
        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(s"$continueUrl?id=12345")
        )
      }
    }

    "the Connector receives OK" should {
      "with location details in headers" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubInitializeAddressLookup(Json.parse(
          testAddressLookupConfig("http://localhost/continueUrl")))("http://localhost/testLocation", OK)

        When("GET /address-lookup-initialise is called")
        val res = getAddressLookupInitialise()

        Then("should return an Internal server page")
        res must have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }
  }

  "GET /report-quarterly/income-and-expenses/sign-up/self-employments/details/address-lookup" when {
    "the address lookup service return successful JSON details" in {
      Given("I setup the Wiremock stubs")
      stubAuthSuccess()
      val json = Json.obj("auditRef"-> "testId1", "address" -> Json.obj("lines" -> Seq("line1", "line2", "line3"), "postcode" -> "TF3 4NT"))
      stubGetAddressLookupDetails("testId1")(OK, json)
      stubSaveSelfEmployments(addressIdKey, json)(OK)

      When("POST /details/address-lookup is called")
      val res = getAddressLookup("testId1")


      Then("Should return a SEE_OTHER with a redirect location of accounting method(this is temporary)")
      res must have(
        httpStatus(SEE_OTHER),
        redirectURI(BusinessListCYAUri)
      )
    }

    "the address lookup service return NOT_FOUND" in {
      Given("I setup the Wiremock stubs")
      stubAuthSuccess()
      stubGetAddressLookupDetails("testId1")(NOT_FOUND)

      When("POST /details/business-accounting-method is called")
      val res = getAddressLookup("testId1")


      Then("Should return a INTERNAL_SERVER_ERROR")
      res must have(
        httpStatus(INTERNAL_SERVER_ERROR)
      )
    }

    "the address lookup service return invalid Json" in {
      Given("I setup the Wiremock stubs")
      stubAuthSuccess()
      stubGetAddressLookupDetails("testId1")(OK, Json.obj("abc" -> "def"))

      When("POST /details/business-accounting-method is called")
      val res = getAddressLookup("testId1")


      Then("Should return a INTERNAL_SERVER_ERROR")
      res must have(
        httpStatus(INTERNAL_SERVER_ERROR)
      )
    }

    "the address lookup service return 400" in {
      Given("I setup the Wiremock stubs")
      stubAuthSuccess()
      stubGetAddressLookupDetails("testId1")(BAD_REQUEST)

      When("POST /details/business-accounting-method is called")
      val res = getAddressLookup("testId1")


      Then("Should return a INTERNAL_SERVER_ERROR")
      res must have(
        httpStatus(INTERNAL_SERVER_ERROR)
      )
    }
  }
}
