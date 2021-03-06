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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.{Form, FormError}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessStartDateForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.BusinessStartDate
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ViewSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.business_start_date

class BusinessStartDateViewSpec extends ViewSpec {

  object BusinessStartDateMessages {
    val title = "When did your sole trader business start trading?"
    val titleSuffix = " - Use software to send Income Tax updates - GOV.UK"
    val heading: String = title
    val line_1: String = "If you have multiple sole trader businesses, enter the start date of your main business."
    val exampleStartDate = "For example, 1 4 2018"
    val continue = "Continue"
    val backLink = "Back"
    val update = "Update"
  }

  val backUrl: String = testBackUrl
  val action: Call = testCall
  val taxYearEnd: Int = 2020
  val testError: FormError = FormError("startDate", "testError")

  class Setup(isEditMode: Boolean = false,
              businessStartDateForm: Form[BusinessStartDate] = BusinessStartDateForm.businessStartDateForm("minStartDateError", "maxStartDateError")) {
    val page: HtmlFormat.Appendable = business_start_date(
      businessStartDateForm,
      testCall,
      isEditMode,
      testBackUrl
    )(FakeRequest(), implicitly, appConfig)

    val document: Document = Jsoup.parse(page.body)
  }


  "Business Start Date" must {

    "have a title" in new Setup {
      document.title mustBe BusinessStartDateMessages.title + BusinessStartDateMessages.titleSuffix
    }
    "have a heading" in new Setup {
      document.getH1Element.text mustBe BusinessStartDateMessages.heading
    }
    "have a paragraph" in new Setup {
      document.select("article p").text mustBe BusinessStartDateMessages.line_1
    }
    "have a Form" in new Setup {
      document.getForm.attr("method") mustBe testCall.method
      document.getForm.attr("action") mustBe testCall.url
    }
    "have a fieldset with dateInputs" in new Setup {
      document.mustHaveDateField("startDate", BusinessStartDateMessages.heading, BusinessStartDateMessages.exampleStartDate)
    }
    "have a continue button when not in edit mode" in new Setup {
      document.getSubmitButton.text mustBe BusinessStartDateMessages.continue
    }
    "have update button when in edit mode" in new Setup(true) {
      document.getSubmitButton.text mustBe BusinessStartDateMessages.update
    }
    "have a backlink " in new Setup {
      document.getBackLink.text mustBe BusinessStartDateMessages.backLink
      document.getBackLink.attr("href") mustBe testBackUrl
    }
    "must display form error on page" in new Setup(
      isEditMode = false,
      businessStartDateForm = BusinessStartDateForm.businessStartDateForm("minStartDateError", "maxStartDateError").withError(testError)
    ) {
      document.mustHaveErrorSummary(List[String](testError.message))
      document.mustHaveDateField("startDate", BusinessStartDateMessages.heading, BusinessStartDateMessages.exampleStartDate, Some(testError.message))
    }

  }
}
