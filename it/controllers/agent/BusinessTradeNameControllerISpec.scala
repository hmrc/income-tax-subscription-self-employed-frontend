
package controllers.agent

import connectors.stubs.IncomeTaxSubscriptionConnectorStub._
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.AuthStub._
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.BusinessTradeNameModel

class BusinessTradeNameControllerISpec extends ComponentSpecBase {
  val businessId: String = "testId"
  val testValidBusinessTradeName: String = "Plumbing"
  val testInvalidBusinessTradeName: String = "!()+{}?^~"
  val testValidBusinessTradeNameModel: BusinessTradeNameModel = BusinessTradeNameModel(testValidBusinessTradeName)
  val testInvalidBusinessTradeNameModel: BusinessTradeNameModel = BusinessTradeNameModel(testInvalidBusinessTradeName)

  "GET /report-quarterly/income-and-expenses/sign-up/self-employments/client/details/business-trade" when {

    "the Connector receives no content" should {
      "return the page with no prepopulated fields" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSelfEmployments("BusinessTradeName")(NO_CONTENT)

        When("GET /client/details/business-trade is called")
        val res = getClientTradeName(businessId)

        Then("should return an OK with the BusinessTradeNamePage")
        res must have(
          httpStatus(OK)
        )
      }
    }

    "Connector returns a previously filled in Business Trade Name" should {
      "show the current trade name page with value entered" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSelfEmployments("BusinessTradeName")(OK, Json.toJson(testValidBusinessTradeNameModel))

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
    "the form data is valid and connector stores it successfully" in {
      Given("I setup the Wiremock stubs")
      stubAuthSuccess()
      stubSaveSelfEmployments("BusinessTradeName", Json.toJson(testValidBusinessTradeNameModel))(OK)

      When("POST /client/details/business-trade is called")
      val res = submitClientTradeName(businessId,Some(testValidBusinessTradeNameModel))

      Then("Should return a SEE_OTHER with a redirect location of Business Trade name")
      res must have(
        httpStatus(SEE_OTHER),
        redirectURI(ClientBusinessAddressInitialiseUri)
      )
    }

    "the form data is invalid and connector stores it unsuccessfully" in {
      Given("I setup the Wiremock stubs")
      stubAuthSuccess()
      stubSaveSelfEmployments("BusinessTradeName", Json.toJson(testInvalidBusinessTradeNameModel))(OK)

      When("POST /client/details/business-trade is called")
      val res = submitClientTradeName(businessId,Some(testInvalidBusinessTradeNameModel))

      Then("Should return a BAD_REQUEST and THE FORM With errors")
      res must have(
        httpStatus(BAD_REQUEST),
      )
    }
  }
}
