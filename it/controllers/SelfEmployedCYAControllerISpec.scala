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

import connectors.stubs.IncomeTaxSubscriptionConnectorStub.{stubGetSubscriptionData, stubSaveSubscriptionData}
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants.{BusinessCYAUri, taskListURI, testAccountingMethodModel}
import helpers.servicemocks.AuthStub.stubAuthSuccess
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND, OK, SEE_OTHER}
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.{businessAccountingMethodKey, businessesKey}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitch.SaveAndRetrieve
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._

import java.time.LocalDate

class SelfEmployedCYAControllerISpec  extends ComponentSpecBase with FeatureSwitching {
  val businessId: String = "testId"

  val testBusinessName: String = "businessName"
  val testBusinessNameModel: BusinessNameModel = BusinessNameModel(testBusinessName)
  val testStartDate: DateModel = DateModel.dateConvert(LocalDate.now)
  val testValidStartDate: DateModel = DateModel.dateConvert(LocalDate.now.minusYears(3))
  val testValidBusinessStartDateModel: BusinessStartDate = BusinessStartDate(testValidStartDate)
  val testValidBusinessTradeName: String = "Plumbing"
  val testValidBusinessTradeNameModel: BusinessTradeNameModel = BusinessTradeNameModel(testValidBusinessTradeName)
  val testBusinessAddressModel: BusinessAddressModel = BusinessAddressModel("testId1", Address(Seq("line1", "line2", "line3"), Some("TF3 4NT")))

  val testBusinesses: Seq[SelfEmploymentData] = Seq(
    SelfEmploymentData(
      "testId",
      businessName = Some(testBusinessNameModel), businessStartDate = Some(testValidBusinessStartDateModel),
      businessTradeName = Some(testValidBusinessTradeNameModel),
      businessAddress = Some(testBusinessAddressModel)
    ))

  val testIncompleteBusinesses: Seq[SelfEmploymentData] = Seq(
    SelfEmploymentData(
      "testId",
      businessName = Some(testBusinessNameModel), businessStartDate = Some(testValidBusinessStartDateModel),
      businessTradeName = Some(testValidBusinessTradeNameModel)
    ))

  val testConfirmedBusinesses: Seq[SelfEmploymentData] = Seq(
    SelfEmploymentData(
      "testId",
      confirmed = true,
      businessName = Some(testBusinessNameModel), businessStartDate = Some(testValidBusinessStartDateModel),
      businessTradeName = Some(testValidBusinessTradeNameModel),
      businessAddress = Some(testBusinessAddressModel)
    ))

