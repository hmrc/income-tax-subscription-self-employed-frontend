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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.AccountingPeriodUtil


object IntegrationTestConstants {

  val id: String = "test-id"
  val testNino = "test-nino"
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
  val BusinessListCYAUri = s"$baseURI/details/business-list"
  val BusinessAddressConfirmationUri = s"$baseURI/details/confirm-business-address"
  val BusinessCYAUri = s"$baseURI/details/business-check-your-answers"
  val ClientBusinessCYAUri = s"$baseURI/client/details/business-check-your-answers"
  val taskListURI = s"$incomeTaxSubscriptionFrontendBaseUrl/business/task-list"
  val yourIncomeSources = s"$incomeTaxSubscriptionFrontendBaseUrl/details/your-income-source"
  val clientTaskListURI = s"$incomeTaxSubscriptionFrontendBaseUrl/client/business/task-list"
  val clientYourIncomeSources = s"$incomeTaxSubscriptionFrontendBaseUrl/client/your-income-source"
  val ClientBusinessListCYAUri = s"$baseURI/client/details/business-list"
  val individualGlobalCYAUri = s"$incomeTaxSubscriptionFrontendBaseUrl/final-check-your-answers"
  val globalCYAUri = s"$incomeTaxSubscriptionFrontendBaseUrl/client/final-check-your-answers"
  val InitialiseUri = s"$baseURI/details"
  val ClientInitialiseUri = s"$baseURI/client/details"
  val ggSignInURI = s"/bas-gateway/sign-in"
  val ggSignOutURI = s"/bas-gateway/sign-out-without-state"

  def businessAddressInitialiseUri(itsaId: String): String = s"$baseURI/address-lookup-initialise/$itsaId"

  def ukAddressConfirmation(id: String): String = s"$baseURI/uk-foreign-business?id=$id"

  def clientUkAddressConfirmation(id: String): String = s"$baseURI/client/uk-foreign-business?id=$id"

  def businessAddressCheckUri(itsaId: String): String = s"$baseURI/address-lookup-check/$itsaId"

  def businessAddressLookupRedirectUri(itsaId: String): String = s"$baseURI/details/address-lookup/$itsaId"

  object Auth {
    def idsResponseJson(internalId: String, externalId: String): JsValue = Json.parse(
      s"""{
           "internalId":"$internalId",
           "externalId":"$externalId"
        }""")
  }

  val referrerPath = "dummyPath"
  val referrerQueryString = "dummyQueryString"

  val address: Address = Address(
    lines = Seq("1 Long Road", "Lonely Town"),
    postcode = Some("ZZ1 1ZZ"),
    country = Country.UK
  )

  val soleTraderBusiness: SoleTraderBusiness = SoleTraderBusiness(
    id = id,
    startDate = Some(DateModel.dateConvert(AccountingPeriodUtil.getStartDateLimit)),
    startDateBeforeLimit = Some(false),
    name = Some("test name"),
    trade = Some("test trade"),
    address = Some(address)
  )

  val soleTraderBusinesses: SoleTraderBusinesses = SoleTraderBusinesses(
    businesses = Seq(
      soleTraderBusiness
    )
  )

