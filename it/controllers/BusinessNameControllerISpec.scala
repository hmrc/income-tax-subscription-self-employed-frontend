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

package controllers

import connectors.stubs.IncomeTaxSubscriptionConnectorStub._
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.AuthStub.stubAuthSuccess
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.businessesKey
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{BusinessNameModel, SelfEmploymentData}

class BusinessNameControllerISpec extends ComponentSpecBase with FeatureSwitching {

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val businessId: String = "testId"

  val testBusinessName: String = "businessName"
  val testBusinessNameModel: BusinessNameModel = BusinessNameModel(testBusinessName)
  val testEmptyBusinessNameModel: BusinessNameModel = BusinessNameModel("")

  val testBusinesses: Seq[SelfEmploymentData] = Seq(SelfEmploymentData(businessId, businessName = Some(testBusinessNameModel)))

  "GET /report-quarterly/income-and-expenses/sign-up/self-employments/details/business-name" when {
    "the Connector is empty" should {
      "return the page with no prepopulated fields" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, businessesKey)(NO_CONTENT)

        When("GET /details/business-name is called")
        val res = getBusinessName(businessId)

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
        stubGetSubscriptionData(reference, businessesKey)(OK, Json.toJson(testBusinesses))

        When("GET /details/business-name is called")
        val res = getBusinessName(businessId)

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
          stubGetSubscriptionData(reference, businessesKey)(NO_CONTENT)
          stubSaveSubscriptionData(reference, businessesKey, Json.toJson(testBusinesses))(OK)

          When("Post /details/business-name is called")
          val res = submitBusinessName(businessId, inEditMode = false, Some(testBusinessNameModel))

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
        stubGetSubscriptionData(reference, businessesKey)(NO_CONTENT)

        When("POST /details/business-name")
        val res = submitBusinessName(businessId, inEditMode = false, Some(testEmptyBusinessNameModel))

        Then("Should return a BAD_REQUEST and THE FORM With errors")
        res must have(
          httpStatus(BAD_REQUEST),
          pageTitle("Error: What is the name of your business?" + titleSuffix)
        )
      }
    }

    "in edit mode" when {
      "the form data is valid and is stored successfully" should {
        " redirect to Self-employment Check Your Answers" in {
          Given("I setup the Wiremock stubs")
          stubAuthSuccess()
          stubGetSubscriptionData(reference, businessesKey)(OK, Json.toJson(testBusinesses.map(_.copy(businessName = Some(BusinessNameModel("test name"))))))
          stubSaveSubscriptionData(reference, businessesKey, Json.toJson(testBusinesses))(OK)

          When("Post /details/business-name is called")
          val res = submitBusinessName(businessId, inEditMode = true, Some(testBusinessNameModel))

          Then("should return a SEE_OTHER")
          res must have(
            httpStatus(SEE_OTHER),
            redirectURI(BusinessCYAUri)
          )
        }
      }

      "the form data is valid and is stored successfully" should {
        "redirect to Business Check Your Answers" in {
          Given("I setup the Wiremock stubs")
          stubAuthSuccess()
          stubGetSubscriptionData(reference, businessesKey)(OK, Json.toJson(testBusinesses.map(_.copy(businessName = Some(BusinessNameModel("test name"))))))
          stubSaveSubscriptionData(reference, businessesKey, Json.toJson(testBusinesses))(OK)

          When("Post /details/business-name is called")
          val res = submitBusinessName(businessId, inEditMode = true, Some(testBusinessNameModel))

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
