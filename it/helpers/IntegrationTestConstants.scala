
package helpers

import java.time.LocalDate

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._


object IntegrationTestConstants {

  lazy val id: String = "AA111111A"

  val baseURI = "/report-quarterly/income-and-expenses/sign-up/self-employments"
  val BusinessStartDateUri = s"$baseURI/details/business-start-date"
  val BusinessNameUri = s"$baseURI/details/business-name"
  val BusinessTradeNameUri = s"$baseURI/details/business-trade"
  val BusinessAccountingMethodUri = s"$baseURI/details/business-accounting-method"
  val BusinessListCYAUri = s"$baseURI/details/business-list"

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

  val testValidBusinessTradeName: String = "Plumbing"
  val testInvalidBusinessTradeName: String = "!()+{}?^~"
  val testValidBusinessTradeNameModel: BusinessTradeNameModel = BusinessTradeNameModel(testValidBusinessTradeName)
  val testInvalidBusinessTradeNameModel: BusinessTradeNameModel = BusinessTradeNameModel(testInvalidBusinessTradeName)

  val testAccountingMethodModel: AccountingMethodModel = AccountingMethodModel(Cash)
}
