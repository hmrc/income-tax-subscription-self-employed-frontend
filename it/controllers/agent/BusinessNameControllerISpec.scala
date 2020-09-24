
package controllers.agent

import java.time.LocalDate

import connectors.stubs.IncomeTaxSubscriptionConnectorStub._
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.AuthStub._
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.BusinessNameModel


class BusinessNameControllerISpec extends ComponentSpecBase {

  val testValidBusinessNameModel: BusinessNameModel = BusinessNameModel("testBusinessName")
  val testEmptyBusinessNameModel: BusinessNameModel = BusinessNameModel("")


  "GET /report-quarterly/income-and-expenses/sign-up/self-employments/client/details/business-name" when {

    "the Connector receives no content" should {
      "return the page with no prepopulated fields" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSelfEmployments("BusinessName")(NO_CONTENT)

        When("GET /client/business/start-date is called")
        val res = getClientBusinessName()

        Then("should return an OK with the ClientBusinessName Page")
        res must have(
          httpStatus(OK)
        )
      }
    }

    "Connector returns a previously filled in ClientBusinessName" should {
      "show the current date of commencement page with date values entered" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSelfEmployments("BusinessName")(OK, Json.toJson(testValidBusinessNameModel))

        When("GET /client/business/start-date is called")
        val res = getClientBusinessName()

        Then("should return an OK with the ClientBusinessName Page")
        res must have(
          httpStatus(OK),
          textField("businessName", "testBusinessName")
        )
      }
    }

  }

  "POST /report-quarterly/income-and-expenses/sign-up/self-employments/client/details/business-name" when {
    "the form data is valid and connector stores it successfully" in {
      Given("I setup the Wiremock stubs")
      stubAuthSuccess()
      stubSaveSelfEmployments("BusinessName", Json.toJson(testValidBusinessNameModel))(OK)

      When("POST /client/details/business-name is called")
      val res = submitClientBusinessName(Some(testValidBusinessNameModel))

      Then("Should return a SEE_OTHER with a redirect location of business name")
      res must have(
        httpStatus(SEE_OTHER),
          redirectURI(ClientBusinessNameUri)
      )
    }

    "the form data is valid and connector stores it successfully in edit mode" in {
      Given("I setup the Wiremock stubs")
      stubAuthSuccess()
      stubSaveSelfEmployments("BusinessName", Json.toJson(testValidBusinessNameModel))(OK)

      When("POST /business/start-date is called")
      val res = submitClientBusinessName(Some(testValidBusinessNameModel), inEditMode = true)


      Then("Should return a SEE_OTHER with a redirect location of ClientBusinessName")
      res must have(
        httpStatus(SEE_OTHER),

        redirectURI(ClientBusinessNameUri)
      )
    }

    "the form data is invalid and connector stores it unsuccessfully" in {
      Given("I setup the Wiremock stubs")
      stubAuthSuccess()
      stubSaveSelfEmployments("BusinessName", Json.toJson(testEmptyBusinessNameModel))(OK)

      When("POST /business/start-date is called")
      val res = submitClientBusinessName(Some(testEmptyBusinessNameModel))

      Then("Should return a BAD_REQUEST and THE FORM With errors")
      res must have(
        httpStatus(BAD_REQUEST)
      )
    }

  }
}
