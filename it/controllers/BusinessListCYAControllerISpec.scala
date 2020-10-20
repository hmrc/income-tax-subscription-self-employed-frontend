
package controllers

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

  val businessId: String = "testId"

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

  val testBusinesses: Seq[SelfEmploymentData] = Seq(SelfEmploymentData(businessId,
    businessName = Some(testBusinessNameModel), businessStartDate = Some(testValidBusinessStartDateModel),
    businessTradeName = Some(testValidBusinessTradeNameModel),
    businessAddress = Some(testBusinessAddressModel)
  ))

  "GET /report-quarterly/income-and-expenses/sign-up/self-employments/details/business-list" when {
    "the Connector is empty" should {
      "return the page with no prepopulated fields" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSelfEmployments(businessesKey)(NO_CONTENT)

        When("GET /details/business-list is called")
        val res = getCheckYourAnswers

        Then("should return an OK with the BusinessNamePage")
        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(InitialiseUri)
        )
      }
    }
    "Connector returns a valid json" should {
      "show check your answers page" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSelfEmployments(businessesKey)(OK, Json.toJson(testBusinesses))

        When("GET /details/business-list is called")
        val res = getCheckYourAnswers

        Then("should return an OK with the CheckYourAnswers page")
        res must have(
          httpStatus(OK),
          pageTitle("Check your answers" + titleSuffix)
        )
      }
    }
  }

  "POST /report-quarterly/income-and-expenses/sign-up/self-employments/details/business-list" when {
    "return SEE_OTHER when selecting yes" in {
      Given("I setup the Wiremock stubs")
      stubAuthSuccess()
      stubGetSelfEmployments(businessesKey)(OK, Json.toJson(testBusinesses))

      When("POST /details/business-list is called")
      val result = submitCheckYourAnswers(Some(AddAnotherBusinessModel(Yes)),1, 5)

      Then("should return SEE_OTHER with InitialiseURI")

      result must have(
        httpStatus(SEE_OTHER),
        redirectURI(InitialiseUri)

      )
    }

    "return SEE_OTHER when selecting No" in {
      Given("I setup the Wiremock stubs")
      stubAuthSuccess()
      stubGetSelfEmployments(businessesKey)(OK, Json.toJson(testBusinesses))

      When("POST /details/business-list is called")
      val result = submitCheckYourAnswers(Some(AddAnotherBusinessModel(No)),1, 5)

      Then("should return SEE_OTHER with BusinessAccountingMethod page")

      result must have(
        httpStatus(SEE_OTHER),
        redirectURI(BusinessAccountingMethodUri)
      )

    }

    "return BAD_REQUEST when no Answer is given" in {
      Given("I setup the Wiremock stubs")
      stubAuthSuccess()
      stubGetSelfEmployments(businessesKey)(OK, Json.toJson(testBusinesses))

      When("POST /details/business-list is called")
      val result = submitCheckYourAnswers(None,1, 5)
      val doc: Document = Jsoup.parse(result.body)

      Then("should return an BAD_REQUEST")

      result must have(
        httpStatus(BAD_REQUEST)
      )

      val errorMessage = doc.select("span[class=error-notification]")
      errorMessage.text() mustBe "Select yes if you want to add another sole trader business"
    }
  }
}

