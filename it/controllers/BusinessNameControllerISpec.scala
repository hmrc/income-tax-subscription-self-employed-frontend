
package controllers

import connectors.stubs.IncomeTaxSubscriptionConnectorStub._
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.AuthStub.stubAuthSuccess
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.businessesKey
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{BusinessNameModel, SelfEmploymentData}

class BusinessNameControllerISpec extends ComponentSpecBase {

  val businessId: String = "testId"

  val testBusinessName: String = "businessName"
  val testBusinessNameModel: BusinessNameModel = BusinessNameModel(testBusinessName)
  val testEmptyBusinessNameModel: BusinessNameModel = BusinessNameModel("")
  val titleSuffix = " - Report your income and expenses quarterly - GOV.UK"

  val testBusinesses: Seq[SelfEmploymentData] = Seq(SelfEmploymentData(businessId, businessName = Some(testBusinessNameModel)))

  "GET /report-quarterly/income-and-expenses/sign-up/self-employments/details/business-name" when {
    "the Connector is empty" should {
      "return the page with no prepopulated fields" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSelfEmployments(businessesKey)(NO_CONTENT)

        When("GET /details/business-name is called")
        val res = getBusinessName(businessId)

        Then("should return an OK with the BusinessNamePage")
        res must have(
          httpStatus(OK),
          pageTitle("What is the name of your business?" + titleSuffix)
        )
      }
    }
    "Connector returns a previously filled in Business Name" should {
      "show the current business name page with the name value entered" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSelfEmployments(businessesKey)(OK, Json.toJson(testBusinesses))

        When("GET /details/business-name is called")
        val res = getBusinessName(businessId)

        Then("should return an OK with the BusinessNamePage")
        res must have(
          httpStatus(OK),
          pageTitle("What is the name of your business?" + titleSuffix),
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
        stubGetSelfEmployments(businessesKey)(NO_CONTENT)
        stubSaveSelfEmployments(businessesKey, Json.toJson(testBusinesses))(OK)

        When("Post /details/business-name is called")
        val res = submitBusinessName(businessId, inEditMode = false, Some(testBusinessNameModel))

        Then("should return a SEE_OTHER")
        res must have(
          httpStatus(SEE_OTHER)
        )
      }

      "the form data is invalid" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSelfEmployments(businessesKey)(NO_CONTENT)

        When("POST /details/business-name")
        val res = submitBusinessName(businessId, inEditMode = false, Some(testEmptyBusinessNameModel))

        Then("Should return a BAD_REQUEST and THE FORM With errors")
        res must have(
          httpStatus(BAD_REQUEST),
          pageTitle("Error: What is the name of your business?" + titleSuffix)
        )
      }
    }
    "in edit mode" when {
      "the form data is valid and is stored successfully and redirected to Check Your Answers" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSelfEmployments(businessesKey)(OK, Json.toJson(testBusinesses.map(_.copy(businessName = Some(BusinessNameModel("test name"))))))
        stubSaveSelfEmployments(businessesKey, Json.toJson(testBusinesses))(OK)

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

