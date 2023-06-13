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
import org.jsoup.nodes.Document
import play.api.data.{Form, FormError}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.BusinessNameForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.BusinessNameModel
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ViewSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.BusinessName
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.ClientDetails

class BusinessNameViewSpec extends ViewSpec {

  object BusinessNameMessages {
    val title = "What is the name of your client’s business?"
    val titleSuffix = " - Use software to report your client’s Income Tax - GOV.UK"
    val heading: String = title
    val caption = "FirstName LastName | ZZ 11 11 11 Z"
    val backLink = "Back"
    val update = "Update"
    val saveAndContinue = "Save and continue"
    val line1: String = "This is the business name they used to register for Self Assessment. " +
      "If their sole trader business does not have a name, enter your client’s name."
    val emptyError = "Enter your client’s name or the name of their business"
  }

  val backUrl: String = testBackUrl
  val action: Call = testCall
  val testError: FormError = FormError("businessName", "testError")
  val testError2: FormError = FormError("businessName", "testError2")
  val businessName: BusinessName = app.injector.instanceOf[BusinessName]

  class Setup(isEditMode: Boolean = false, businessNameForm: Form[BusinessNameModel] = BusinessNameForm.businessNameValidationForm(Nil)) {
    val page: HtmlFormat.Appendable = businessName(
      businessNameForm,
      testCall,
      isEditMode = isEditMode,
      testBackUrl,
      ClientDetails("FirstName LastName", "ZZ111111Z")
    )(FakeRequest(), implicitly)

    val document: Document = Jsoup.parse(page.body)
  }

  "Business Name Page" must {

    "have a title" in new Setup() {
      document.title mustBe BusinessNameMessages.title + BusinessNameMessages.titleSuffix
    }
    "have a heading" in new Setup() {
      document.getH1Element.text mustBe BusinessNameMessages.heading

    }
    "have a caption" in new Setup(){
      document.selectHead(".govuk-caption-l")
        .text() mustBe BusinessNameMessages.caption
    }

    "have a Form" in new Setup() {
      document.getForm.attr("method") mustBe testCall.method
      document.getForm.attr("action") mustBe testCall.url
    }

    "have a continue button when not in edit mode" in new Setup() {
      document.getGovukButton.text mustBe BusinessNameMessages.saveAndContinue
    }

    "have save and continue button when in edit mode" in new Setup(true) {
      document.getGovukButton.text mustBe BusinessNameMessages.saveAndContinue
    }

    "have a text input field with hint" when {
      "there is an error" in new Setup(isEditMode = false, businessNameForm = BusinessNameForm.businessNameValidationForm(Nil).withError(testError)) {
        document.mustHaveTextInput(
          name = BusinessNameForm.businessName,
          label = BusinessNameMessages.heading,
          hint = Some(BusinessNameMessages.line1),
          error = Some(testError)
        )
      }

      "there is no error" in new Setup(isEditMode = false, businessNameForm = BusinessNameForm.businessNameValidationForm(Nil)) {
        document.mustHaveTextInput(
          name = BusinessNameForm.businessName,
          label = BusinessNameMessages.heading,
          hint = Some(BusinessNameMessages.line1),
          error = None
        )
      }
    }
  }
}
