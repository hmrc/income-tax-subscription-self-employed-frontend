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

package controllers.agent

import connectors.stubs.IncomeTaxSubscriptionConnectorStub._
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.AuthStub._
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.businessAccountingMethodKey
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.routes

class BusinessAccountingMethodControllerISpec extends ComponentSpecBase {

  val id: String = "test-id"

  "GET /report-quarterly/income-and-expenses/sign-up/self-employments/client/details/business-accounting-method" when {

    "the Connector receives no content" should {
      "return the page with no prepopulated fields" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, businessAccountingMethodKey)(NO_CONTENT)

        When("GET /client/details/business-accounting-method is called")
        val res = getClientBusinessAccountingMethod(id)

        Then("should return an OK with the BusinessAccountingMethodPage")
        res must have(
          httpStatus(OK),
          pageTitle("What accounting method does your client use for their sole trader businesses?" + agentTitleSuffix),
          radioButtonSet(id = "businessAccountingMethod", selectedRadioButton = None)
        )
      }
    }

    "Connector returns a previously selected Accounting method option" should {
      "show the current business accounting method page with previously selected option" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, businessAccountingMethodKey)(OK, Json.toJson(testAccountingMethodModel))

        When("GET /client/details/business-accounting-method is called")
        val res = getClientBusinessAccountingMethod(id)

        val expectedText = removeHtmlMarkup(messages("agent.business.accounting-method.cash"))

        Then("should return an OK with the BusinessAccountingMethodPage")
        res must have(
          httpStatus(OK),
          pageTitle("What accounting method does your client use for their sole trader businesses?" + agentTitleSuffix),
          radioButtonSet(id = "businessAccountingMethod", selectedRadioButton = Some(expectedText))
        )
      }
    }

  }

  "POST /report-quarterly/income-and-expenses/sign-up/self-employments/details/business-accounting-method" should {
    "not in edit mode" when {
      "the form data is valid and connector stores it successfully" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubSaveSubscriptionData(reference, businessAccountingMethodKey, Json.toJson(testAccountingMethodModel))(OK)

        When("POST /client/details/business-accounting-method is called")
        val res = submitClientBusinessAccountingMethod(Some(testAccountingMethodModel), id = id)

        Then("Should return a SEE_OTHER with a redirect location of accounting method(this is temporary)")
        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.SelfEmployedCYAController.show(id).url)
        )
      }

      "the form data is invalid and connector stores it unsuccessfully" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubSaveSubscriptionData(reference, businessAccountingMethodKey, Json.toJson("invalid"))(OK)

        When("POST /client/details/business-accounting-method is called")
        val res = submitClientBusinessAccountingMethod(None, id = id)


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
        stubSaveSubscriptionData(reference, businessAccountingMethodKey,
          Json.toJson(testAccountingMethodModel))(OK)

        When("POST /client/details/business-accounting-method?isEditMode=true is called")
        val res = submitClientBusinessAccountingMethod(Some(testAccountingMethodModel), inEditMode = true, id = id)


        Then("Should return a SEE_OTHER with a redirect location of accounting method(this is temporary)")
        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.SelfEmployedCYAController.show(id, isEditMode = true).url)
        )
      }

      "the form data is invalid and connector stores it unsuccessfully" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubSaveSubscriptionData(reference, businessAccountingMethodKey, Json.toJson("invalid"))(OK)

        When("POST /client/details/business-accounting-method is called")
        val res = submitClientBusinessAccountingMethod(None, id = id)


        Then("Should return a BAD_REQUEST and THE FORM With errors")
        res must have(
          httpStatus(BAD_REQUEST),
        )
      }
    }

  }
}
