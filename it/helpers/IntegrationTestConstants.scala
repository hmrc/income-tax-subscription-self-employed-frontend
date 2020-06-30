
package helpers

import java.time.LocalDate

import helpers.IntegrationTestConstants.baseURI
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{DateModel, BusinessStartDate, BusinessNameModel}


object IntegrationTestConstants {

  lazy val id: String = "AA111111A"

  val baseURI = "/report-quarterly/income-and-expenses/sign-up/self-employments"
  val BusinessStartDateUri = s"$baseURI/details/business-start-date"
  val BusinessNameUri = s"$baseURI/details/business-name"

  object Auth {
    def idsResponseJson(internalId: String, externalId: String): JsValue = Json.parse(
      s"""{
           "internalId":"$internalId",
           "externalId":"$externalId"
        }""")
  }

  val testStartDate: DateModel = DateModel.dateConvert(LocalDate.now)
  val testValidStartDate: DateModel = DateModel.dateConvert(LocalDate.now.minusYears(3))
  val testBusinessStartDateModel = BusinessStartDate(testStartDate)
  val testValidBusinessStartDateModel = BusinessStartDate(testValidStartDate)

  val testBusinessName: String = "businessName"
  val testBusinessNameModel: BusinessNameModel = BusinessNameModel(testBusinessName)
  val testEmptyBusinessNameModel: BusinessNameModel = BusinessNameModel("")

}
