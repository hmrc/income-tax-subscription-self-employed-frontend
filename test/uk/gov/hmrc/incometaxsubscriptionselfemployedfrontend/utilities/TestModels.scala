/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities

import java.time.LocalDate

import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._

object TestModels {

  val testValidStartDate: DateModel = DateModel.dateConvert(LocalDate.now.minusYears(3))
  val testBusinessStartDateModel: BusinessStartDate = BusinessStartDate(testValidStartDate)
  val testBusinessNameModel: BusinessNameModel = BusinessNameModel("Business")
  val testValidBusinessTradeName: String = "Plumbing"
  val testInvalidBusinessTradeName: String = "!()+{}?^~"
  val testValidBusinessTradeNameModel: BusinessTradeNameModel = BusinessTradeNameModel(testValidBusinessTradeName)
  val testInvalidBusinessTradeNameModel: BusinessTradeNameModel = BusinessTradeNameModel(testInvalidBusinessTradeName)
  val testAccountingMethodModel: AccountingMethodModel = AccountingMethodModel(Cash)

  val mockBusinessNameModel: BusinessNameModel = BusinessNameModel("ITSA me, Mario")
  val mockAddAnotherBusinessModelWithYes: AddAnotherBusinessModel = AddAnotherBusinessModel(Yes)
  val mockAddAnotherBusinessModelWithNo: AddAnotherBusinessModel = AddAnotherBusinessModel(No)

  val testValidBusinessAddressModel: BusinessAddressModel = BusinessAddressModel(auditRef = "1",
    Address(lines = Seq("line1", "line2", "line3"), postcode = "TF3 4NT"))

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

  def testAddressLookupConfigClient(continueUrl: String): String =
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
       |          "title": "Select client‘s business address",
       |          "heading": "Select client‘s business address"
       |        },
       |        "lookupPageLabels": {
       |          "title": "What is your client‘s business address?",
       |          "heading": "What is your client‘s business address?"
       |        },
       |        "editPageLabels": {
       |          "title": "Enter client‘s business address",
       |          "heading": "Enter client‘s business address",
       |          "postcodeLabel":"Postcode"
       |        },
       |        "confirmPageLabels": {
       |          "title": "Confirm client‘s business address",
       |          "heading": "Confirm client‘s business address"
       |        }
       |      },
       |      "cy": {
       |        "selectPageLabels": {
       |          "title": "dewis cyfeiriad busnes y cleient",
       |          "heading": "dewis cyfeiriad busnes y cleient"
       |        },
       |        "lookupPageLabels": {
       |          "title": "Beth yw cyfeiriad busnes eich cleient?",
       |          "heading": "Beth yw cyfeiriad busnes eich cleient?"
       |        },
       |        "editPageLabels": {
       |          "title": "Rhowch gyfeiriad busnes y cleient",
       |          "heading": "Rhowch gyfeiriad busnes y cleient",
       |          "postcodeLabel":"Cod post y DU"
       |        },
       |        "confirmPageLabels": {
       |          "title": "Cadarnhau cyfeiriad busnes y cleient",
       |          "heading": "Cadarnhau cyfeiriad busnes y cleient"
       |        }
       |      }
       |    }
       |  }""".stripMargin
}