  "GET /report-quarterly/income-and-expenses/sign-up/self-employments/details/business-check-your-answers" should {
    "return OK" when {
      "the save & retrieve feature switch is enabled" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, businessAccountingMethodKey)(OK, Json.toJson(testAccountingMethodModel))
        stubGetSubscriptionData(reference, businessesKey)(OK, Json.toJson(testBusinesses))
        And("save & retrieve feature switch is enabled")
        enable(SaveAndRetrieve)

        When("GET /details/business-check-your-answers is called")
        val res = getBusinessCheckYourAnswers(businessId, false )

        Then("should return an OK with the SelfEmployedCYA page")
        res must have(
          httpStatus(OK),
          pageTitle("Check your details" + titleSuffix)
        )
      }
    }

    "return NOT_FOUND" when {
      "the save & retrieve feature switch is disabled" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        And("save & retrieve feature switch is enabled")
        disable(SaveAndRetrieve)

        When("GET /details/business-check-your-answers is called")
        val res = getBusinessCheckYourAnswers(businessId, false)

        Then("Should return NOT FOUND")
        res must have(
          httpStatus(NOT_FOUND)
        )
      }
    }

    "return INTERNAL_SERVER_ERROR" when {
      "the accounting method cannot be retrieved" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        And("save & retrieve feature switch is enabled")
        enable(SaveAndRetrieve)

        When("GET /details/business-check-your-answers is called")
        val res = getBusinessCheckYourAnswers(businessId, false)

        Then("Should return INTERNAL_SERVER_ERROR")
        res must have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }

      "self employment data cannot be retrieved" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, businessAccountingMethodKey)(OK, Json.toJson(testAccountingMethodModel))
        And("save & retrieve feature switch is enabled")
        enable(SaveAndRetrieve)

        When("GET /details/business-check-your-answers is called")
        val res = getBusinessCheckYourAnswers(businessId, false)

        Then("Should return INTERNAL_SERVER_ERROR")
        res must have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }
  }

  "POST /report-quarterly/income-and-expenses/sign-up/self-employments/details/business-check-your-answers" should {
    "redirect to the task list page" in {
      Given("I setup the Wiremock stubs")
      stubAuthSuccess()
      stubGetSubscriptionData(reference, businessAccountingMethodKey)(OK, Json.toJson(testAccountingMethodModel))
      stubGetSubscriptionData(reference, businessesKey)(OK, Json.toJson(testBusinesses))
      stubSaveSubscriptionData(reference, businessesKey, Json.toJson(testConfirmedBusinesses))(OK)
      And("save & retrieve feature switch is enabled")
      enable(SaveAndRetrieve)

      When("GET /details/business-check-your-answers is called")
      val res = submitBusinessCheckYourAnswers(businessId)

      Then("Should return a SEE_OTHER with a redirect location of task list page")
      res must have(
        httpStatus(SEE_OTHER),
        redirectURI(taskListURI)
      )
    }

    "redirect to the self employed CYA page" in {
      Given("I setup the Wiremock stubs")
      stubAuthSuccess()
      stubGetSubscriptionData(reference, businessAccountingMethodKey)(OK, Json.toJson(testAccountingMethodModel))
      stubGetSubscriptionData(reference, businessesKey)(OK, Json.toJson(testIncompleteBusinesses))
      And("save & retrieve feature switch is enabled")
      enable(SaveAndRetrieve)

      When("GET /details/business-check-your-answers is called")
      val res = submitBusinessCheckYourAnswers(businessId)

      Then("Should return a SEE_OTHER with a redirect location of self-employed CYA page")
      res must have(
        httpStatus(SEE_OTHER),
        redirectURI(BusinessCYAUri)
      )
    }

    "return NOT_FOUND" when {
      "the save & retrieve feature switch is disabled" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        And("save & retrieve feature switch is disabled")
        disable(SaveAndRetrieve)

        When("GET /details/business-check-your-answers is called")
        val res = submitBusinessCheckYourAnswers(businessId)

        Then("Should return NOT FOUND")
        res must have(
          httpStatus(NOT_FOUND)
        )
      }
    }

    "return INTERNAL_SERVER_ERROR" when {
      "the accounting method cannot be retrieved" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        And("save & retrieve feature switch is enabled")
        enable(SaveAndRetrieve)

        When("GET /details/business-check-your-answers is called")
        val res = submitBusinessCheckYourAnswers(businessId)

        Then("Should return INTERNAL_SERVER_ERROR")
        res must have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }

      "self employment data cannot be retrieved" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, businessAccountingMethodKey)(OK, Json.toJson(testAccountingMethodModel))
        And("save & retrieve feature switch is enabled")
        enable(SaveAndRetrieve)

        When("GET /details/business-check-your-answers is called")
        val res = submitBusinessCheckYourAnswers(businessId)

        Then("Should return INTERNAL_SERVER_ERROR")
        res must have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }

      "self employment data cannot be saved" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, businessAccountingMethodKey)(OK, Json.toJson(testAccountingMethodModel))
        stubGetSubscriptionData(reference, businessesKey)(OK, Json.toJson(testBusinesses))
        stubSaveSubscriptionData(reference, businessesKey, Json.toJson(testConfirmedBusinesses))(INTERNAL_SERVER_ERROR)
        And("save & retrieve feature switch is enabled")
        enable(SaveAndRetrieve)

        When("GET /details/business-check-your-answers is called")
        val res = submitBusinessCheckYourAnswers(businessId)

        Then("Should return INTERNAL_SERVER_ERROR")
        res must have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }
  }
}
