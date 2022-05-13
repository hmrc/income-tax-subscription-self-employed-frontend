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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.businessesKey
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitch.SaveAndRetrieve
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._

class BusinessTradeNameControllerISpec extends ComponentSpecBase with FeatureSwitching {

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  override def beforeEach(): Unit = {
    disable(SaveAndRetrieve)
    super.beforeEach()
  }

  val businessId: String = "testId"

  val maxLength = 35
  val testValidBusinessTradeName: String = "Plumbing"
  val testInvalidBusinessTradeName: String = "!()+{}?^~"
  val testValidBusinessTradeNameModel: BusinessTradeNameModel = BusinessTradeNameModel(testValidBusinessTradeName)
  val testInvalidBusinessTradeNameModel: BusinessTradeNameModel = BusinessTradeNameModel(testInvalidBusinessTradeName * maxLength + 1)

  val testBusiness: SelfEmploymentData = SelfEmploymentData(
    id = businessId,
    businessStartDate = Some(BusinessStartDate(DateModel("1", "1", "1"))),
    businessName = Some(BusinessNameModel("testName")),
    businessTradeName = Some(testValidBusinessTradeNameModel)
  )

  "GET /report-quarterly/income-and-expenses/sign-up/self-employments/details/business-trade" when {

    "the user hasn't entered their business name" should {
      "redirect to the business name page" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, businessesKey)(OK, Json.toJson(Seq(testBusiness.copy(businessName = None, businessTradeName = None))))

        When("GET /details/business-trade is called")
        val res = getBusinessTradeName(businessId)

        Then("should return a SEE_OTHER to the business name page")
        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(BusinessNameUri)
        )
      }
    }

    "the Connector receives no content" should {
      "return the page with no prepopulated fields" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, businessesKey)(OK, Json.toJson(Seq(testBusiness.copy(businessTradeName = None))))

        When("GET /details/business-trade is called")
        val res = getBusinessTradeName(businessId)

        Then("should return an OK with the BusinessTradeNamePage")
        res must have(
          httpStatus(OK),
          pageTitle("What is the trade of your business?" + titleSuffix),
          textField("businessTradeName", "")
        )
      }
    }

    "Connector returns a previously filled in Business Trade Name" should {
      "show the current business trade name page with name values entered" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, businessesKey)(OK, Json.toJson(Seq(testBusiness)))

        When("GET /details/business-trade is called")
        val res = getBusinessTradeName(businessId)

        Then("should return an OK with the BusinessTradeNamePage")
        res must have(
          httpStatus(OK),
          pageTitle("What is the trade of your business?" + titleSuffix),
          textField("businessTradeName", testValidBusinessTradeName)
        )
      }
    }

  }

  "POST /report-quarterly/income-and-expenses/sign-up/self-employments/details/business-trade" when {
    "not in edit mode" when {
      "save and retrieve feature switch is enabled" when {
        "the form data is valid and connector stores it successfully" should {
          "redirect to Business Address Look Up page" in {
            Given("I setup the Wiremock stubs")
            stubAuthSuccess()
            enable(SaveAndRetrieve)
            stubGetSubscriptionData(reference, businessesKey)(OK, Json.toJson(Seq(testBusiness.copy(businessTradeName = None))))
            stubSaveSubscriptionData(reference, businessesKey, Json.toJson(Seq(testBusiness)))(OK)

            When("POST /details/business-trade is called")
            val res = submitBusinessTradeName(businessId, inEditMode = false, Some(testValidBusinessTradeNameModel))

            Then("Should return a SEE_OTHER")
            res must have(
              httpStatus(SEE_OTHER),
              redirectURI(businessAddressInitialiseUri(businessId))
            )
          }
        }
      }

      "save and retrieve feature switch is disabled" when {
        "the form data is valid and connector stores it successfully" should {
          "redirect to Business Address Look Up page" in {
            Given("I setup the Wiremock stubs")
            stubAuthSuccess()
            stubGetSubscriptionData(reference, businessesKey)(OK, Json.toJson(Seq(testBusiness.copy(businessTradeName = None))))
            stubSaveSubscriptionData(reference, businessesKey, Json.toJson(Seq(testBusiness)))(OK)

            When("POST /details/business-trade is called")
            val res = submitBusinessTradeName(businessId, inEditMode = false, Some(testValidBusinessTradeNameModel))

            Then("Should return a SEE_OTHER")
            res must have(
              httpStatus(SEE_OTHER),
              redirectURI(businessAddressInitialiseUri(businessId))
            )
          }
        }
      }

      "the form data is valid but is a duplicate submission" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, businessesKey)(OK, Json.toJson(Seq(
          testBusiness.copy(id = "idOne"),
          testBusiness.copy(id = "idTwo", businessTradeName = None)
        )))


        When("POST /details/business-trade is called")
        val res = submitBusinessTradeName("idTwo", inEditMode = false, Some(testValidBusinessTradeNameModel))

        Then("Should return a SEE_OTHER")
        res must have(
          httpStatus(BAD_REQUEST),
          pageTitle("Error: What is the trade of your business?" + titleSuffix)
        )
      }

      "the form data is invalid and connector stores it unsuccessfully" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, businessesKey)(OK, Json.toJson(Seq(testBusiness.copy(businessTradeName = None))))

        When("POST /details/business-trade is called")
        val res = submitBusinessTradeName(businessId, inEditMode = false, Some(testInvalidBusinessTradeNameModel))

        Then("Should return a BAD_REQUEST and THE FORM With errors")
        res must have(
          httpStatus(BAD_REQUEST),
          pageTitle("Error: What is the trade of your business?" + titleSuffix)
        )
      }

    }
    "in edit mode" when {
      "save and retrieve feature switch is enabled" when {
        "the form data is valid and connector stores it successfully" should {
          "redirect to Self-employment Check Your Answer page" in {
            Given("I setup the Wiremock stubs")
            stubAuthSuccess()
            enable(SaveAndRetrieve)
            stubGetSubscriptionData(reference, businessesKey)(OK, Json.toJson(Seq(testBusiness.copy(businessTradeName = Some(BusinessTradeNameModel("test trade"))))))
            stubSaveSubscriptionData(reference, businessesKey, Json.toJson(Seq(testBusiness)))(OK)

            When("POST /details/business-trade is called")
            val res = submitBusinessTradeName(businessId, inEditMode = true, Some(testValidBusinessTradeNameModel))

            Then(s"Should return a $SEE_OTHER with a redirect location of check your answers")
            res must have(
              httpStatus(SEE_OTHER),
              redirectURI(BusinessCYAUri)
            )
          }
        }
      }
      "save and retrieve feature switch is disabled" when {
        "the form data is valid and connector stores it successfully" should {
          "redirect to Business Check Your Answer page" in {
            Given("I setup the Wiremock stubs")
            stubAuthSuccess()
            stubGetSubscriptionData(reference, businessesKey)(OK, Json.toJson(Seq(testBusiness.copy(businessTradeName = Some(BusinessTradeNameModel("test trade"))))))
            stubSaveSubscriptionData(reference, businessesKey, Json.toJson(Seq(testBusiness)))(OK)

            When("POST /details/business-trade is called")
            val res = submitBusinessTradeName(businessId, inEditMode = true, Some(testValidBusinessTradeNameModel))

            Then(s"Should return a $SEE_OTHER with a redirect location of check your answers")
            res must have(
              httpStatus(SEE_OTHER),
              redirectURI(BusinessListCYAUri)
            )
          }
        }
      }

    }
  }
}
