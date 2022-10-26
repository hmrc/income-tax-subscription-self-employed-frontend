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
import helpers.servicemocks.AuthStub._
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.{businessAccountingMethodKey, businessesKey}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{Address, BusinessAddressModel, BusinessNameModel, BusinessStartDate, BusinessTradeNameModel, DateModel, SelfEmploymentData}

class BusinessAccountingMethodControllerISpec extends ComponentSpecBase with FeatureSwitching {

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val id: String = "test-id"

  val testBusinesses: Seq[SelfEmploymentData] = Seq(
    SelfEmploymentData(
      id = id,
      businessStartDate = Some(BusinessStartDate(DateModel("1", "1", "1980"))),
      businessName = Some(BusinessNameModel("testBusinessName")),
      businessTradeName = Some(BusinessTradeNameModel("testBusinessTrade")),
      businessAddress = Some(BusinessAddressModel(auditRef = "testAuditRef", address = Address(lines = Seq("line 1"), postcode = Some("ZZ1 1ZZ"))))
    )
  )

  "GET /report-quarterly/income-and-expenses/sign-up/self-employments/details/business-accounting-method" when {

    "the Connector receives no content" should {
      "return the page with no prepopulated fields" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, businessAccountingMethodKey)(NO_CONTENT)
        stubGetSubscriptionData(reference, businessesKey)(OK, Json.toJson(testBusinesses))

        When("GET /details/business-accounting-method is called")
        val res = getBusinessAccountingMethod(id)

        Then("should return an OK with the BusinessAccountingMethodPage")
        res must have(
          httpStatus(OK),
          pageTitle("What accounting method do you use for your sole trader business?" + titleSuffix),
          radioButtonSet(id = "businessAccountingMethod", selectedRadioButton = None)
        )
      }
    }

    "Connector returns a previously selected Accounting method option" when {
      "not in edit mode" should {
        "show the current business accounting method page with previously selected option and a continue button" in {
          Given("I setup the Wiremock stubs")
          stubAuthSuccess()
          stubGetSubscriptionData(reference, businessAccountingMethodKey)(OK, Json.toJson(testAccountingMethodModel))
          stubGetSubscriptionData(reference, businessesKey)(OK, Json.toJson(testBusinesses))

          When("GET /details/business-accounting-method is called")
          val res = getBusinessAccountingMethod(id)

          val expectedLabel = removeHtmlMarkup(messages("business.accounting_method.cash.label"))
          val expectedHint = removeHtmlMarkup(messages("business.accounting_method.cash.hint"))

          Then("should return an OK with the BusinessAccountingMethodPage")
          res must have(
            httpStatus(OK),
            pageTitle("What accounting method do you use for your sole trader business?" + titleSuffix),
            elementTextByClass(cssClass = "govuk-button")("Save and continue"),
            govukRadioButtonSet(id = "businessAccountingMethod", expectedLabel, expectedHint)
          )
        }
      }

      "in edit mode" should {
        "show the current business accounting method page with previously selected option and an update button" in {
          Given("I setup the Wiremock stubs")
          stubAuthSuccess()
          stubGetSubscriptionData(reference, businessAccountingMethodKey)(OK, Json.toJson(testAccountingMethodModel))
          stubGetSubscriptionData(reference, businessesKey)(OK, Json.toJson(testBusinesses))

          When("GET /details/business-accounting-method is called")
          val res = getBusinessAccountingMethod(id, inEditMode = true)

          val expectedLabel = removeHtmlMarkup(messages("business.accounting_method.cash.label"))
          val expectedHint = removeHtmlMarkup(messages("business.accounting_method.cash.hint"))

          Then("should return an OK with the BusinessAccountingMethodPage")
          res must have(
            httpStatus(OK),
            pageTitle("What accounting method do you use for your sole trader business?" + titleSuffix),
            elementTextByClass("govuk-button")("Save and continue"),
            govukRadioButtonSet(id = "businessAccountingMethod", expectedLabel, expectedHint)
          )
        }
      }
    }
  }

  "POST /report-quarterly/income-and-expenses/sign-up/self-employments/details/business-accounting-method" when {
    "the form data is valid and connector stores it successfully" when {
      "not in edit mode" when {
        "redirect to self employed check your answer page" in {
          Given("I setup the Wiremock stubs")
          stubAuthSuccess()
          stubSaveSubscriptionData(reference, businessAccountingMethodKey, Json.toJson(testAccountingMethodModel))(OK)

          When("POST /details/business-accounting-method is called")
          val res = submitBusinessAccountingMethod(Some(testAccountingMethodModel), id = id)

          Then("Should return a SEE_OTHER with a redirect location of routing controller in Subscription FE")
          res must have(
            httpStatus(SEE_OTHER),
            redirectURI(BusinessCYAUri)
          )
        }
      }

      "in edit mode" when {
        "redirect to self employed check your answer page" in {
          Given("I setup the Wiremock stubs")
          stubAuthSuccess()
          stubSaveSubscriptionData(reference, businessAccountingMethodKey, Json.toJson(testAccountingMethodModel))(OK)

          When("POST /details/business-accounting-method is called")
          val res = submitBusinessAccountingMethod(Some(testAccountingMethodModel), inEditMode = true, id = id)

          Then("Should return a SEE_OTHER with a redirect location of routing controller in Subscription FE")
          res must have(
            httpStatus(SEE_OTHER),
            redirectURI(BusinessCYAUri)
          )
        }
      }
    }

    "the form data is invalid and connector stores it unsuccessfully" in {
      Given("I setup the Wiremock stubs")
      stubAuthSuccess()
      stubSaveSubscriptionData(reference, businessAccountingMethodKey, Json.toJson("invalid"))(OK)
      stubGetSubscriptionData(reference, businessesKey)(OK, Json.toJson(testBusinesses))

      When("POST /details/business-accounting-method is called")
      val res = submitBusinessAccountingMethod(None, id = id)

      Then("Should return a BAD_REQUEST and THE FORM With errors")
      res must have(
        httpStatus(BAD_REQUEST),
        pageTitle("Error: What accounting method do you use for your sole trader business?" + titleSuffix)
      )
    }
  }
}
