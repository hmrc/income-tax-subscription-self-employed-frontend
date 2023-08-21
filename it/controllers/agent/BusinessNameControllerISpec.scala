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
import helpers.servicemocks.AuthStub._
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.businessesKey
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.routes
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{BusinessNameModel, SelfEmploymentData}


class BusinessNameControllerISpec extends ComponentSpecBase {

  val crypto: ApplicationCrypto = app.injector.instanceOf[ApplicationCrypto]
  val businessId: String = "testId"

  val testBusinessName: String = "testBusinessName"
  val testBusinessNameModel: BusinessNameModel = BusinessNameModel(testBusinessName)
  val testBusinesses: Seq[SelfEmploymentData] = Seq(SelfEmploymentData(businessId, businessName = Some(testBusinessNameModel.encrypt(crypto.QueryParameterCrypto))))
  val testEmptyBusinessNameModel: BusinessNameModel = BusinessNameModel("")
  val testEmptyBusinesses: Seq[SelfEmploymentData] = Seq(SelfEmploymentData(businessId, businessName = Some(testEmptyBusinessNameModel)))


  "GET /report-quarterly/income-and-expenses/sign-up/self-employments/client/details/business-name" when {

    "the Connector receives no content" should {
      "return the page with no prepopulated fields" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, businessesKey)(NO_CONTENT)

        When("GET /client/details/business-name is called")
        val res = getClientBusinessName(businessId)

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
        stubGetSubscriptionData(reference, businessesKey)(OK, Json.toJson(testBusinesses))

        When("GET /client/business/start-date is called")
        val res = getClientBusinessName(businessId)

        Then("should return an OK with the ClientBusinessName Page")
        res must have(
          httpStatus(OK),
          textField("businessName", "testBusinessName")
        )
      }
    }

  }

  "POST /report-quarterly/income-and-expenses/sign-up/self-employments/client/details/business-name" when {
    "not in edit mode" when {
      "the form data is valid and connector stores it successfully" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, businessesKey)(NO_CONTENT)
        stubSaveSubscriptionData(reference, businessesKey, Json.toJson(testBusinesses))(OK)

        When("POST /client/details/business-name is called")
        val res = submitClientBusinessName(businessId, false, Some(testBusinessNameModel))

        Then("Should return a SEE_OTHER with a redirect location of business name")
        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.BusinessStartDateController.show(businessId).url)
        )
      }

      "the form data is invalid" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, businessesKey)(NO_CONTENT)
        stubSaveSubscriptionData(reference, businessesKey, Json.toJson(testEmptyBusinesses))(OK)

        When("POST /client/details/business-name is called")
        val res = submitClientBusinessName(businessId, false, Some(testEmptyBusinessNameModel))

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
        stubGetSubscriptionData(reference, businessesKey)(NO_CONTENT)
        stubSaveSubscriptionData(reference, businessesKey, Json.toJson(testBusinesses))(OK)

        When("POST /client/details/business-name is called")
        val res = submitClientBusinessName(businessId, true, Some(testBusinessNameModel))

        Then("Should return a SEE_OTHER with a redirect location of business name")
        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.SelfEmployedCYAController.show(businessId, isEditMode = true).url)
        )
      }

      "the form data is invalid" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, businessesKey)(NO_CONTENT)
        stubSaveSubscriptionData(reference, businessesKey, Json.toJson(testEmptyBusinesses))(OK)

        When("POST /client/details/business-name is called")
        val res = submitClientBusinessName(businessId, true, Some(testEmptyBusinessNameModel))

        Then("Should return a BAD_REQUEST and THE FORM With errors")
        res must have(
          httpStatus(BAD_REQUEST)
        )
      }
    }
  }
}
