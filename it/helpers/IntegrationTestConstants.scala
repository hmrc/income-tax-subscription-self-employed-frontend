
package helpers

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._


object IntegrationTestConstants {

  lazy val id: String = "AA111111A"
  val incomeTaxSubscriptionFrontendBaseUrl = "/report-quarterly/income-and-expenses/sign-up"
  val baseURI = "/report-quarterly/income-and-expenses/sign-up/self-employments"
  val BusinessStartDateUri = s"$baseURI/details/business-start-date"
  val DateOfCommencementUri = s"$baseURI/client/details/business-start-date"
  val ClientBusinessNameUri = s"$baseURI/client/details/business-name"
  val ClientBusinessTradeNameUri = s"$baseURI/client/details/business-trade"
  val BusinessNameUri = s"$baseURI/details/business-name"
  val BusinessTradeNameUri = s"$baseURI/details/business-trade"
  val BusinessAccountingMethodUri = s"$baseURI/details/business-accounting-method"
  val BusinessListCYAUri = s"$baseURI/details/business-list"
  val InitialiseUri = s"$baseURI/details"
  def businessAddressInitialiseUri(itsaId: String): String = s"$baseURI/address-lookup-initialise/$itsaId"
  def businessAddressLookupRedirectUri(itsaId: String): String = s"$baseURI/details/address-lookup/$itsaId"

  object Auth {
    def idsResponseJson(internalId: String, externalId: String): JsValue = Json.parse(
      s"""{
           "internalId":"$internalId",
           "externalId":"$externalId"
        }""")
  }

  val testAccountingMethodModel: AccountingMethodModel = AccountingMethodModel(Cash)

  def testAddressLookupConfig(continueUrl: String): String =
    s"""{
       |  "version": 2,
       |  "options": {
       |    "continueUrl": "$continueUrl",
       |    "showBackButtons": true,
       |    "includeHMRCBranding": true,
       |    "ukMode": true,
       |    "selectPageConfig": {
       |      "proposalListLimit": 50,
       |      "showSearchLinkAgain": true
       |    },
       |    "confirmPageConfig": {
       |      "showChangeLink": true,
       |      "showSubHeadingAndInfo": true,
       |      "showSearchAgainLink": false,
       |      "showConfirmChangeText": true
       |    },
       |    "timeoutConfig": {
       |      "timeoutAmount": 900,
       |      "timeoutUrl": "http://tax.service.gov.uk/income-tax-subscription-frontend/session-timeout"
       |    }
       |},
       |    "labels": {
       |      "en": {
       |        "selectPageLabels": {
       |          "title": "Select business address",
       |          "heading": "Select business address"
       |        },
       |        "lookupPageLabels": {
       |          "title": "What is your business address?",
       |          "heading": "What is your business address?"
       |        },
       |        "editPageLabels": {
       |          "title": "Enter business address",
       |          "heading": "Enter business address",
       |          "postcodeLabel":"Postcode"
       |        },
       |        "confirmPageLabels": {
       |          "title": "Confirm business address",
       |          "heading": "Confirm business address"
       |        }
       |      },
       |      "cy": {
       |        "selectPageLabels": {
       |          "title": "Dewiswch gyfeiriad busnes",
       |          "heading": "Dewiswch gyfeiriad busnes"
       |        },
       |        "lookupPageLabels": {
       |          "title": "Beth yw cyfeiriad eich busnes?",
       |          "heading": "Beth yw cyfeiriad eich busnes?"
       |        },
       |        "editPageLabels": {
       |          "title": "Rhowch gyfeiriad busnes",
       |          "heading": "Rhowch gyfeiriad busnes",
       |          "postcodeLabel":"Cod post y DU"
       |        },
       |        "confirmPageLabels": {
       |          "title": "Cadarnhau cyfeiriad busnes",
       |          "heading": "Cadarnhau cyfeiriad busnes"
       |        }
       |      }
       |    }
       |  }""".stripMargin
}
