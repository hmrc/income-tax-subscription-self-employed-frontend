
package controllers.agent

import connectors.stubs.IncomeTaxSubscriptionConnectorStub._
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.AuthStub._
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.BusinessAccountingMethodController

class BusinessAccountingMethodControllerISpec extends ComponentSpecBase {

  "GET /report-quarterly/income-and-expenses/sign-up/self-employments/client/details/business-accounting-method" when {

    "the Connector receives no content" should {
      "return the page with no prepopulated fields" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSelfEmployments(BusinessAccountingMethodController.businessAccountingMethodKey)(NO_CONTENT)

        When("GET /client/details/business-accounting-method is called")
        val res = getClientBusinessAccountingMethod()

        Then("should return an OK with the BusinessAccountingMethodPage")
        res must have(
          httpStatus(OK),
          pageTitle("What accounting method does your client use for their self-employed business?"),
          radioButtonSet(id = "businessAccountingMethod", selectedRadioButton = None)
        )
      }
    }


    "Connector returns a previously selected Accounting method option" should {
      "show the current business accounting method page with previously selected option" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSelfEmployments(BusinessAccountingMethodController.businessAccountingMethodKey)(OK,
          Json.toJson(testAccountingMethodModel))

        When("GET /client/details/business-accounting-method is called")
        val res = getClientBusinessAccountingMethod()

        val expectedText = removeHtmlMarkup(messages("agent.business.accounting_method.cash"))

        Then("should return an OK with the BusinessAccountingMethodPage")
        res must have(
          httpStatus(OK),
          pageTitle("What accounting method does your client use for their self-employed business?"),
          radioButtonSet(id = "businessAccountingMethod", selectedRadioButton = Some(expectedText))
        )
      }
    }

  }

  "POST /report-quarterly/income-and-expenses/sign-up/self-employments/details/business-accounting-method" when {
    "the form data is valid and connector stores it successfully" in {
      Given("I setup the Wiremock stubs")
      stubAuthSuccess()
      stubSaveSelfEmployments(BusinessAccountingMethodController.businessAccountingMethodKey,
        Json.toJson(testAccountingMethodModel))(OK)

      When("POST /client/details/business-accounting-method is called")
      val res = submitClientBusinessAccountingMethod(Some(testAccountingMethodModel))


      Then("Should return a SEE_OTHER with a redirect location of accounting method(this is temporary)")
      res must have(
        httpStatus(SEE_OTHER),
        redirectURI(ClientBusinessAccountingMethodUri)
      )
    }

    "the form data is invalid and connector stores it unsuccessfully" in {
      Given("I setup the Wiremock stubs")
      stubAuthSuccess()
      stubSaveSelfEmployments(BusinessAccountingMethodController.businessAccountingMethodKey,
        Json.toJson("invalid"))(OK)

      When("POST /client/details/business-accounting-method is called")
      val res = submitClientBusinessAccountingMethod(None)


      Then("Should return a BAD_REQUEST and THE FORM With errors")
      res must have(
        httpStatus(BAD_REQUEST),
      )
    }

  }
}
