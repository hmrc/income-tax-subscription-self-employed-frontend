
package controllers.agent

import java.time.LocalDate

import connectors.stubs.IncomeTaxSubscriptionConnectorStub._
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.AuthStub.stubAuthSuccess
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.businessesKey
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._

class BusinessListCYAControllerISpec extends ComponentSpecBase {

  val testBusinessName: String = "businessName"
  val testBusinessNameModel: BusinessNameModel = BusinessNameModel(testBusinessName)
  val testEmptyBusinessNameModel: BusinessNameModel = BusinessNameModel("")
  val testStartDate: DateModel = DateModel.dateConvert(LocalDate.now)
  val testValidStartDate: DateModel = DateModel.dateConvert(LocalDate.now.minusYears(3))
  val testBusinessStartDateModel: BusinessStartDate = BusinessStartDate(testStartDate)
  val testValidBusinessStartDateModel: BusinessStartDate = BusinessStartDate(testValidStartDate)
  val testValidBusinessTradeName: String = "Plumbing"
  val testValidBusinessTradeNameModel: BusinessTradeNameModel = BusinessTradeNameModel(testValidBusinessTradeName)
  val testBusinessAddressModel: BusinessAddressModel = BusinessAddressModel("testId1", Address(Seq("line1", "line2", "line3"), "TF3 4NT"))
  val titleSuffix = " - Business Tax account - GOV.UK"

  val testGetAllSelfEmploymentModel: GetAllSelfEmploymentModel = GetAllSelfEmploymentModel(
    testBusinessStartDateModel, testBusinessNameModel, testValidBusinessTradeNameModel, testBusinessAddressModel)


  "GET /report-quarterly/income-and-expenses/sign-up/self-employments/client/details/business-list" when {
    "the Connector is empty" should {
      "return the page with no prepopulated fields" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetAllSelfEmployedDetails(NO_CONTENT)

        When("GET /client/details/business-list is called")
        val res = getClientCheckYourAnswers

        Then("should return an OK with the BusinessNamePage")
        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(DateOfCommencementUri)
        )
      }
    }
    "Connector returns a valid json" should {
      "show check your answers page" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetAllSelfEmployedDetails(OK, Json.toJson(testGetAllSelfEmploymentModel))

        When("GET /client/details/business-list is called")
        val res = getClientCheckYourAnswers

        Then("should return an OK with the CheckYourAnswers page")
        res must have(
          httpStatus(OK),
          pageTitle("Check your answers" + titleSuffix)
        )
      }
    }
  }

  "POST /report-quarterly/income-and-expenses/sign-up/self-employments/client/details/business-list" when {
    "return SEE_OTHER when clicking on Confirm and Signup" in {
      Given("I setup the Wiremock stubs")
      stubAuthSuccess()

      When("POST /client/details/business-list is called")
      val result = submitClientCheckYourAnswers()

      Then("should return SEE_OTHER with InitialiseURI")

      result must have(
        httpStatus(SEE_OTHER),
        redirectURI(ClientBusinessListCYAUri)

      )
    }
  }
}

