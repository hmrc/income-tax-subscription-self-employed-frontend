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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.agent

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.{Form, FormError}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.BusinessNameForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.BusinessNameModel
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ViewSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.business_name

class BusinessNameViewSpec extends ViewSpec {

  object BusinessNameMessages {
    val title = "What is the name of your client’s business?"
    val titleSuffix = " - Business Tax account - GOV.UK"
    val heading: String = title
    val continue = "Continue"
    val backLink = "Back"
    val update = "Update"
    val line1 = "This is the business name they used to register for Self Assessment. If their business does not have a name, enter your client’s full name."
    val emptyError = "Enter your client`s name or the name of their business"
  }

  val backUrl: String = testBackUrl
  val action: Call = testCall
  val testError: FormError = FormError("businessName", "testError")
  val testError2: FormError = FormError("businessName", "testError2")

  class Setup(isEditMode: Boolean = false, businessNameForm: Form[BusinessNameModel] = BusinessNameForm.businessNameValidationForm(Nil)) {
    val page: HtmlFormat.Appendable = business_name(
      businessNameForm,
      testCall,
      isEditMode = isEditMode,
      testBackUrl
    )(FakeRequest(), implicitly, appConfig)

    val document: Document = Jsoup.parse(page.body)
  }

  "Business Name Page" must {
    "have a title" in new Setup() {
      document.title mustBe BusinessNameMessages.title + BusinessNameMessages.titleSuffix
    }
    "have a heading" in new Setup() {
      document.getH1Element.text mustBe BusinessNameMessages.heading

    }

    "have a paragraph" in new Setup() {
      document.select("article p").text mustBe BusinessNameMessages.line1

    }


    "have a Form" in new Setup() {
      document.getForm.attr("method") mustBe testCall.method
      document.getForm.attr("action") mustBe testCall.url
    }

    "have a continue button when not in edit mode" in new Setup() {
      document.getSubmitButton.text mustBe BusinessNameMessages.continue
    }

    "have update button when in edit mode" in new Setup(true) {
      document.getSubmitButton.text mustBe BusinessNameMessages.update
    }

  }

  "have a backlink" in new Setup() {
    document.getBackLink.text mustBe BusinessNameMessages.backLink
    document.getBackLink.attr("href") mustBe testBackUrl

  }

  "must display form error on page" in new Setup(false, BusinessNameForm.businessNameValidationForm(Nil).withError(testError)) {
    document.mustHaveErrorSummary(List[String](testError.message))
    document.listErrorMessages(List[String](testError.message))

  }

  "must display multiple form errors on page" in new Setup(false, BusinessNameForm.businessNameValidationForm(Nil).withError(testError).withError(testError2)) {
    document.mustHaveErrorSummary(List[String](testError.message, testError2.message))
    document.listErrorMessages(List[String](testError.message, testError2.message))
  }


}
