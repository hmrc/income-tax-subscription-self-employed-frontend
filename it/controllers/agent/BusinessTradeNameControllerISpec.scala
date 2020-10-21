
package controllers.agent

import connectors.stubs.IncomeTaxSubscriptionConnectorStub._
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.AuthStub._
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.businessesKey
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{BusinessNameModel, BusinessStartDate, BusinessTradeNameModel, DateModel, SelfEmploymentData}

class BusinessTradeNameControllerISpec extends ComponentSpecBase {
  val businessId: String = "testId"
  val testValidBusinessTradeName: String = "Plumbing"
  val testInvalidBusinessTradeName: String = "!()+{}?^~"
  val testValidBusinessTradeNameModel: BusinessTradeNameModel = BusinessTradeNameModel(testValidBusinessTradeName)
  val testInvalidBusinessTradeNameModel: BusinessTradeNameModel = BusinessTradeNameModel(testInvalidBusinessTradeName)

  val testBusiness: SelfEmploymentData = SelfEmploymentData(
    id = businessId,
    businessStartDate = Some(BusinessStartDate(DateModel("1", "1", "1"))),
    businessName = Some(BusinessNameModel("testName")),
    businessTradeName = Some(testValidBusinessTradeNameModel)
  )

  "GET /report-quarterly/income-and-expenses/sign-up/self-employments/client/details/business-trade" when {

    "the user hasn't entered their business name" should {
      "redirect to the business name page" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSelfEmployments(businessesKey)(OK, Json.toJson(Seq(testBusiness.copy(businessName = None, businessTradeName = None))))

        When("GET /client/details/business-trade is called")
        val res = getClientTradeName(businessId)

        Then("should return a SEE_OTHER to the business name page")
        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(ClientBusinessNameUri)
        )
      }
    }

    "the Connector receives no content" should {
      "return the page with no prepopulated fields" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSelfEmployments(businessesKey)(OK, Json.toJson(Seq(testBusiness.copy(businessTradeName = None))))

        When("GET /client/details/business-trade is called")
        val res = getClientTradeName(businessId)

        Then("should return an OK with the BusinessTradeNamePage")
        res must have(
          httpStatus(OK),
          pageTitle("What is the trade of your client’s business?" + titleSuffix),
          textField("businessTradeName", "")
        )
      }
    }

    "Connector returns a previously filled in Business Trade Name" should {
      "show the current trade name page with value entered" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSelfEmployments(businessesKey)(OK, Json.toJson(Seq(testBusiness)))

        When("GET /client/details/business-trade is called")
        val res = getClientTradeName(businessId)

        Then("should return an OK with the BusinessTradeNamePage")
        res must have(
          httpStatus(OK),
          textField("businessTradeName", testValidBusinessTradeName)
        )
      }
    }
  }

  "POST /report-quarterly/income-and-expenses/sign-up/self-employments/client/details/business-trade" when {
    "not in edit mode" when {
      "the form data is valid and connector stores it successfully" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSelfEmployments(businessesKey)(OK, Json.toJson(Seq(testBusiness.copy(businessTradeName = None))))
        stubSaveSelfEmployments("BusinessTradeName", Json.toJson(testValidBusinessTradeNameModel))(OK)

        When("POST /client/details/business-trade is called")
        val res = submitClientTradeName(businessId, Some(testValidBusinessTradeNameModel))

        Then("Should return a SEE_OTHER with a redirect location of Business Trade name")
        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(ClientBusinessAddressInitialiseUri)
        )
      }

      "the form data is valid but is a duplicate submission" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSelfEmployments(businessesKey)(OK, Json.toJson(Seq(
          testBusiness.copy(id = "idOne"),
          testBusiness.copy(id = "idTwo", businessTradeName = None)
        )))


        When("POST /client/details/business-trade is called")
        val res = submitClientTradeName("idTwo", Some(testValidBusinessTradeNameModel))

        Then("Should return a SEE_OTHER")
        res must have(
          httpStatus(BAD_REQUEST),
          pageTitle("Error: What is the trade of your client’s business?" + titleSuffix)
        )
      }

      "the form data is invalid and connector stores it unsuccessfully" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSelfEmployments(businessesKey)(OK, Json.toJson(Seq(testBusiness.copy(businessTradeName = None))))

        When("POST /client/details/business-trade is called")
        val res = submitClientTradeName(businessId, Some(testInvalidBusinessTradeNameModel))

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
        stubGetSelfEmployments(businessesKey)(OK, Json.toJson(Seq(testBusiness.copy(businessTradeName = None))))
        stubSaveSelfEmployments(businessesKey, Json.toJson(testBusiness))(OK)

        When("POST /client/details/business-trade is called")
        val res = submitClientTradeName(businessId, Some(testValidBusinessTradeNameModel), true)

        Then("Should return a SEE_OTHER with a redirect location of Business Trade name")
        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(ClientBusinessListCYAUri)
        )
      }

      "the form data is invalid and connector stores it unsuccessfully" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSelfEmployments(businessesKey)(OK, Json.toJson(Seq(testBusiness.copy(businessTradeName = None))))
        stubSaveSelfEmployments("BusinessTradeName", Json.toJson(testInvalidBusinessTradeNameModel))(OK)

        When("POST /client/details/business-trade is called")
        val res = submitClientTradeName(businessId, Some(testInvalidBusinessTradeNameModel), true)

        Then("Should return a BAD_REQUEST and THE FORM With errors")
        res must have(
          httpStatus(BAD_REQUEST),
        )
      }
    }
  }
}
