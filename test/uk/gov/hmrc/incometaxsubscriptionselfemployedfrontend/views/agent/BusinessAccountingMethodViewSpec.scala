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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{AccountingMethodModel, Accruals, Cash}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ViewSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.BusinessAccountingMethod

class BusinessAccountingMethodViewSpec extends ViewSpec with FeatureSwitching {

  val backUrl: Option[String] = Some("/test-back-url")
  val action: Call = testCall
  val emptyError = "Select if your client uses cash basis accounting or traditional accounting"

  object BusinessAccountingMethodMessages {
    val title = "What accounting method does your client use for their sole trader businesses?"
    val titleSuffix = " - Use software to report your client’s Income Tax - GOV.UK"
    val heading: String = title
    val cash = "Cash basis accounting"
    val accruals = "Traditional accounting"
    val continue = "Continue"
    val update = "Update"
    val backLink = "Back"
    val saveAndContinue = "Save and continue"
    val saveAndComeBackLater = "Save and come back later"
  }

  private val businessAccountingMethodView = app.injector.instanceOf[BusinessAccountingMethod]

  class Setup(businessAccountingMethodForm: Form[AccountingMethodModel] = BusinessAccountingMethodForm.businessAccountingMethodForm,
              isEditMode: Boolean = false, backLink: Option[String] = Some(testBackUrl)) {

    val page: HtmlFormat.Appendable = businessAccountingMethodView(
      businessAccountingMethodForm,
      testCall,
      isEditMode,
      backUrl = backLink
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
      document.getBackLinkByClass.attr("href") mustBe "javascript:history.back()"
    }

    "have a heading" in new Setup {
      document.getH1Element.text mustBe BusinessAccountingMethodMessages.heading

    }

    "have a hidden legend" in new Setup {
      document.selectHead("legend").text mustBe BusinessAccountingMethodMessages.heading
      document.selectHead("legend").attr("class") mustBe "govuk-fieldset__legend govuk-visually-hidden"
    }

    "has a set of radio buttons inputs" in new Setup {
      document.mainContent.mustHaveRadioInput(
        name = BusinessAccountingMethodForm.businessAccountingMethod,
        radioItems = Seq(
          RadioItem(
            content = Text(BusinessAccountingMethodMessages.cash),
            value = Some(Cash.CASH),
            id = Some(BusinessAccountingMethodForm.businessAccountingMethod)
          ),
          RadioItem(
            content = Text(BusinessAccountingMethodMessages.accruals),
            value = Some(Accruals.ACCRUALS),
            id = Some(s"${BusinessAccountingMethodForm.businessAccountingMethod}-2")
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

    "must display empty form error summary when submit with an empty form" in new Setup(BusinessAccountingMethodForm.businessAccountingMethodForm.withError("", emptyError)) {
      document.mustHaveErrorSummaryByNewGovUkClass(List[String](emptyError))
    }

    "must display empty form error message when submit with an empty form" in new Setup(BusinessAccountingMethodForm.businessAccountingMethodForm.withError(BusinessAccountingMethodForm.businessAccountingMethod, emptyError)) {

      document.mustHaveGovUkErrorNotificationMessage(emptyError)
    }
  }
}
