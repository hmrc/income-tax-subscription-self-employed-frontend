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
import helpers.IntegrationTestConstants._
import helpers.servicemocks.AuthStub._
import helpers.{ComponentSpecBase, ViewSpec}
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.businessesKey
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{BusinessStartDate, DateModel, SelfEmploymentData}

import java.time.LocalDate

class BusinessStartDateControllerISpec extends ComponentSpecBase with ViewSpec with FeatureSwitching {

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val businessId: String = "testId"

  val testStartDate: DateModel = DateModel.dateConvert(LocalDate.now)
  val testValidStartDate: DateModel = DateModel.dateConvert(LocalDate.now.minusYears(3))
  val testBusinessStartDateModel: BusinessStartDate = BusinessStartDate(testStartDate)
  val testValidBusinessStartDateModel: BusinessStartDate = BusinessStartDate(testValidStartDate)

  val testBusinesses: Seq[SelfEmploymentData] = Seq(SelfEmploymentData(id = businessId, businessStartDate = Some(testValidBusinessStartDateModel)))

  "GET /report-quarterly/income-and-expenses/sign-up/self-employments/details/business-start-date" when {

    "the Connector receives no content" should {
      "return the page with no prepopulated fields" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, businessesKey)(NO_CONTENT)

        When("GET /business-start-date is called")
        val res = getBusinessStartDate(businessId)

        Then("should return an OK with the BusinessStartDatePage")
        res must have(
          httpStatus(OK),
          pageTitle("When did your sole trader business start trading?" + titleSuffix),
          dateField("startDate", DateModel("", "", ""))
        )
      }
    }

    "Connector returns a previously filled in Business Start Date" should {
      "show the current date of commencement page with date values entered" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, businessesKey)(OK, Json.toJson(testBusinesses))

        When("GET /business-start-date is called")
        val res = getBusinessStartDate(businessId)

        Then("should return an OK with the BusinessStartDatePage")

        res must have(
          httpStatus(OK),
          pageTitle("When did your sole trader business start trading?" + titleSuffix),
          dateField("startDate", testValidStartDate)
        )
      }
    }

  }

  "POST /report-quarterly/income-and-expenses/sign-up/self-employments/details/business-start-date" when {
    "not in edit mode" when {
      "the form data is valid and connector stores it successfully" should {
        "redirect to Business Trade Name page" in {
          Given("I setup the Wiremock stubs")
          stubAuthSuccess()
          stubGetSubscriptionData(reference, businessesKey)(NO_CONTENT)
          stubSaveSubscriptionData(reference, businessesKey, Json.toJson(testBusinesses))(OK)

          When("POST /business-start-date is called")
          val res = submitBusinessStartDate(Some(testValidBusinessStartDateModel), businessId)

          Then("Should return a SEE_OTHER with a redirect location of accounting period dates")
          res must have(
            httpStatus(SEE_OTHER),
            redirectURI(BusinessTradeNameUri)
          )
        }
      }

      "the form data is invalid" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, businessesKey)(NO_CONTENT)

        When("POST /business-start-date is called")
        val res = submitBusinessStartDate(Some(testBusinessStartDateModel), businessId)

        Then("Should return a BAD_REQUEST and THE FORM With errors")
        res must have(
          httpStatus(BAD_REQUEST),
          pageTitle("Error: When did your sole trader business start trading?" + titleSuffix)
        )
      }
    }

    "in edit mode" when {
      "the form data is valid and connector stores it successfully" should {
        "redirect to Self-employment Check Your Answer page" in {
          Given("I setup the Wiremock stubs")
          stubAuthSuccess()
          stubGetSubscriptionData(reference, businessesKey)(
            responseStatus = OK,
            responseBody = Json.toJson(testBusinesses.map(_.copy(businessStartDate = Some(BusinessStartDate(DateModel("9", "9", "9"))))))
          )
          stubSaveSubscriptionData(reference, businessesKey, Json.toJson(testBusinesses))(OK)

          When("POST /business-start-date is called")
          val res = submitBusinessStartDate(Some(testValidBusinessStartDateModel), businessId, inEditMode = true)


          Then("Should return a SEE_OTHER with a redirect location of check your answers")
          res must have(
            httpStatus(SEE_OTHER),
            redirectURI(BusinessCYAUri)
          )
        }
      }
    }
  }
}
