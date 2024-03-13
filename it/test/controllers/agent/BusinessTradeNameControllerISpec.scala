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

package controllers.agent

import connectors.stubs.IncomeTaxSubscriptionConnectorStub._
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.AuthStub._
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.{incomeSourcesComplete, soleTraderBusinessesKey}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._

class BusinessTradeNameControllerISpec extends ComponentSpecBase {

  val testInvalidBusinessTradeName: String = "!()+{}?^~"

  val soleTraderBusinessesWithoutTrade: SoleTraderBusinesses = soleTraderBusinesses.copy(
    businesses = soleTraderBusinesses.businesses.map(_.copy(trade = None))
  )

  "GET /report-quarterly/income-and-expenses/sign-up/self-employments/client/details/business-trade" when {
    "the Connector receives no content" should {
      "return the page with no prepopulated fields" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinessesWithoutTrade))

        When("GET /client/details/business-trade is called")
        val res = getClientTradeName(id)

        Then("should return an OK with the BusinessTradeNamePage")
        res must have(
          httpStatus(OK),
          pageTitle("What is the trade of your client’s business?" + agentTitleSuffix),
          textField("businessTradeName", "")
        )
      }
    }

    "Connector returns a previously filled in Business Trade Name" should {
      "show the current trade name page with value entered" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinesses))

        When("GET /client/details/business-trade is called")
        val res = getClientTradeName(id)

        Then("should return an OK with the BusinessTradeNamePage")
        res must have(
          httpStatus(OK),
          textField("businessTradeName", "test trade")
        )
      }
    }
  }

  "POST /report-quarterly/income-and-expenses/sign-up/self-employments/client/details/business-trade" when {
    "not in edit mode" when {
      "the form data is valid and connector stores it successfully" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()

        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinessesWithoutTrade))
        stubSaveSubscriptionData(reference, soleTraderBusinessesKey, Json.toJson(soleTraderBusinesses))(OK)
        stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)

        When("POST /client/details/business-trade is called")
        val res = submitClientTradeName(id, Some("test trade"))

        Then("Should return a SEE_OTHER with a redirect location of Business Trade name")
        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(ClientBusinessAddressCheckUri)
        )
      }

      "the form data is valid but is a duplicate submission" in {
        val data: SoleTraderBusinesses = SoleTraderBusinesses(
          businesses = Seq(
            SoleTraderBusiness("idOne", name = Some("nameOne"), trade = Some("tradeOne")),
            SoleTraderBusiness("idTwo", name = Some("nameOne"), trade = None)
          )
        )

        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(data))

        When("POST /client/details/business-trade is called")
        val res = submitClientTradeName("idTwo", Some("tradeOne"))

        Then("Should return a SEE_OTHER")
        res must have(
          httpStatus(BAD_REQUEST),
          pageTitle("Error: What is the trade of your client’s business?" + agentTitleSuffix)
        )
      }

      "the form data is invalid and connector stores it unsuccessfully" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinessesWithoutTrade))

        When("POST /client/details/business-trade is called")
        val res = submitClientTradeName(id, Some(testInvalidBusinessTradeName))

        Then("Should return a BAD_REQUEST and THE FORM With errors")
        res must have(
          httpStatus(BAD_REQUEST),
        )
      }
    }
    "in edit mode" when {
      "the form data is valid and connector stores it successfully" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()

        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinessesWithoutTrade))
        stubSaveSubscriptionData(reference, soleTraderBusinessesKey, Json.toJson(soleTraderBusinesses))(OK)
        stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)

        When("POST /client/details/business-trade is called")
        val res = submitClientTradeName(id, Some("test trade"), inEditMode = true)

        Then("Should return a SEE_OTHER with a redirect location of Business Trade name")
        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(ClientBusinessCYAUri)
        )
      }

      "the form data is invalid and connector stores it unsuccessfully" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinessesWithoutTrade))

        When("POST /client/details/business-trade is called")
        val res = submitClientTradeName(id, Some(testInvalidBusinessTradeName), inEditMode = true)

        Then("Should return a BAD_REQUEST and THE FORM With errors")
        res must have(
          httpStatus(BAD_REQUEST),
        )
      }
    }
  }
}
