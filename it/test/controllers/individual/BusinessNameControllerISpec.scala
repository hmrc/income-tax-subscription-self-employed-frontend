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

package controllers.individual

import connectors.stubs.IncomeTaxSubscriptionConnectorStub._
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.AuthStub.stubAuthSuccess
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.{incomeSourcesComplete, soleTraderBusinessesKey}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.SoleTraderBusinesses

class BusinessNameControllerISpec extends ComponentSpecBase with FeatureSwitching {

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val testBusinessName: String = "test name"

  val soleTraderBusinessesWithoutName: SoleTraderBusinesses = soleTraderBusinesses.copy(
    businesses = soleTraderBusinesses.businesses.map(_.copy(name = None))
  )

  "GET /report-quarterly/income-and-expenses/sign-up/self-employments/details/business-name" when {
    "the Connector is empty" should {
      "return the page with no prepopulated fields" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinessesWithoutName))

        When("GET /details/business-name is called")
        val res = getBusinessName(id)

        Then("should return an OK with the BusinessNamePage")
        res must have(
          httpStatus(OK),
          pageTitle("What is the name of your business?" + titleSuffix)
        )
      }
    }
    "Connector returns a previously filled in Business Name" should {
      "show the current business name page with the name value entered" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinesses))

        When("GET /details/business-name is called")
        val res = getBusinessName(id)

        Then("should return an OK with the BusinessNamePage")
        res must have(
          httpStatus(OK),
          pageTitle("What is the name of your business?" + titleSuffix),
          textField("businessName", testBusinessName)
        )
      }
    }
  }

  "POST /report-quarterly/income-and-expenses/sign-up/self-employments/details/business-name" when {
    "not in edit mode" when {
      "the form data is valid and is stored successfully" should {
        "redirect to Business Start Date page " in {
          Given("I setup the Wiremock stubs")
          stubAuthSuccess()
          stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinessesWithoutName))
          stubSaveSubscriptionData(reference, soleTraderBusinessesKey, Json.toJson(soleTraderBusinesses))(OK)
          stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)

          When("Post /details/business-name is called")
          val res = submitBusinessName(id, inEditMode = false, Some(testBusinessName))

          Then("should return a SEE_OTHER")
          res must have(
            httpStatus(SEE_OTHER),
            redirectURI(BusinessStartDateUri)
          )
        }
      }

      "the form data is invalid" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinessesWithoutName))

        When("POST /details/business-name")
        val res = submitBusinessName(id, inEditMode = false, None)

        Then("Should return a BAD_REQUEST and THE FORM With errors")
        res must have(
          httpStatus(BAD_REQUEST),
          pageTitle("Error: What is the name of your business?" + titleSuffix)
        )
      }
    }

    "in edit mode" when {
      "the form data is valid and is stored successfully" should {
        "redirect to Self-employment Check Your Answers" in {
          Given("I setup the Wiremock stubs")
          stubAuthSuccess()
          stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinessesWithoutName))
          stubSaveSubscriptionData(reference, soleTraderBusinessesKey, Json.toJson(soleTraderBusinesses))(OK)
          stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)

          When("Post /details/business-name is called")
          val res = submitBusinessName(id, inEditMode = true, Some(testBusinessName))

          Then("should return a SEE_OTHER")
          res must have(
            httpStatus(SEE_OTHER),
            redirectURI(BusinessCYAUri)
          )
        }
      }
    }
  }
}
