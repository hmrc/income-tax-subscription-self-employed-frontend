/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package helpers

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._


object IntegrationTestConstants {

  val id: String = "test-id"
  val incomeTaxSubscriptionFrontendBaseUrl = "/report-quarterly/income-and-expenses/sign-up"
  val baseURI = "/report-quarterly/income-and-expenses/sign-up/self-employments"
  val BusinessStartDateUri = s"$baseURI/details/business-start-date"
  val DateOfCommencementUri = s"$baseURI/client/details/business-start-date"
  val ClientBusinessNameUri = s"$baseURI/client/details/business-name"
  val ClientBusinessTradeNameUri = s"$baseURI/client/details/business-trade"
  val ClientBusinessAddressInitialiseUri = s"$baseURI/client/address-lookup-initialise"
  val ClientBusinessAddressCheckUri = s"$baseURI/client/address-lookup-check"
  val BusinessNameUri = s"$baseURI/details/business-name"
  val BusinessTradeNameUri = s"$baseURI/details/business-trade"
  val BusinessAccountingMethodUri = s"$baseURI/details/business-accounting-method"
  val ClientBusinessAccountingMethodUri = s"$baseURI/client/details/business-accounting-method"
  val BusinessListCYAUri = s"$baseURI/details/business-list"
  val BusinessAddressConfirmationUri = s"$baseURI/details/confirm-business-address"
  val BusinessCYAUri = s"$baseURI/details/business-check-your-answers"
  val ClientBusinessCYAUri = s"$baseURI/client/details/business-check-your-answers"
  val taskListURI = s"$incomeTaxSubscriptionFrontendBaseUrl/business/task-list"
  val yourIncomeSources = s"$incomeTaxSubscriptionFrontendBaseUrl/details/your-income-source"
  val clientTaskListURI = s"$incomeTaxSubscriptionFrontendBaseUrl/client/business/task-list"
  val clientYourIncomeSources = s"$incomeTaxSubscriptionFrontendBaseUrl/client/your-income-source"
  val ClientBusinessListCYAUri = s"$baseURI/client/details/business-list"
  val globalCYAUri = s"$incomeTaxSubscriptionFrontendBaseUrl/client/final-check-your-answers"
  val InitialiseUri = s"$baseURI/details"
  val ClientInitialiseUri = s"$baseURI/client/details"
  val ggSignInURI = s"/bas-gateway/sign-in"
  val ggSignOutURI = s"/bas-gateway/sign-out-without-state"

  def businessAddressInitialiseUri(itsaId: String): String = s"$baseURI/address-lookup-initialise/$itsaId"
  def businessAddressCheckUri(itsaId: String): String = s"$baseURI/address-lookup-check/$itsaId"
  def businessAddressLookupRedirectUri(itsaId: String): String = s"$baseURI/details/address-lookup/$itsaId"

  object Auth {
    def idsResponseJson(internalId: String, externalId: String): JsValue = Json.parse(
      s"""{
           "internalId":"$internalId",
           "externalId":"$externalId"
        }""")
  }

  val testAccountingMethodModel: AccountingMethod = Cash

  val referrerPath = "dummyPath"
  val referrerQueryString = "dummyQueryString"

  val address: Address = Address(
    lines = Seq("1 Long Road", "Lonely Town"),
    postcode = Some("ZZ1 1ZZ")
  )

  val soleTraderBusinesses: SoleTraderBusinesses = SoleTraderBusinesses(
    businesses = Seq(
      SoleTraderBusiness(
        id = id,
        startDate = Some(DateModel("1", "1", "1980")),
        name = Some("test name"),
        trade = Some("test trade"),
        address = Some(address)
      )
    ),
    accountingMethod = Some(Cash)
  )

