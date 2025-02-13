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
import helpers.IntegrationTestConstants._
import helpers.servicemocks.AuthStub._
import helpers.{ComponentSpecBase, ViewSpec}
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.{incomeSourcesComplete, soleTraderBusinessesKey}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{DateModel, SoleTraderBusinesses}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.AccountingPeriodUtil

class BusinessStartDateControllerISpec extends ComponentSpecBase with ViewSpec with FeatureSwitching {

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  private val testStartDate: DateModel = DateModel.dateConvert(AccountingPeriodUtil.getStartDateLimit)
  private val pageTitle = "Start date for sole trader business"

  val soleTraderBusinessesWithoutStartDate: SoleTraderBusinesses = soleTraderBusinesses.copy(
    businesses = soleTraderBusinesses.businesses.map(_.copy(startDate = None))
  )

  "GET /report-quarterly/income-and-expenses/sign-up/self-employments/details/business-start-date" when {

    "the Connector receives no content" should {
      "return the page with no prepopulated fields" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinessesWithoutStartDate))

        When("GET /business-start-date is called")
        val res = getBusinessStartDate(id)

        Then("should return an OK with the BusinessStartDatePage")
        res must have(
          httpStatus(OK),
          pageTitle(pageTitle + titleSuffix),
          dateField("startDate", DateModel("", "", ""))
        )
      }
    }

    "Connector returns a previously filled in Business Start Date" should {
      "show the current date of commencement page with date values entered" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinesses))

        When("GET /business-start-date is called")
        val res = getBusinessStartDate(id)

        Then("should return an OK with the BusinessStartDatePage")

        res must have(
          httpStatus(OK),
          pageTitle(pageTitle + titleSuffix),
          dateField("startDate", testStartDate)
        )
      }
    }

  }

  "POST /report-quarterly/income-and-expenses/sign-up/self-employments/details/business-start-date" when {
    "not in edit mode" when {
      "the form data is valid and connector stores it successfully" should {
        "redirect to the address check route" in {
          Given("I setup the Wiremock stubs")
          stubAuthSuccess()
          stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinessesWithoutStartDate))
          stubSaveSubscriptionData(reference, soleTraderBusinessesKey, Json.toJson(soleTraderBusinesses))(OK)
          stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)

          When("POST /business-start-date is called")
          val res = submitBusinessStartDate(Some(testStartDate), id)

          Then("Should return a SEE_OTHER with a redirect to the address check route")
          res must have(
            httpStatus(SEE_OTHER),
            redirectURI(businessAddressCheckUri(id))
          )
        }
      }

      "the form data is invalid" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinessesWithoutStartDate))

        When("POST /business-start-date is called")
        val res = submitBusinessStartDate(None, id)

        Then("Should return a BAD_REQUEST and THE FORM With errors")
        res must have(
          httpStatus(BAD_REQUEST),
          pageTitle("Error: " + pageTitle + titleSuffix)
        )
      }
    }

    "in edit mode" when {
      "the form data is valid and connector stores it successfully" should {
        "redirect to Self-employment Check Your Answer page" in {
          Given("I setup the Wiremock stubs")
          stubAuthSuccess()
          stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinessesWithoutStartDate))
          stubSaveSubscriptionData(reference, soleTraderBusinessesKey, Json.toJson(soleTraderBusinesses))(OK)
          stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)

          When("POST /business-start-date is called")
          val res = submitBusinessStartDate(Some(testStartDate), id, inEditMode = true)


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
