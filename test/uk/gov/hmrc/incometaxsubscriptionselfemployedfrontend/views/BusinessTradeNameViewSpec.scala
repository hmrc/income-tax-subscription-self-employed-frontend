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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.api.data.{Form, FormError}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessTradeNameForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ViewSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.BusinessTradeName

class BusinessTradeNameViewSpec extends ViewSpec with FeatureSwitching {

  override def beforeEach(): Unit = {
    super.beforeEach()
  }

  val businessTradeName: BusinessTradeName = app.injector.instanceOf[BusinessTradeName]

  object BusinessTradeNameMessages {
    val title = "What is the trade of your business?"
    val titleSuffix = " - Use software to send Income Tax updates - GOV.UK"
    val heading: String = title
    val captionHidden = "This section is"
    val captionVisible = "Sole trader"
    val hintText = "For example: plumbing, electrical work, consulting."
    val update = "Update"
    val backLink = "Back"
    val saveAndContinue = "Save and continue"
    val saveAndComeBackLater = "Save and come back later"
    val emptyError = "Enter the trade of your business."
  }

  val backUrl: String = testBackUrl
  val action: Call = testCall
  val taxYearEnd: Int = 2020
  val emptyFormError: FormError = FormError("businessTradeName", "error.business-trade-name.empty")
  val id: String = "testId"

  class Setup(isEditMode: Boolean = false,
              businessTradeNameForm: Form[String] = BusinessTradeNameForm.businessTradeNameValidationForm(Nil)) {

    val page: HtmlFormat.Appendable = businessTradeName(
      businessTradeNameForm,
      testCall,
      isEditMode = isEditMode,
      testBackUrl
    )(FakeRequest(), implicitly)

    val document: Document = Jsoup.parse(page.body)
  }


  "Business Trade Name" must {
    "have a title" in new Setup {
      document.title mustBe BusinessTradeNameMessages.title + BusinessTradeNameMessages.titleSuffix
    }
    "have a caption" in new Setup {
      document.selectHead(".govuk-caption-l")
        .text() mustBe BusinessTradeNameMessages.captionVisible
    }
    "have a heading" in new Setup {
      document.getH1Element.text mustBe BusinessTradeNameMessages.heading
    }
    "have a form" which {
      "has the correct attributes" in new Setup {
        document.getForm.attr("method") mustBe testCall.method
        document.getForm.attr("action") mustBe testCall.url
      }

      "has a text input field with hint" when {
        "there is no error" in new Setup {
          document.getForm.mustHaveTextInput()(
            name = BusinessTradeNameForm.businessTradeName,
            label = BusinessTradeNameMessages.heading,
            isLabelHidden = false,
            isPageHeading = true,
            hint = Some(BusinessTradeNameMessages.hintText)
          )
        }

        "there is an error" in
          new Setup(false, BusinessTradeNameForm.businessTradeNameValidationForm(Nil).withError(emptyFormError)) {
            document.getForm.mustHaveTextInput()(
              name = BusinessTradeNameForm.businessTradeName,
              label = BusinessTradeNameMessages.heading,
              isLabelHidden = false,
              isPageHeading = true,
              hint = Some(BusinessTradeNameMessages.hintText),
              error = Some(BusinessTradeNameMessages.emptyError)
            )
          }
      }
    }

    "have update button when in edit mode" in new Setup(true) {
      document.getButtonByClass mustBe BusinessTradeNameMessages.saveAndContinue
    }
    "have a save and continue button" in new Setup() {
      document.select("button").last().text mustBe BusinessTradeNameMessages.saveAndContinue
    }
    "have a save and come back later link" in new Setup() {
      val saveAndComeBackLink: Element = document.selectHead("a[role=button]")
      saveAndComeBackLink.text mustBe BusinessTradeNameMessages.saveAndComeBackLater
      saveAndComeBackLink.attr("href") mustBe
        appConfig.subscriptionFrontendProgressSavedUrl + "?location=sole-trader-business-trade"
    }
    "have a backlink " in new Setup {
      document.getBackLinkByClass.text mustBe BusinessTradeNameMessages.backLink
      document.getBackLinkByClass.attr("href") mustBe testBackUrl
    }
  }

}
