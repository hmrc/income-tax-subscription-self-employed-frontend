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
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.data.{Form, FormError}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessStartDateForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.BusinessStartDate
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ViewSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.{BusinessStartDate => BusinessStartDateView}

import java.time.LocalDate

class BusinessStartDateViewSpec extends ViewSpec {

  object BusinessStartDateMessages {
    val title = "When did your sole trader business start trading?"
    val titleSuffix = " - Use software to send Income Tax updates - GOV.UK"
    val heading: String = title
    val line_1: String = "If you have multiple sole trader businesses, enter the start date of your main business."
    val hint = "For example, 17 4 2018."
    val continue = "Continue"
    val backLink = "Back"
    val update = "Update"
    val saveAndContinue = "Save and continue"
    val saveAndComeBack = "Save and come back later"
    val empty = "Enter the date your business started trading."
    val maxDate = "The date your business started trading must be the same as or before 11 April 2021."
    val minDate = "The date your business started must be on or after 11 April 2021."
  }

  val backUrl: String = testBackUrl
  val action: Call = testCall
  val taxYearEnd: Int = 2020
  private val testError: FormError = FormError("startDate", "error.business_start_date.day_month_year.empty")

  private val dateTooLateError = FormError("startDate", "error.business_start_date.day_month_year.max_date", List("11 April 2021"))
  private val dateTooEarlyError = FormError("startDate", "error.business_start_date.day_month_year.min_date", List("11 April 2021"))

  val businessStartDateView : BusinessStartDateView = app.injector.instanceOf[BusinessStartDateView]

  class Setup(isEditMode: Boolean = false,
              isSaveAndRetrieve: Boolean = false,
              businessStartDateForm: Form[BusinessStartDate] = BusinessStartDateForm.businessStartDateForm(BusinessStartDateForm.minStartDate, BusinessStartDateForm.maxStartDate, d => d.toString)) {
    val page: HtmlFormat.Appendable = businessStartDateView(
      businessStartDateForm,
      testCall,
      isEditMode,
      isSaveAndRetrieve = isSaveAndRetrieve,
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
      document.select("p[class=govuk-body]").text mustBe BusinessStartDateMessages.line_1
    }
    "have a Form" in new Setup {
      document.getForm.attr("method") mustBe testCall.method
      document.getForm.attr("action") mustBe testCall.url
    }
    "have a fieldset with dateInputs" in new Setup {
      val fieldset = document.selectFirst("fieldset")
      fieldset.select("div[id=startDate]").attr("class") mustBe "govuk-date-input"
      fieldset.attr("aria-describedby") mustBe s"startDate-hint"
      fieldset.selectHead("legend").text mustBe BusinessStartDateMessages.heading
      fieldset.selectHead("div[id=startDate-hint]").text mustBe BusinessStartDateMessages.hint
      fieldset.select("div label[for=startDate-dateDay]").text() mustBe "Day"
      fieldset.select("div label[for=startDate-dateMonth]").text() mustBe "Month"
      fieldset.select("div label[for=startDate-dateYear]").text() mustBe "Year"
    }

    "have a continue button when not in edit mode" in new Setup {
      document.select("button[id=continue-button]").text mustBe BusinessStartDateMessages.continue
    }

    "have update button when in edit mode" in new Setup(true) {
      document.select("button[id=continue-button]").text mustBe BusinessStartDateMessages.update
    }

    "have a continue button with alternate text when in SaveAndRetrieve mode" in new Setup(false, true) {
      document.select("button").last.text mustBe BusinessStartDateMessages.saveAndContinue
    }

    "have a SaveAndComeBack button when in SaveAndRetrieve mode" in new Setup(false, true) {
      document.select(s"a[href=${appConfig.incomeTaxSubscriptionFrontendBaseUrl + "/business/progress-saved"}]").text mustBe BusinessStartDateMessages.saveAndComeBack
    }

    "have a backlink " in new Setup {
      private val backLink: Elements = document.select(".govuk-back-link")
      backLink.text mustBe BusinessStartDateMessages.backLink
      backLink.attr("href") mustBe testBackUrl
    }

    "must display form error on page" in new Setup(
      isEditMode = false,
      businessStartDateForm = BusinessStartDateForm.businessStartDateForm(BusinessStartDateForm.minStartDate, BusinessStartDateForm.maxStartDate, d => d.toString).withError(testError)
    ) {
      document.select("div[class=govuk-error-summary]").attr("role") mustBe "alert"
      document.select("div[class=govuk-error-summary]").select("h2").text mustBe "There is a problem"
      document.select("p[id=startDate-Error]").text() mustBe s"Error: ${BusinessStartDateMessages.empty}"
    }

    "must display max date error on page" in new Setup(
      businessStartDateForm = BusinessStartDateForm.businessStartDateForm(LocalDate.now(), LocalDate.now(), d => d.toString).withError(dateTooLateError)
    ){
      document.select("div[class=govuk-error-summary]").attr("role") mustBe "alert"
      document.select("div[class=govuk-error-summary]").select("h2").text mustBe "There is a problem"
      document.select("p[id=startDate-Error]").text() mustBe s"Error: ${BusinessStartDateMessages.maxDate}"
    }

    "must display min date error on page" in new Setup(
      businessStartDateForm = BusinessStartDateForm.businessStartDateForm(LocalDate.now(), LocalDate.now(), d => d.toString).withError(dateTooEarlyError)
    ){
      document.select("div[class=govuk-error-summary]").attr("role") mustBe "alert"
      document.select("div[class=govuk-error-summary]").select("h2").text mustBe "There is a problem"
      document.select("p[id=startDate-Error]").text() mustBe s"Error: ${BusinessStartDateMessages.minDate}"
    }
  }
}
