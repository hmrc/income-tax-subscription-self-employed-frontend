
package controllers.agent

import java.time.LocalDate

import connectors.stubs.IncomeTaxSubscriptionConnectorStub._
import helpers.IntegrationTestConstants._
import helpers.ComponentSpecBase
import helpers.servicemocks.AuthStub._
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{BusinessStartDate, DateModel}


class DateOfCommencementControllerISpec extends ComponentSpecBase {
  val businessId: String = "testId"
  val testStartDate: DateModel = DateModel.dateConvert(LocalDate.now)
  val testValidStartDate: DateModel = DateModel.dateConvert(LocalDate.now.minusYears(3))
  val testBusinessStartDateModel: BusinessStartDate = BusinessStartDate(testStartDate)
  val testValidBusinessStartDateModel: BusinessStartDate = BusinessStartDate(testValidStartDate)


  "GET /report-quarterly/income-and-expenses/sign-up/self-employments/client/details/business-start-date" when {

    "the Connector receives no content" should {
      "return the page with no prepopulated fields" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSelfEmployments("BusinessStartDate")(NO_CONTENT)

        When("GET /client/business/start-date is called")
        val res = getDateOfCommencement(businessId)

        Then("should return an OK with the DateOfCommencement Page")
        res must have(
          httpStatus(OK)
        )
      }
    }

    "Connector returns a previously filled in DateOfCommencement" should {
      "show the current date of commencement page with date values entered" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSelfEmployments("BusinessStartDate")(OK, Json.toJson(testValidBusinessStartDateModel))

        When("GET /client/business/start-date is called")
        val res = getDateOfCommencement(businessId)

        Then("should return an OK with the DateOfCommencement Page")
        res must have(
          httpStatus(OK),
          dateField("startDate", testValidStartDate)
        )
      }
    }

  }

  "POST /report-quarterly/income-and-expenses/sign-up/self-employments/client/details/business-start-date" when {
    "the form data is valid and connector stores it successfully" in {
      Given("I setup the Wiremock stubs")
      stubAuthSuccess()
      stubSaveSelfEmployments("BusinessStartDate", Json.toJson(testValidBusinessStartDateModel))(OK)

      When("POST /cient/business/start-date is called")
      val res = submitDateOfCommencement(businessId,Some(testValidBusinessStartDateModel))

      Then("Should return a SEE_OTHER with a redirect location of business name")
      res must have(
        httpStatus(SEE_OTHER),
          redirectURI(ClientBusinessNameUri)
      )
    }

    "the form data is valid and connector stores it successfully in edit mode" in {
      Given("I setup the Wiremock stubs")
      stubAuthSuccess()
      stubSaveSelfEmployments("BusinessStartDate", Json.toJson(testValidBusinessStartDateModel))(OK)

      When("POST /business/start-date is called")
      val res = submitDateOfCommencement(businessId,Some(testValidBusinessStartDateModel), inEditMode = true)


      Then("Should return a SEE_OTHER with a redirect location of DateOfCommencement")
      res must have(
        httpStatus(SEE_OTHER),

        redirectURI(ClientBusinessListCYAUri)
      )
    }

    "the form data is invalid and connector stores it unsuccessfully" in {
      Given("I setup the Wiremock stubs")
      stubAuthSuccess()
      stubSaveSelfEmployments("BusinessStartDate", Json.toJson(testBusinessStartDateModel))(OK)

      When("POST /business/start-date is called")
      val res = submitDateOfCommencement(businessId,Some(testBusinessStartDateModel))

      Then("Should return a BAD_REQUEST and THE FORM With errors")
      res must have(
        httpStatus(BAD_REQUEST)
      )
    }

  }
}