  //scalastyle:off
  def testAddressLookupConfig(isUk: Boolean, continueUrl: String, referrerUrlMaybe: Option[String] = None): String = {
    val referrerUrl = referrerUrlMaybe.getOrElse(referrerPath + "%3F" + referrerQueryString)
    val accessibilityFooterUrl = s"http://localhost:12346/accessibility-statement/income-tax-sign-up?referrerUrl=$referrerUrl"
    s"""{
       |  "version": 2,
       |  "options": {
       |    "manualAddressEntryConfig": {
       |      "line1MaxLength": 35,
       |      "line2MaxLength": 35,
       |      "line3MaxLength": 35,
       |      "townMaxLength": 35,
       |      "showOrganisationName": false,
       |      "mandatoryFields": {
       |        "addressLine1": true,
       |        "addressLine2": false,
       |        "addressLine3": false,
       |        "town": true,
       |        "postcode": false
       |      }
       |    },
       |    "continueUrl": "$continueUrl",
       |    "showBackButtons": true,
       |    "includeHMRCBranding": true,
       |    "serviceHref": "https://www.gov.uk/guidance/sign-up-your-business-for-making-tax-digital-for-income-tax",
       |    "ukMode": $isUk,
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
       |    "accessibilityFooterUrl": "$accessibilityFooterUrl"
       |  },
       |  "labels": {
       |    "en": {
       |      "appLevelLabels": {
       |        "navTitle": "Sign up for Making Tax Digital for Income Tax"
       |      },
       |      "selectPageLabels": {
       |        "title": "Select business address",
       |        "heading": "Select business address"
       |      },
       |      "lookupPageLabels": {
       |        "title": "Find your business address",
       |        "heading": "Find your business address",
       |        "filterLabel": "Property name or number (optional)",
       |        "postcodeLabel": "Postcode"
       |      },
       |      "editPageLabels": {
       |        "title": "Enter your business address",
       |        "heading": "Enter your business address",
       |        "line1Label": "Address line 1",
       |        "line2Label": "Address line 2",
       |        "line3Label": "Address line 3",
       |        "townLabel": "Town or city",
       |        "postcodeLabel": "Postcode or zipcode",
       |        "countryLabel": "Country or territory"
       |      },
       |      "confirmPageLabels": {
       |        "title": "Confirm your business address",
       |        "heading": "Confirm your business address",
       |        "infoSubheading": "Your selected address:",
       |        "infoMessage": "",
       |        "confirmChangeText": "By confirming, you agree that the information you have given is complete and correct.",
       |        "submitLabel": "Confirm and continue"
       |      },
       |      "countryPickerLabels": {
       |        "title": "Select the country or territory for your business address",
       |        "heading": "Select the country or territory for your business address",
       |        "countryLabel": "Select country or territory"
       |      }
       |    },
       |    "cy": {
       |      "appLevelLabels": {
       |        "navTitle": "Cofrestru ar gyfer y cynllun Troi Treth yn Ddigidol ar gyfer Treth Incwm"
       |      },
       |      "selectPageLabels": {
       |        "title": "Dewiswch gyfeiriad busnes",
       |        "heading": "Dewiswch gyfeiriad busnes"
       |      },
       |      "lookupPageLabels": {
       |        "title": "Dewch o hyd i gyfeiriad eich busnes",
       |        "heading": "Dewch o hyd i gyfeiriad eich busnes",
       |        "filterLabel": "Enw neu rif yr eiddo (dewisol)",
       |        "postcodeLabel": "Cod post"
       |      },
       |      "editPageLabels": {
       |        "title": "Rhowch gyfeiriad busnes",
       |        "heading": "Rhowch gyfeiriad busnes",
       |        "line1Label": "Cyfeiriad – llinell 1",
       |        "line2Label": "Cyfeiriad – llinell 2",
       |        "line3Label": "Cyfeiriad – llinell 3",
       |        "townLabel": "Tref neu ddinas",
       |        "postcodeLabel": "Cod post neu god zip",
       |        "countryLabel": "Gwlad neu diriogaeth"
       |      },
       |      "confirmPageLabels": {
       |        "title": "Cadarnhau cyfeiriad eich busnes",
       |        "heading": "Cadarnhau cyfeiriad eich busnes",
       |        "infoSubheading": "Eich cyfeiriad",
       |        "infoMessage": "",
       |        "confirmChangeText": "Drwy gadarnhau, rydych yn cytuno bod yr wybodaeth a roddwyd gennych yn gyflawn ac yn gywir.",
       |        "submitLabel": "Cadarnhau a pharhau"
       |      },
       |      "countryPickerLabels": {
       |        "title": "Dewiswch y wlad neu’r diriogaeth ar gyfer cyfeiriad eich busnes",
       |        "heading": "Dewiswch y wlad neu’r diriogaeth ar gyfer cyfeiriad eich busnes",
       |        "countryLabel": "Dewiswch gwlad neu diriogaeth"
       |      }
       |    }
       |  }
       |}""".stripMargin
  }

