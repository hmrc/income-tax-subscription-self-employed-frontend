/*
 * Copyright 2023 HM Revenue & Customs
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
import play.api.mvc.RequestHeader
import uk.gov.hmrc.hmrcfrontend.config.AccessibilityStatementConfig

import javax.inject.{Inject, Singleton}

@Singleton
class AddressLookupConfig @Inject()(appConfig: AppConfig, messagesApi: MessagesApi, accessibilityStatementConfig: AccessibilityStatementConfig){

  //scalastyle:off
  def config(continueUrl: String)(implicit request: RequestHeader): JsObject = {
    val en = Lang("EN")
    val cy = Lang("CY")

    Json.obj(
      "version" -> 2,
      "options" -> Json.obj(
        "continueUrl" -> continueUrl,
        "showBackButtons" -> true,
        "includeHMRCBranding" -> true,
        "serviceHref" -> Some(appConfig.govukGuidanceITSASignUpIndivLink),
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
          "timeoutUrl" -> s"${appConfig.incomeTaxSubscriptionFrontendBaseUrl}/session-timeout"
        ),
        "accessibilityFooterUrl" -> accessibilityStatementConfig.url
      ),
      "labels" -> Json.obj(
        "en" -> Json.obj(
          "appLevelLabels" -> Json.obj(
            "navTitle" -> messagesApi("base.service-name")(en)
          ),
          "selectPageLabels" -> Json.obj(
            "title" -> messagesApi("address-lookup.select-page.title")(en),
            "heading" -> messagesApi("address-lookup.select-page.heading")(en)
          ),
          "lookupPageLabels" -> Json.obj(
            "title" -> messagesApi("address-lookup.lookup-page.title")(en),
            "heading" -> messagesApi("address-lookup.lookup-page.heading")(en)
          ),
          "editPageLabels" -> Json.obj(
            "title" -> messagesApi("address-lookup.edit-page.title")(en),
            "heading" -> messagesApi("address-lookup.edit-page.heading")(en),
            "postcodeLabel" -> messagesApi("address-lookup.edit-page.postcode-label")(en)
          ),
          "confirmPageLabels" -> Json.obj(
            "title" -> messagesApi("address-lookup.confirm-page.title")(en),
            "heading" -> messagesApi("address-lookup.confirm-page.heading")(en)
          )
        ),
        "cy" -> Json.obj(
          "appLevelLabels" -> Json.obj(
            "navTitle" -> messagesApi("base.service-name")(cy)
          ),
          "selectPageLabels" -> Json.obj(
            "title" -> messagesApi("address-lookup.select-page.title")(cy),
            "heading" -> messagesApi("address-lookup.select-page.heading")(cy)
          ),
          "lookupPageLabels" -> Json.obj(
            "title" -> messagesApi("address-lookup.lookup-page.title")(cy),
            "heading" -> messagesApi("address-lookup.lookup-page.heading")(cy)
          ),
          "editPageLabels" -> Json.obj(
            "title" -> messagesApi("address-lookup.edit-page.title")(cy),
            "heading" -> messagesApi("address-lookup.edit-page.heading")(cy),
            "postcodeLabel" -> messagesApi("address-lookup.edit-page.postcode-label")(cy)
          ),
          "confirmPageLabels" -> Json.obj(
            "title" -> messagesApi("address-lookup.confirm-page.title")(cy),
            "heading" -> messagesApi("address-lookup.confirm-page.heading")(cy)
          )
        )
      )
    )
  }

  //scalastyle:off
  def agentConfig(continueUrl: String)(implicit request: RequestHeader): JsObject = {
    val en = Lang("EN")
    val cy = Lang("CY")

    Json.obj(
      "version" -> 2,
      "options" -> Json.obj(
        "continueUrl" -> continueUrl,
        "showBackButtons" -> true,
        "includeHMRCBranding" -> true,
        "serviceHref" -> Some(appConfig.govukGuidanceITSASignUpAgentLink),
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
          "timeoutUrl" -> s"${appConfig.incomeTaxSubscriptionFrontendBaseUrl}/session-timeout"
        ),
        "accessibilityFooterUrl" -> accessibilityStatementConfig.url
      ),
      "labels" -> Json.obj(
        "en" -> Json.obj(
          "appLevelLabels" -> Json.obj(
            "navTitle" -> messagesApi("base.agent.service-name")(en)
          ),
          "selectPageLabels" -> Json.obj(
            "title" -> messagesApi("agent.address-lookup.select-page.title")(en),
            "heading" -> messagesApi("agent.address-lookup.select-page.heading")(en)
          ),
          "lookupPageLabels" -> Json.obj(
            "title" -> messagesApi("agent.address-lookup.lookup-page.title")(en),
            "heading" -> messagesApi("agent.address-lookup.lookup-page.heading")(en)
          ),
          "editPageLabels" -> Json.obj(
            "title" -> messagesApi("agent.address-lookup.edit-page.title")(en),
            "heading" -> messagesApi("agent.address-lookup.edit-page.heading")(en),
            "postcodeLabel" -> messagesApi("agent.address-lookup.edit-page.postcode-label")(en)
          ),
          "confirmPageLabels" -> Json.obj(
            "title" -> messagesApi("agent.address-lookup.confirm-page.title")(en),
            "heading" -> messagesApi("agent.address-lookup.confirm-page.heading")(en)
          )
        ),
        "cy" -> Json.obj(
          "appLevelLabels" -> Json.obj(
            "navTitle" -> messagesApi("base.agent.service-name")(cy)
          ),
          "selectPageLabels" -> Json.obj(
            "title" -> messagesApi("agent.address-lookup.select-page.title")(cy),
            "heading" -> messagesApi("agent.address-lookup.select-page.heading")(cy)
          ),
          "lookupPageLabels" -> Json.obj(
            "title" -> messagesApi("agent.address-lookup.lookup-page.title")(cy),
            "heading" -> messagesApi("agent.address-lookup.lookup-page.heading")(cy)
          ),
          "editPageLabels" -> Json.obj(
            "title" -> messagesApi("agent.address-lookup.edit-page.title")(cy),
            "heading" -> messagesApi("agent.address-lookup.edit-page.heading")(cy),
            "postcodeLabel" -> messagesApi("agent.address-lookup.edit-page.postcode-label")(cy)
          ),
          "confirmPageLabels" -> Json.obj(
            "title" -> messagesApi("agent.address-lookup.confirm-page.title")(cy),
            "heading" -> messagesApi("agent.address-lookup.confirm-page.heading")(cy)
          )
        )
      )
    )
  }
}
