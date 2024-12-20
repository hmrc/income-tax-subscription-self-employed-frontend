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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.agent

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.BusinessAccountingMethodForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{AccountingMethod, Accruals, Cash, ClientDetails}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ViewSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.BusinessAccountingMethod

class BusinessAccountingMethodViewSpec extends ViewSpec with FeatureSwitching {

  val backUrl: Option[String] = Some("/test-back-url")
  val action: Call = testCall
  val emptyErrorKey = "agent.error.business-accounting-method.empty"

  object BusinessAccountingMethodMessages {
    val title = "What accounting method does your client use for their sole trader businesses?"
    val titleSuffix = " - Use software to report your client’s Income Tax - GOV.UK"
    val heading: String = title
    val captionVisible = "FirstName LastName | ZZ 11 11 11 Z"
    val hint = "All your client’s sole trader businesses must use the same accounting method."
    val cash = "Cash basis accounting"
    val accruals = "Traditional accounting"
    val continue = "Continue"
    val update = "Update"
    val backLink = "Back"
    val saveAndContinue = "Save and continue"
    val saveAndComeBackLater = "Save and come back later"
    val emptyError: String = "Select an accounting method."
  }

  private val businessAccountingMethodView = app.injector.instanceOf[BusinessAccountingMethod]

  class Setup(businessAccountingMethodForm: Form[AccountingMethod] = BusinessAccountingMethodForm.businessAccountingMethodForm,
              isEditMode: Boolean = false, backLink: Option[String] = Some(testBackUrl)) {

    val page: HtmlFormat.Appendable = businessAccountingMethodView(
      businessAccountingMethodForm,
      testCall,
      isEditMode,
      backUrl = backLink,
      ClientDetails("FirstName LastName", "ZZ111111Z")
    )(FakeRequest(), implicitly)


    val document: Document = Jsoup.parse(page.body)
  }

  "Business Accounting Method Page" must {

    "have a title" in new Setup {
      document.title mustBe BusinessAccountingMethodMessages.title + BusinessAccountingMethodMessages.titleSuffix
    }

    "have a backlink" in new Setup {
      document.getBackLinkByClass.text mustBe BusinessAccountingMethodMessages.backLink
      document.getBackLinkByClass.attr("href") mustBe testBackUrl
    }

    "have a javascript backlink" in new Setup(backLink = None) {
      document.getBackLinkByClass.text mustBe BusinessAccountingMethodMessages.backLink
      document.getBackLinkByClass.attr("href") mustBe "#"
      document.getBackLinkByClass.attr("data-module") mustBe "hmrc-back-link"
    }

    "have the correct heading and caption" in new Setup {
      document.mainContent.mustHaveHeadingAndCaption(
        heading = BusinessAccountingMethodMessages.heading,
        caption = BusinessAccountingMethodMessages.captionVisible,
        isSection = false
      )
    }

    "have the correct radio inputs" in new Setup {
      document.mainContent.mustHaveRadioInput(
        selector = "fieldset"
      )(
        name = BusinessAccountingMethodForm.businessAccountingMethod,
        legend = BusinessAccountingMethodMessages.heading,
        isHeading = false,
        isLegendHidden = true,
        hint = Some(BusinessAccountingMethodMessages.hint),
        errorMessage = None,
        radioContents = Seq(
          RadioItem(
            content = Text(BusinessAccountingMethodMessages.cash),
            value = Some(Cash.CASH),
          ),
          RadioItem(
            content = Text(BusinessAccountingMethodMessages.accruals),
            value = Some(Accruals.ACCRUALS),
          )
        )
      )
    }

    "have a Form" in new Setup {
      document.getForm.attr("method") mustBe testCall.method
      document.getForm.attr("action") mustBe testCall.url
    }

    "have a save and continue button" in new Setup() {
      document.select("button").last().text mustBe BusinessAccountingMethodMessages.saveAndContinue
    }

    "have a save and come back later button" in new Setup() {
      val saveAndComeBackLater: Element = document.mainContent.selectHead("a[role=button]")
      saveAndComeBackLater.text mustBe BusinessAccountingMethodMessages.saveAndComeBackLater
      saveAndComeBackLater.attr("href") mustBe
        appConfig.subscriptionFrontendClientProgressSavedUrl + "?location=sole-trader-accounting-type"
    }

    "must display empty form error summary when submit with an empty form" in new Setup(
      BusinessAccountingMethodForm.businessAccountingMethodForm.withError(BusinessAccountingMethodForm.businessAccountingMethod, emptyErrorKey)
    ) {
      document.mainContent.mustHaveErrorSummaryByNewGovUkClass(List[String](BusinessAccountingMethodMessages.emptyError))
    }


    "must display empty form error message when submit with an empty form" in new Setup(
      BusinessAccountingMethodForm.businessAccountingMethodForm.withError(BusinessAccountingMethodForm.businessAccountingMethod, emptyErrorKey)
    ) {
      document.mustHaveGovUkErrorNotificationMessage(BusinessAccountingMethodMessages.emptyError)
    }
  }
}