  //scalastyle:off
  def testAddressLookupConfigClient(isUk: Boolean, continueUrl: String, referrerUrlMaybe: Option[String] = None): String = {
    val referrerUrl = referrerUrlMaybe.getOrElse(referrerPath + "%3F" + referrerQueryString)
    val accessibilityFooterUrl = s"http://localhost:12346/accessibility-statement/income-tax-sign-up?referrerUrl=$referrerUrl"
    s"""{
       |  "version": 2,
       |  "options": {
       |    "manualAddressEntryConfig": {
       |      "line1MaxLength": 35,
       |      "line2MaxLength": 35,
       |      "line3MaxLength": 35,
       |      "townMaxLength": 35,
       |      "showOrganisationName": false,
       |      "mandatoryFields": {
       |        "addressLine1": true,
       |        "addressLine2": false,
       |        "addressLine3": false,
       |        "town": true,
       |        "postcode": false
       |      }
       |    },
       |    "continueUrl": "$continueUrl",
       |    "showBackButtons": true,
       |    "includeHMRCBranding": true,
       |    "serviceHref": "https://www.gov.uk/guidance/sign-up-your-client-for-making-tax-digital-for-income-tax",
       |    "ukMode": $isUk,
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
       |    "accessibilityFooterUrl": "$accessibilityFooterUrl"
       |  },
       |  "labels": {
       |    "en": {
       |      "appLevelLabels": {
       |        "navTitle": "Sign up your clients for Making Tax Digital for Income Tax"
       |      },
       |      "selectPageLabels": {
       |        "title": "Select client’s business address",
       |        "heading": "Select client’s business address"
       |      },
       |      "lookupPageLabels": {
       |        "title": "Find your client’s business address",
       |        "heading": "Find your client’s business address",
       |        "filterLabel": "Property name or number (optional)",
       |        "postcodeLabel": "Postcode"
       |      },
       |      "editPageLabels": {
       |        "title": "Enter your client’s business address",
       |        "heading": "Enter your client’s business address",
       |        "line1Label": "Address line 1",
       |        "line2Label": "Address line 2",
       |        "line3Label": "Address line 3",
       |        "townLabel": "Town or city",
       |        "postcodeLabel": "Postcode or zipcode",
       |        "countryLabel": "Country or territory"
       |      },
       |      "confirmPageLabels": {
       |        "title": "Confirm your client’s business address",
       |        "heading": "Confirm your client’s business address",
       |        "infoSubheading": "Your selected address:",
       |        "infoMessage": "",
       |        "confirmChangeText": "By confirming, you agree that the information you have given is complete and correct.",
       |        "submitLabel": "Confirm and continue"
       |      },
       |      "countryPickerLabels": {
       |        "title": "Select the country or territory for your client’s business address",
       |        "heading": "Select the country or territory for your client’s business address",
       |        "countryLabel": "Select country or territory"
       |      }
       |    },
       |    "cy": {
       |      "appLevelLabels": {
       |        "navTitle": "Cofrestrwch eich cleientiaid ar gyfer y cynllun Troi Treth yn Ddigidol ar gyfer Treth Incwm"
       |      },
       |      "selectPageLabels": {
       |        "title": "dewis cyfeiriad busnes y cleient",
       |        "heading": "dewis cyfeiriad busnes y cleient"
       |      },
       |      "lookupPageLabels": {
       |        "title": "Dewch o hyd i gyfeiriad busnes eich cleient",
       |        "heading": "Dewch o hyd i gyfeiriad busnes eich cleient",
       |        "filterLabel": "Enw neu rif yr eiddo (dewisol)",
       |        "postcodeLabel": "Cod post"
       |      },
       |      "editPageLabels": {
       |        "title": "Nodwch gyfeiriad busnes eich cleien",
       |        "heading": "Nodwch gyfeiriad busnes eich cleien",
       |        "line1Label": "Cyfeiriad – llinell 1",
       |        "line2Label": "Cyfeiriad – llinell 2",
       |        "line3Label": "Cyfeiriad – llinell 3",
       |        "townLabel": "Tref neu ddinas",
       |        "postcodeLabel": "Cod post neu god zip",
       |        "countryLabel": "Gwlad neu diriogaeth"
       |      },
       |      "confirmPageLabels": {
       |        "title": "Cadarnhau cyfeiriad busnes eich cleient",
       |        "heading": "Cadarnhau cyfeiriad busnes eich cleient",
       |        "infoSubheading": "Eich cyfeiriad",
       |        "infoMessage": "",
       |        "confirmChangeText": "Drwy gadarnhau, rydych yn cytuno bod yr wybodaeth a roddwyd gennych yn gyflawn ac yn gywir.",
       |        "submitLabel": "Cadarnhau a pharhau"
       |      },
       |      "countryPickerLabels": {
       |        "title": "Dewiswch y wlad neu’r diriogaeth ar gyfer cyfeiriad busnes eich cleient",
       |        "heading": "Dewiswch y wlad neu’r diriogaeth ar gyfer cyfeiriad busnes eich cleient",
       |        "countryLabel": "Dewiswch gwlad neu diriogaeth"
       |      }
       |    }
       |  }
       |}""".stripMargin
  }
}
