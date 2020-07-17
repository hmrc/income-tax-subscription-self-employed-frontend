
package controllers

import connectors.stubs.IncomeTaxSubscriptionConnectorStub._
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.AuthStub.stubAuthSuccess
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.BusinessNameController

class BusinessNameControllerISpec extends ComponentSpecBase {

  val businessId: String = "testId"

  "GET /report-quarterly/income-and-expenses/sign-up/self-employments/details/business-name" when {

    "the Connector is empty" should {
      "return the page with no prepopulated fields" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSelfEmployments(BusinessNameController.businessNameKey)(NO_CONTENT)

        When("GET /details/business-name is called")
        val res = getBusinessName(businessId)

        Then("should return an OK with the BusinessNamePage")
        res must have(
          httpStatus(OK),
          pageTitle("What is the name of your business?")
        )
      }
    }
    "Connector returns a previously filled in Business Name" should {
      "show the current business name page with the name value entered" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSelfEmployments(BusinessNameController.businessNameKey)(OK, Json.toJson(testBusinessNameModel))

        When("GET /details/business-name is called")
        val res = getBusinessName(businessId)

        Then("should return an OK with the BusinessNamePage")
        res must have(
          httpStatus(OK),
          pageTitle("What is the name of your business?"),
          textField("businessName", testBusinessName)

        )


      }


    }


  }
  "POST /report-quarterly/income-and-expenses/sign-up/self-employments/details/business-name" when {
    "not in edit mode" when {
      "the form data is valid and is stored successfully" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubSaveSelfEmployments(BusinessNameController.businessNameKey, Json.toJson(testBusinessNameModel))(CREATED)

        When("Post /details/business-name is called")
        val res = submitBusinessName(businessId, inEditMode = false, Some(testBusinessNameModel))

        Then("should return a SEE_OTHER")
        res must have(
          httpStatus(SEE_OTHER)
        )
      }

      "the form data is invalid and connector stores it unsuccessfully" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubSaveSelfEmployments(BusinessNameController.businessNameKey, Json.toJson(testEmptyBusinessNameModel))(OK)

        When("POST /details/business-name")
        val res = submitBusinessName(businessId, inEditMode = false, Some(testEmptyBusinessNameModel))


        Then("Should return a BAD_REQUEST and THE FORM With errors")
        res must have(
          httpStatus(BAD_REQUEST),
          pageTitle("Error: What is the name of your business?")
        )
      }
    }
    "in edit mode" when {
      "the form data is valid and is stored successfully and redirected to Check Your Answers" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubSaveSelfEmployments(BusinessNameController.businessNameKey, Json.toJson(testBusinessNameModel))(CREATED)

        When("Post /details/business-name is called")
        val res = submitBusinessName(businessId, inEditMode = true, Some(testBusinessNameModel))

        Then("should return a SEE_OTHER")
        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(BusinessListCYAUri)
        )
      }
    }
  }
}

