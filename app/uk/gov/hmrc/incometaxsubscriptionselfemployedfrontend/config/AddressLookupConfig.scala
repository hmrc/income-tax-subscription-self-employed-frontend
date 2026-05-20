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
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{JsObject, JsValue, Json}
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
        "useNewGovUkServiceNavigation" -> true,
        "includeHMRCBranding" -> false,
        "continueUrl" -> continueUrl,
        "showBackButtons" -> true,
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
        Seq(en, cy).map { lang => labels(lang, prefix) }:_*
      )
    )
  }
  
  private def labels(lang: Lang, prefix: String): (String, JsValueWrapper) =
    lang.language -> Json.obj(
      "appLevelLabels" -> Json.obj(
        "navTitle" -> messagesApi(s"base.${prefix}service-name")(lang)
      ),
      "selectPageLabels" -> Json.obj(
        "title" -> messagesApi(s"${prefix}address-lookup.select-page.title")(lang),
        "heading" -> messagesApi(s"${prefix}address-lookup.select-page.heading")(lang)
      ),
      "lookupPageLabels" -> Json.obj(
        "title" -> messagesApi(s"${prefix}address-lookup.lookup-page.title")(lang),
        "heading" -> messagesApi(s"${prefix}address-lookup.lookup-page.heading")(lang),
        "filterLabel" -> messagesApi("address-lookup.lookup-page.property-label")(lang),
        "postcodeLabel" -> messagesApi("address-lookup.lookup-page.postcode-label")(lang)
      ),
      "editPageLabels" -> Json.obj(
        "title" -> messagesApi(s"${prefix}address-lookup.edit-page.title")(lang),
        "heading" -> messagesApi(s"${prefix}address-lookup.edit-page.heading")(lang),
        "line1Label" -> messagesApi("address-lookup.edit-page.line1-label")(lang),
        "line2Label" -> messagesApi("address-lookup.edit-page.line2-label")(lang),
        "line3Label" -> messagesApi("address-lookup.edit-page.line3-label")(lang),
        "townLabel" -> messagesApi("address-lookup.edit-page.town-label")(lang),
        "postcodeLabel" -> messagesApi("address-lookup.edit-page.postcode-label")(lang),
        "countryLabel" -> messagesApi("address-lookup.edit-page.country-label")(lang)
      ),
      "international" -> Json.obj(
        "editPageLabels" -> Json.obj(
          "title" -> messagesApi(s"${prefix}address-lookup.edit-page.title")(lang),
          "heading" -> messagesApi(s"${prefix}address-lookup.edit-page.heading")(lang),
          "line1Label" -> messagesApi("address-lookup.edit-page.line1-label")(lang),
          "line2Label" -> messagesApi("address-lookup.edit-page.line2-label")(lang),
          "line3Label" -> messagesApi("address-lookup.edit-page.line3-label")(lang),
          "townLabel" -> messagesApi("address-lookup.edit-page.town-label")(lang),
          "postcodeLabel" -> messagesApi("address-lookup.edit-page.postcode-label")(lang),
          "countryLabel" -> messagesApi("address-lookup.edit-page.country-label")(lang)
        )
      ),
      "confirmPageLabels" -> Json.obj(
        "title" -> messagesApi(s"${prefix}address-lookup.confirm-page.title")(lang),
        "heading" -> messagesApi(s"${prefix}address-lookup.confirm-page.heading")(lang),
        "infoSubheading" -> messagesApi("address-lookup.confirm-page.address-label")(lang),
        "infoMessage" -> "",
        "confirmChangeText" -> messagesApi("address-lookup.confirm-page.para")(lang),
        "submitLabel" -> messagesApi("address-lookup.confirm-page.confirm")(lang)
      ),
      "countryPickerLabels" -> Json.obj(
        "title" -> messagesApi(s"${prefix}address-lookup.country-page.title")(lang),
        "heading" -> messagesApi(s"${prefix}address-lookup.country-page.heading")(lang),
        "countryLabel" -> messagesApi("address-lookup.country-page.country-label")(lang)
      )
    )

  private val maxLength = 35

  private val manualAddressEntryConfig = Json.obj(
    "line1MaxLength" -> maxLength,
    "line2MaxLength" -> maxLength,
    "line3MaxLength" -> maxLength,
    "townMaxLength" -> maxLength,
    "showOrganisationName" -> false,
    "mandatoryFields" -> Json.obj(
      "addressLine1" -> true,
      "addressLine2" -> false,
      "addressLine3" -> false,
      "town" -> true,
      "postcode" -> false
    )
  )
}
