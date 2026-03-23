/*
 * Copyright 2024 HM Revenue & Customs
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
  def config(continueUrl: String, isAgent: Boolean, isUk: Boolean)(implicit request: RequestHeader): JsObject = {
    val en = Lang("EN")
    val cy = Lang("CY")
    
    val (prefix, serviceUrl) = if (isAgent) (
      "agent.",
      appConfig.govukGuidanceITSASignUpAgentLink
    ) else (
      "",
      appConfig.govukGuidanceITSASignUpIndivLink
    )

    Json.obj(
      "version" -> 2,
      "options" -> Json.obj(
        "manualAddressEntryConfig" -> manualAddressEntryConfig,
        "continueUrl" -> continueUrl,
        "showBackButtons" -> true,
        "includeHMRCBranding" -> true,
        "serviceHref" -> Some(serviceUrl),
        "ukMode" -> isUk,
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
        en.language -> Json.obj(
          "appLevelLabels" -> Json.obj(
            "navTitle" -> messagesApi(s"base.${prefix}service-name")(en)
          ),
          "selectPageLabels" -> Json.obj(
            "title" -> messagesApi(s"${prefix}address-lookup.select-page.title")(en),
            "heading" -> messagesApi(s"${prefix}address-lookup.select-page.heading")(en)
          ),
          "lookupPageLabels" -> Json.obj(
            "title" -> messagesApi(s"${prefix}address-lookup.lookup-page.title")(en),
            "heading" -> messagesApi(s"${prefix}address-lookup.lookup-page.heading")(en),
            "filterLabel" -> messagesApi(s"${prefix}address-lookup.lookup-page.property-label")(en),
            "postcodeLabel" -> messagesApi(s"${prefix}address-lookup.lookup-page.postcode-label")(en)
          ),
          "editPageLabels" -> Json.obj(
            "title" -> messagesApi(s"${prefix}address-lookup.edit-page.title")(en),
            "heading" -> messagesApi(s"${prefix}address-lookup.edit-page.heading")(en),
            "line1Label" -> messagesApi(s"${prefix}address-lookup.edit-page.line1-label")(en),
            "line2Label" -> messagesApi(s"${prefix}address-lookup.edit-page.line2-label")(en),
            "line3Label" -> messagesApi(s"${prefix}address-lookup.edit-page.line3-label")(en),
            "townLabel" -> messagesApi(s"${prefix}address-lookup.edit-page.town-label")(en),
            "postcodeLabel" -> messagesApi(s"${prefix}address-lookup.edit-page.postcode-label")(en),
            "countryLabel" -> messagesApi(s"${prefix}address-lookup.edit-page.country-label")(en)
          ),
          "confirmPageLabels" -> Json.obj(
            "title" -> messagesApi(s"${prefix}address-lookup.confirm-page.title")(en),
            "heading" -> messagesApi(s"${prefix}address-lookup.confirm-page.heading")(en),
            "infoSubheading" -> messagesApi(s"${prefix}address-lookup.confirm-page.address-label")(en),
            "infoMessage" -> "",
            "confirmChangeText" -> messagesApi(s"${prefix}address-lookup.confirm-page.para")(en),
            "submitLabel" -> messagesApi(s"${prefix}address-lookup.confirm-page.confirm")(en)
          ),
          "countryPickerLabels" -> Json.obj(
            "title" -> messagesApi(s"${prefix}address-lookup.country-page.title")(en),
            "heading" -> messagesApi(s"${prefix}address-lookup.country-page.heading")(en),
            "countryLabel" -> messagesApi(s"${prefix}address-lookup.country-page.country-label")(en)
          )
        ),
        cy.language -> Json.obj(
          "appLevelLabels" -> Json.obj(
            "navTitle" -> messagesApi(s"base.${prefix}service-name")(cy)
          ),
          "selectPageLabels" -> Json.obj(
            "title" -> messagesApi(s"${prefix}address-lookup.select-page.title")(cy),
            "heading" -> messagesApi(s"${prefix}address-lookup.select-page.heading")(cy)
          ),
          "lookupPageLabels" -> Json.obj(
            "title" -> messagesApi(s"${prefix}address-lookup.lookup-page.title")(cy),
            "heading" -> messagesApi(s"${prefix}address-lookup.lookup-page.heading")(cy),
            "filterLabel" -> messagesApi(s"${prefix}address-lookup.lookup-page.property-label")(cy),
            "postcodeLabel" -> messagesApi(s"${prefix}address-lookup.lookup-page.postcode-label")(cy)
          ),
          "editPageLabels" -> Json.obj(
            "title" -> messagesApi(s"${prefix}address-lookup.edit-page.title")(cy),
            "heading" -> messagesApi(s"${prefix}address-lookup.edit-page.heading")(cy),
            "line1Label" -> messagesApi(s"${prefix}address-lookup.edit-page.line1-label")(cy),
            "line2Label" -> messagesApi(s"${prefix}address-lookup.edit-page.line2-label")(cy),
            "line3Label" -> messagesApi(s"${prefix}address-lookup.edit-page.line3-label")(cy),
            "townLabel" -> messagesApi(s"${prefix}address-lookup.edit-page.town-label")(cy),
            "postcodeLabel" -> messagesApi(s"${prefix}address-lookup.edit-page.postcode-label")(cy),
            "countryLabel" -> messagesApi(s"${prefix}address-lookup.edit-page.country-label")(cy)
          ),
          "confirmPageLabels" -> Json.obj(
            "title" -> messagesApi(s"${prefix}address-lookup.confirm-page.title")(cy),
            "heading" -> messagesApi(s"${prefix}address-lookup.confirm-page.heading")(cy),
            "infoSubheading" -> messagesApi(s"${prefix}address-lookup.confirm-page.address-label")(en),
            "infoMessage" -> "",
            "confirmChangeText" -> messagesApi(s"${prefix}address-lookup.confirm-page.para")(cy),
            "submitLabel" -> messagesApi(s"${prefix}address-lookup.confirm-page.confirm")(cy)
          ),
          "countryPickerLabels" -> Json.obj(
            "title" -> messagesApi(s"${prefix}address-lookup.country-page.title")(cy),
            "heading" -> messagesApi(s"${prefix}address-lookup.country-page.heading")(cy),
            "countryLabel" -> messagesApi(s"${prefix}address-lookup.country-page.country-label")(cy)
          )
        )
      )
    )
  }

  private val maxLength = 35

  private val manualAddressEntryConfig = Json.obj(
    "line1MaxLength" -> maxLength,
    "line2MaxLength" -> maxLength,
    "line3MaxLength" -> maxLength,
    "townMaxLength" -> maxLength,
    "showOrganisationName" -> false
  )
}
