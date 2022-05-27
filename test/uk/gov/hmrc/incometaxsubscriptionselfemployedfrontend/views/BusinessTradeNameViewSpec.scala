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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.api.data.{Form, FormError}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitch.SaveAndRetrieve
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessTradeNameForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.BusinessTradeNameModel
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ViewSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.BusinessTradeName

class BusinessTradeNameViewSpec extends ViewSpec with FeatureSwitching {

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(SaveAndRetrieve)
  }

  val businessTradeName: BusinessTradeName = app.injector.instanceOf[BusinessTradeName]

  object BusinessTradeNameMessages {
    val title = "What is the trade of your business?"
    val titleSuffix = " - Use software to send Income Tax updates - GOV.UK"
    val heading: String = title
    val hintText = "For example: plumbing, electrical work, consulting."
    val continue = "Continue"
    val update = "Update"
    val backLink = "Back"
    val saveAndContinue = "Save and continue"
    val saveAndComeBackLater = "Save and come back later"
  }

  val backUrl: String = testBackUrl
  val action: Call = testCall
  val taxYearEnd: Int = 2020
  val testError: FormError = FormError("businessTradeName", "testError")
  val id: String = "testId"

  class Setup(isEditMode: Boolean = false, saveAndRetrieveEnabled: Boolean = false,
              businessTradeNameForm: Form[BusinessTradeNameModel] = BusinessTradeNameForm.businessTradeNameValidationForm(Nil)) {

    if(saveAndRetrieveEnabled) enable(SaveAndRetrieve)

    val page: HtmlFormat.Appendable = businessTradeName(
      businessTradeNameForm,
      testCall,
      isEditMode = isEditMode,
      testBackUrl
    )(FakeRequest(), implicitly, appConfig)

    val document: Document = Jsoup.parse(page.body)
  }


  "Business Trade Name" must {
    "have a title" in new Setup {
      document.title mustBe BusinessTradeNameMessages.title + BusinessTradeNameMessages.titleSuffix
    }
    "have a heading" in new Setup {
      document.getH1Element.text mustBe BusinessTradeNameMessages.heading
    }
    "have a Form" in new Setup {
      document.getForm.attr("method") mustBe testCall.method
      document.getForm.attr("action") mustBe testCall.url
    }
    "have a textInputs" in new Setup {
      document.mustHaveTextField("businessTradeName", BusinessTradeNameMessages.title)
    }
    "have a hint text for textInput" in new Setup {
      document.getHintTextByClass mustBe BusinessTradeNameMessages.hintText
    }
    "have a continue button when not in edit mode" in new Setup {
      document.getButtonByClass mustBe BusinessTradeNameMessages.continue
    }
    "have update button when in edit mode" in new Setup(true) {
      document.getButtonByClass mustBe BusinessTradeNameMessages.update
    }
    "have a save and continue button" when {
      "the save and retrieve feature switch is enabled" in new Setup(false, true) {
        document.select("button").last().text mustBe BusinessTradeNameMessages.saveAndContinue
      }
    }
    "have a save and come back later link" when {
      "the save and retrieve feature switch is enabled" in new Setup(false, true) {
        val saveAndComeBackLink: Element = document.selectHead("a[role=button]")
        saveAndComeBackLink.text mustBe BusinessTradeNameMessages.saveAndComeBackLater
        saveAndComeBackLink.attr("href") mustBe
          appConfig.subscriptionFrontendProgressSavedUrl + "?location=sole-trader-business-trade"
      }
    }
    "have a backlink " in new Setup {
      document.getBackLinkByClass.text mustBe BusinessTradeNameMessages.backLink
      document.getBackLinkByClass.attr("href") mustBe testBackUrl
    }
    "must display form error on page along with textInput and hintText" in
      new Setup(false, false, BusinessTradeNameForm.businessTradeNameValidationForm(Nil).withError(testError)) {
        document.mustHaveErrorSummaryByNewGovUkClass(List[String](testError.message))
        document.mustHaveTextField("businessTradeName", BusinessTradeNameMessages.title)
        document.getHintTextByClass mustBe BusinessTradeNameMessages.hintText
      }

  }

}
