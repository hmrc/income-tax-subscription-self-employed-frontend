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
import org.jsoup.nodes.Document
import play.api.data.{Form, FormError}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessNameForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ViewSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.BusinessName

class BusinessNameViewSpec extends ViewSpec {

  object BusinessNameMessages {
    val title = "What is the name of your business?"
    val titleSuffix = " - Use software to send Income Tax updates - GOV.UK"
    val heading: String = title
    val captionHidden = "This section is"
    val captionVisible = "Sole trader"
    val continue = "Continue"
    val update = "Update"
    val saveAndContinueButton = "Save and continue"
    val backLink = "Back"
    val line1: String = "This is the business name you used to register for Self Assessment. " +
      "If your sole trader business does not have a separate name, enter your own first and last name."
    val line2 = "The business name you enter can only include upper or lower case letters, full stops, commas, digits, &, ', \\, /, -."
    val emptyError = "Enter your name or the name of your business."
  }

  val backUrl: String = testBackUrl
  val action: Call = testCall
  val formError: FormError = FormError("businessName", "error.business-name.empty")
  val businessName: BusinessName = app.injector.instanceOf[BusinessName]

  class Setup(isEditMode: Boolean = false, businessNameForm: Form[String] = BusinessNameForm.businessNameValidationForm(Nil)) {
    val page: HtmlFormat.Appendable = businessName(
      businessNameForm,
      testCall,
      isEditMode = isEditMode,
      testBackUrl

    )(FakeRequest(), implicitly)

    val document: Document = Jsoup.parse(page.body)
  }

  "Business Name Page" must {

    "have a heading with caption" in new Setup() {
      document.selectHead(".hmrc-caption").text mustBe s"${BusinessNameMessages.captionHidden} ${BusinessNameMessages.captionVisible}"
      document.getH1Element.text mustBe BusinessNameMessages.heading

    }

    "have a Form" in new Setup() {
      document.getForm.attr("method") mustBe testCall.method
      document.getForm.attr("action") mustBe testCall.url
    }

    "have a text input field with hint" when {
      "there is an error" in new Setup(isEditMode = false, businessNameForm = BusinessNameForm.businessNameValidationForm(Nil).withError(formError)) {
        document.mustHaveTextInput(
          name = BusinessNameForm.businessName,
          label = BusinessNameMessages.heading,
          hint = Some(s"${BusinessNameMessages.line1} ${BusinessNameMessages.line2}"),
          error = Some(BusinessNameForm.businessName -> BusinessNameMessages.emptyError)
        )
      }

      "there is no error" in new Setup(isEditMode = false, businessNameForm = BusinessNameForm.businessNameValidationForm(Nil)) {
        document.mustHaveTextInput(
          name = BusinessNameForm.businessName,
          label = BusinessNameMessages.heading,
          hint = Some(s"${BusinessNameMessages.line1} ${BusinessNameMessages.line2}"),
          error = None
        )
      }

    }

    "have a save and continue button when not in edit mode" in new Setup(isEditMode = false) {
      document.mainContent.selectHead(".govuk-button").text mustBe BusinessNameMessages.saveAndContinueButton
    }

    "have save and continue button when in edit mode" in new Setup(isEditMode = true) {
      document.select("button").last().text mustBe BusinessNameMessages.saveAndContinueButton
    }

  }

}