  def testAddressLookupConfig(continueUrl: String, referrerUrlMaybe: Option[String] = None): String = {
    val referrerUrl = referrerUrlMaybe.getOrElse(referrerPath + "%3F" + referrerQueryString)
    val accessibilityFooterUrl = s"http://localhost:12346/accessibility-statement/income-tax-sign-up?referrerUrl=$referrerUrl"
    s"""{
       |  "version": 2,
       |  "options": {
       |    "continueUrl": "$continueUrl",
       |    "showBackButtons": true,
       |    "includeHMRCBranding": true,
       |    "serviceHref": "https://www.gov.uk/guidance/sign-up-your-business-for-making-tax-digital-for-income-tax",
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
       |      "timeoutUrl": "http://localhost:9561/report-quarterly/income-and-expenses/sign-up/session-timeout"
       |    },
       |    "accessibilityFooterUrl":"$accessibilityFooterUrl"
       |  },
       |  "labels": {
       |    "en": {
       |      "appLevelLabels": {
       |        "navTitle": "Use software to send Income Tax updates"
       |      },
       |      "selectPageLabels": {
       |        "title": "Select business address",
       |        "heading": "Select business address"
       |      },
       |      "lookupPageLabels": {
       |        "title": "What is your business address?",
       |        "heading": "What is your business address?"
       |      },
       |      "editPageLabels": {
       |        "title": "Enter business address",
       |        "heading": "Enter business address",
       |        "postcodeLabel":"Postcode (optional)"
       |      },
       |      "confirmPageLabels": {
       |        "title": "Confirm business address",
       |        "heading": "Confirm business address"
       |      }
       |    },
       |    "cy": {
       |      "appLevelLabels": {
       |        "navTitle": "Defnyddio meddalwedd i anfon diweddariadau Treth Incwm"
       |      },
       |      "selectPageLabels": {
       |        "title": "Dewiswch gyfeiriad busnes",
       |        "heading": "Dewiswch gyfeiriad busnes"
       |      },
       |      "lookupPageLabels": {
       |        "title": "Beth yw cyfeiriad eich busnes?",
       |        "heading": "Beth yw cyfeiriad eich busnes?"
       |      },
       |      "editPageLabels": {
       |        "title": "Rhowch gyfeiriad busnes",
       |        "heading": "Rhowch gyfeiriad busnes",
       |        "postcodeLabel":"Cod post (dewisol)"
       |      },
       |      "confirmPageLabels": {
       |        "title": "Cadarnhau cyfeiriad busnes",
       |        "heading": "Cadarnhau cyfeiriad busnes"
       |      }
       |    }
       |  }
       |}""".stripMargin
  }

  def testAddressLookupConfigClient(continueUrl: String, referrerUrlMaybe: Option[String] = None): String = {
    val referrerUrl = referrerUrlMaybe.getOrElse(referrerPath + "%3F" + referrerQueryString)
    val accessibilityFooterUrl = s"http://localhost:12346/accessibility-statement/income-tax-sign-up?referrerUrl=$referrerUrl"
    s"""{
       |  "version": 2,
       |  "options": {
       |    "continueUrl": "$continueUrl",
       |    "showBackButtons": true,
       |    "includeHMRCBranding": true,
       |    "serviceHref": "https://www.gov.uk/guidance/sign-up-your-client-for-making-tax-digital-for-income-tax",
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
       |      "timeoutUrl": "http://localhost:9561/report-quarterly/income-and-expenses/sign-up/session-timeout"
       |    },
       |    "accessibilityFooterUrl":"$accessibilityFooterUrl"
       |  },
       |  "labels": {
       |    "en": {
       |      "appLevelLabels": {
       |        "navTitle": "Use software to report your client’s Income Tax"
       |      },
       |      "selectPageLabels": {
       |        "title": "Select client’s business address",
       |        "heading": "Select client’s business address"
       |      },
       |      "lookupPageLabels": {
       |        "title": "What is your client’s business address?",
       |        "heading": "What is your client’s business address?"
       |      },
       |      "editPageLabels": {
       |        "title": "Enter client’s business address",
       |        "heading": "Enter client’s business address",
       |        "postcodeLabel":"Postcode"
       |      },
       |      "confirmPageLabels": {
       |        "title": "Confirm client’s business address",
       |        "heading": "Confirm client’s business address"
       |      }
       |    },
       |    "cy": {
       |      "appLevelLabels": {
       |        "navTitle": "Defnyddiwch feddalwedd i roi gwybod am Dreth Incwm eich cleient"
       |       },
       |      "selectPageLabels": {
       |        "title": "dewis cyfeiriad busnes y cleient",
       |        "heading": "dewis cyfeiriad busnes y cleient"
       |      },
       |      "lookupPageLabels": {
       |        "title": "Beth yw cyfeiriad busnes eich cleient?",
       |        "heading": "Beth yw cyfeiriad busnes eich cleient?"
       |      },
       |      "editPageLabels": {
       |        "title": "Rhowch gyfeiriad busnes y cleient",
       |        "heading": "Rhowch gyfeiriad busnes y cleient",
       |        "postcodeLabel":"Cod post y DU"
       |      },
       |      "confirmPageLabels": {
       |        "title": "Cadarnhau cyfeiriad busnes y cleient",
       |        "heading": "Cadarnhau cyfeiriad busnes y cleient"
       |      }
       |    }
       |  }
       |}""".stripMargin
  }
}
