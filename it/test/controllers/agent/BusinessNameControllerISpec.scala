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
import connectors.stubs.SessionDataConnectorStub.stubGetSessionData
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants.{id, soleTraderBusinesses}
import helpers.servicemocks.AuthStub._
import play.api.http.Status._
import play.api.libs.json.{JsString, Json}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.{incomeSourcesComplete, soleTraderBusinessesKey}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.routes
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.SoleTraderBusinesses
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ITSASessionKeys


class BusinessNameControllerISpec extends ComponentSpecBase {

  val soleTraderBusinessesWithoutName: SoleTraderBusinesses = soleTraderBusinesses.copy(
    businesses = soleTraderBusinesses.businesses.map(_.copy(name = None))
  )

  val testNino: String = "test-nino"

  "GET /report-quarterly/income-and-expenses/sign-up/self-employments/client/details/business-name" when {

    "the Connector receives no content" should {
      "return the page with no prepopulated fields" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinessesWithoutName))
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

        When("GET /client/details/business-name is called")
        val res = getClientBusinessName(id)

        Then("should return an OK with the ClientBusinessName Page")
        res must have(
          httpStatus(OK)
        )
      }
    }

    "Connector returns a previously filled in ClientBusinessName" should {
      "show the current business name page with date values entered" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinesses))
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

        When("GET /client/business/start-date is called")
        val res = getClientBusinessName(id)

        Then("should return an OK with the ClientBusinessName Page")
        res must have(
          httpStatus(OK),
          textField("businessName", "test name")
        )
      }
    }
  }

  "POST /report-quarterly/income-and-expenses/sign-up/self-employments/client/details/business-name" when {
    "not in edit mode" when {
      "the form data is valid and connector stores it successfully" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinessesWithoutName))
        stubSaveSubscriptionData(reference, soleTraderBusinessesKey, Json.toJson(soleTraderBusinesses))(OK)
        stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)

        When("POST /client/details/business-name is called")
        val res = submitClientBusinessName(id, inEditMode = false, Some("test name"))

        Then("Should return a SEE_OTHER with a redirect location of business name")
        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.BusinessStartDateController.show(id).url)
        )
      }

      "the form data is invalid" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinessesWithoutName))
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

        When("POST /client/details/business-name is called")
        val res = submitClientBusinessName(id, inEditMode = false, None)

        Then("Should return a BAD_REQUEST and THE FORM With errors")
        res must have(
          httpStatus(BAD_REQUEST)
        )
      }
    }
    "in edit mode" when {
      "the form data is valid and connector stores it successfully" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinessesWithoutName))
        stubSaveSubscriptionData(reference, soleTraderBusinessesKey, Json.toJson(soleTraderBusinesses))(OK)
        stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)

        When("POST /client/details/business-name is called")
        val res = submitClientBusinessName(id, inEditMode = true, Some("test name"))

        Then("Should return a SEE_OTHER with a redirect location of business name")
        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.SelfEmployedCYAController.show(id, isEditMode = true).url)
        )
      }

      "the form data is invalid" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinessesWithoutName))
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

        When("POST /client/details/business-name is called")
        val res = submitClientBusinessName(id, inEditMode = true, None)

        Then("Should return a BAD_REQUEST and THE FORM With errors")
        res must have(
          httpStatus(BAD_REQUEST)
        )
      }
    }
  }
}
