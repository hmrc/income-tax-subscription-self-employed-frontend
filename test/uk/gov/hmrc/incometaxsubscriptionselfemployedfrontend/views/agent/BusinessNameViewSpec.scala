/*
 * Copyright 2021 HM Revenue & Customs
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

class BusinessNameViewSpec extends ViewSpec {

  object BusinessNameMessages {
    val title = "What is the name of your client’s sole trader business?"
    val titleSuffix = " - Use software to report your client’s Income Tax - GOV.UK"
    val heading: String = title
    val continue = "Continue"
    val backLink = "Back"
    val update = "Update"
    val line1 = "This is the business name they used to register for Self Assessment. If their business does not have a name, enter your client’s name."
    val emptyError = "Enter your client`s name or the name of their business"
  }

  val backUrl: String = testBackUrl
  val action: Call = testCall
  val testError: FormError = FormError("businessName", "testError")
  val testError2: FormError = FormError("businessName", "testError2")
  val businessName = app.injector.instanceOf[BusinessName]

  class Setup(isEditMode: Boolean = false, businessNameForm: Form[BusinessNameModel] = BusinessNameForm.businessNameValidationForm(Nil)) {
    val page: HtmlFormat.Appendable = businessName(
      businessNameForm,
      testCall,
      isEditMode = isEditMode,
      testBackUrl
    )(FakeRequest(), implicitly, appConfig)

    val document: Document = Jsoup.parse(page.body)
  }

  "Business Name Page" must {

    "have the correct template" when {
      "there is an error" in new TemplateViewTest(
        view = businessName(
          businessNameForm = BusinessNameForm.businessNameValidationForm(Nil).withError(testError),
          testCall,
          isEditMode = false,
          testBackUrl
        )(FakeRequest(), implicitly, appConfig),
        title = BusinessNameMessages.title,
        isAgent = true,
        backLink = Some(testBackUrl),
        hasSignOutLink = true,
        error = Some(testError)
      )

      "there is no error" in new TemplateViewTest(
        view = businessName(
          businessNameForm = BusinessNameForm.businessNameValidationForm(Nil),
          testCall,
          isEditMode = false,
          testBackUrl
        )(FakeRequest(), implicitly, appConfig),
        title = BusinessNameMessages.title,
        isAgent = true,
        backLink = Some(testBackUrl),
        hasSignOutLink = true,
        error = None
      )
    }

    "have a title" in new Setup() {
      document.title mustBe BusinessNameMessages.title + BusinessNameMessages.titleSuffix
    }
    "have a heading" in new Setup() {
      document.getH1Element.text mustBe BusinessNameMessages.heading

    }

    "have a Form" in new Setup() {
      document.getForm.attr("method") mustBe testCall.method
      document.getForm.attr("action") mustBe testCall.url
    }

    "have a continue button when not in edit mode" in new Setup() {
      document.getGovukButton.text mustBe BusinessNameMessages.continue
    }

    "have update button when in edit mode" in new Setup(true) {
      document.getGovukButton.text mustBe BusinessNameMessages.update
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
