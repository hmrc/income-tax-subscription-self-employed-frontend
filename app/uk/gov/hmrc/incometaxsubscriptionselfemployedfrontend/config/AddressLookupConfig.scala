/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config

import play.api.i18n.{Lang, MessagesApi}
import play.api.libs.json.{JsObject, Json}

import javax.inject.Inject

class AddressLookupConfig @Inject()(messagesApi: MessagesApi) {

  //scalastyle:off
  def config(continueUrl: String): JsObject = {
    val en = Lang("EN")
    val cy = Lang("CY")

    Json.obj(
      "version" -> 2,
      "options" -> Json.obj(
        "continueUrl" -> continueUrl,
        "showBackButtons" -> true,
        "includeHMRCBranding" -> true,
        "ukMode" -> true,
        "selectPageConfig" -> Json.obj(
          "proposalListLimit" -> 50,
          "showSearchLinkAgain" -> true
        ),
        "confirmPageConfig" -> Json.obj(
          "showChangeLink" -> false,
          "showSubHeadingAndInfo" -> false,
          "showSearchAgainLink" -> false,
          "showConfirmChangeText" -> true
        ),
        "timeoutConfig" -> Json.obj(
          "timeoutAmount" -> 900,
          "timeoutUrl" -> "http://tax.service.gov.uk/report-quarterly/income-and-expenses/sign-up/session-timeout"
        )
      ),
      "labels" -> Json.obj(
        "en" -> Json.obj(
          "selectPageLabels" -> Json.obj(
            "title" -> messagesApi("addressLookup.selectPage.title")(en),
            "heading" -> messagesApi("addressLookup.selectPage.heading")(en)
          ),
          "lookupPageLabels" -> Json.obj(
            "title" -> messagesApi("addressLookup.lookupPage.title")(en),
            "heading" -> messagesApi("addressLookup.lookupPage.heading")(en)
          ),
          "editPageLabels" -> Json.obj(
            "title" -> messagesApi("addressLookup.editPage.title")(en),
            "heading" -> messagesApi("addressLookup.editPage.heading")(en),
            "postcodeLabel" -> messagesApi("addressLookup.editPage.postcodeLabel")(en)
          ),
          "confirmPageLabels" -> Json.obj(
            "title" -> messagesApi("addressLookup.confirmPage.title")(en),
            "heading" -> messagesApi("addressLookup.confirmPage.heading")(en)
          )
        ),
        "cy" -> Json.obj(
          "selectPageLabels" -> Json.obj(
            "title" -> messagesApi("addressLookup.selectPage.title")(cy),
            "heading" -> messagesApi("addressLookup.selectPage.heading")(cy)
          ),
          "lookupPageLabels" -> Json.obj(
            "title" -> messagesApi("addressLookup.lookupPage.title")(cy),
            "heading" -> messagesApi("addressLookup.lookupPage.heading")(cy)
          ),
          "editPageLabels" -> Json.obj(
            "title" -> messagesApi("addressLookup.editPage.title")(cy),
            "heading" -> messagesApi("addressLookup.editPage.heading")(cy),
            "postcodeLabel" -> messagesApi("addressLookup.editPage.postcodeLabel")(cy)
          ),
          "confirmPageLabels" -> Json.obj(
            "title" -> messagesApi("addressLookup.confirmPage.title")(cy),
            "heading" -> messagesApi("addressLookup.confirmPage.heading")(cy)
          )
        )
      )
    )
  }

  //scalastyle:off
  def agentConfig(continueUrl: String): JsObject = {
    val en = Lang("EN")
    val cy = Lang("CY")

    Json.obj(
      "version" -> 2,
      "options" -> Json.obj(
        "continueUrl" -> continueUrl,
        "showBackButtons" -> true,
        "includeHMRCBranding" -> true,
        "ukMode" -> true,
        "selectPageConfig" -> Json.obj(
          "proposalListLimit" -> 50,
          "showSearchLinkAgain" -> true
        ),
        "confirmPageConfig" -> Json.obj(
          "showChangeLink" -> true,
          "showSubHeadingAndInfo" -> true,
          "showSearchAgainLink" -> false,
          "showConfirmChangeText" -> true
        ),
        "timeoutConfig" -> Json.obj(
          "timeoutAmount" -> 900,
          "timeoutUrl" -> "http://tax.service.gov.uk/report-quarterly/income-and-expenses/sign-up/session-timeout"
        )
      ),
      "labels" -> Json.obj(
        "en" -> Json.obj(
          "selectPageLabels" -> Json.obj(
            "title" -> messagesApi("agent.addressLookup.selectPage.title")(en),
            "heading" -> messagesApi("agent.addressLookup.selectPage.heading")(en)
          ),
          "lookupPageLabels" -> Json.obj(
            "title" -> messagesApi("agent.addressLookup.lookupPage.title")(en),
            "heading" -> messagesApi("agent.addressLookup.lookupPage.heading")(en)
          ),
          "editPageLabels" -> Json.obj(
            "title" -> messagesApi("agent.addressLookup.editPage.title")(en),
            "heading" -> messagesApi("agent.addressLookup.editPage.heading")(en),
            "postcodeLabel" -> messagesApi("agent.addressLookup.editPage.postcodeLabel")(en)
          ),
          "confirmPageLabels" -> Json.obj(
            "title" -> messagesApi("agent.addressLookup.confirmPage.title")(en),
            "heading" -> messagesApi("agent.addressLookup.confirmPage.heading")(en)
          )
        ),
        "cy" -> Json.obj(
          "selectPageLabels" -> Json.obj(
            "title" -> messagesApi("agent.addressLookup.selectPage.title")(cy),
            "heading" -> messagesApi("agent.addressLookup.selectPage.heading")(cy)
          ),
          "lookupPageLabels" -> Json.obj(
            "title" -> messagesApi("agent.addressLookup.lookupPage.title")(cy),
            "heading" -> messagesApi("agent.addressLookup.lookupPage.heading")(cy)
          ),
          "editPageLabels" -> Json.obj(
            "title" -> messagesApi("agent.addressLookup.editPage.title")(cy),
            "heading" -> messagesApi("agent.addressLookup.editPage.heading")(cy),
            "postcodeLabel" -> messagesApi("agent.addressLookup.editPage.postcodeLabel")(cy)
          ),
          "confirmPageLabels" -> Json.obj(
            "title" -> messagesApi("agent.addressLookup.confirmPage.title")(cy),
            "heading" -> messagesApi("agent.addressLookup.confirmPage.heading")(cy)
          )
        )
      )
    )
  }
}
