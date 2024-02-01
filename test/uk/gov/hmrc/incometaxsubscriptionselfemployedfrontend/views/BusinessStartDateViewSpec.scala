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
import org.jsoup.select.Elements
import play.api.data.{Form, FormError}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessStartDateForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.DateModel
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ViewSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.{BusinessStartDate => BusinessStartDateView}

import java.time.LocalDate

class BusinessStartDateViewSpec extends ViewSpec {

  object BusinessStartDateMessages {
    val title = "When did your business start trading?"
    val titleSuffix = " - Use software to send Income Tax updates - GOV.UK"
    val heading: String = title
    val captionHidden = "This section is"
    val captionVisual = "Sole trader"
    val line_1: String = "This is the date we’ll use to calculate Class 2 National Insurance charge, if appropriate."
    val hint = "For example, 17 4 2018."
    val backLink = "Back"
    val update = "Update"
    val saveAndContinue = "Save and continue"
    val saveAndComeBack = "Save and come back later"
    val empty = "Enter the date your business started trading."
    val maxDate = s"The date the business started trading must be on or before 11 April 2021."
    val minDate = "The date your business started must be on or after 11 April 2021."
  }

  val backUrl: String = testBackUrl
  val action: Call = testCall
  val taxYearEnd: Int = 2020
  private val testError: FormError = FormError("startDate", "error.business-start-date.day-month-year.empty")

  private val dateTooLateError = FormError("startDate", "error.business-start-date.day-month-year.max-date", List("11 April 2021"))
  private val dateTooEarlyError = FormError("startDate", "error.business-start-date.day-month-year.min-date", List("11 April 2021"))

  val businessStartDateView: BusinessStartDateView = app.injector.instanceOf[BusinessStartDateView]

  class Setup(
               isEditMode: Boolean = false,
               form: Form[DateModel] = BusinessStartDateForm.businessStartDateForm(
                 BusinessStartDateForm.minStartDate,
                 BusinessStartDateForm.maxStartDate, d => d.toString
               )
             ) {
    val page: HtmlFormat.Appendable = businessStartDateView(
      form,
      testCall,
      isEditMode,
      testBackUrl
    )(FakeRequest(), implicitly)

    val document: Document = Jsoup.parse(page.body)
  }


  "Business Start Date" must {

    "have a title" in new Setup {
      document.title mustBe BusinessStartDateMessages.title + BusinessStartDateMessages.titleSuffix
    }
    "have a caption" in new Setup {
      document.selectHead(".hmrc-caption").text mustBe s"${BusinessStartDateMessages.captionHidden} ${BusinessStartDateMessages.captionVisual}"
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
      private val fieldset = document.selectFirst("fieldset")
      fieldset.select("div[id=startDate]").attr("class") mustBe "govuk-date-input"
      fieldset.attr("aria-describedby") mustBe s"startDate-hint"
      fieldset.selectHead("legend").text mustBe BusinessStartDateMessages.heading
      fieldset.selectHead("div[id=startDate-hint]").text mustBe BusinessStartDateMessages.hint
      fieldset.select("div label[for=startDate-dateDay]").text() mustBe "Day"
      fieldset.select("div label[for=startDate-dateMonth]").text() mustBe "Month"
      fieldset.select("div label[for=startDate-dateYear]").text() mustBe "Year"
    }

    "has buttons" which {
      "include the save and continue button" in new Setup() {
        document.getForm.getGovukButton.text mustBe BusinessStartDateMessages.saveAndContinue
      }
      "include the save and come back later link" in new Setup() {
        val saveAndComeBackLink: Element = document.selectHead("a[role=button]")
        saveAndComeBackLink.text mustBe BusinessStartDateMessages.saveAndComeBack
        saveAndComeBackLink.attr("href") mustBe
          appConfig.subscriptionFrontendProgressSavedUrl + "?location=sole-trader-trading-start-date"
      }
    }

    "have a backlink " in new Setup {
      private val backLink: Elements = document.select(".govuk-back-link")
      backLink.text mustBe BusinessStartDateMessages.backLink
      backLink.attr("href") mustBe testBackUrl
    }

    "must display form error on page" in new Setup(
      isEditMode = false,
      form = BusinessStartDateForm.businessStartDateForm(BusinessStartDateForm.minStartDate, BusinessStartDateForm.maxStartDate, d => d.toString).withError(testError)
    ) {
      document.select("div[class=govuk-error-summary]").select("div").attr("role") mustBe "alert"
      document.select("div[class=govuk-error-summary]").select("h2").text mustBe "There is a problem"
      document.select("p[id=startDate-Error]").text() mustBe s"Error: ${BusinessStartDateMessages.empty}"
    }

    "must display max date error on page" in new Setup(
      form = BusinessStartDateForm.businessStartDateForm(LocalDate.now(), LocalDate.now(), d => d.toString).withError(dateTooLateError)
    ) {
      document.select("div[class=govuk-error-summary]").select("div").attr("role") mustBe "alert"
      document.select("div[class=govuk-error-summary]").select("h2").text mustBe "There is a problem"
      document.select("p[id=startDate-Error]").text() mustBe s"Error: ${BusinessStartDateMessages.maxDate}"
    }

    "must display min date error on page" in new Setup(
      form = BusinessStartDateForm.businessStartDateForm(LocalDate.now(), LocalDate.now(), d => d.toString).withError(dateTooEarlyError)
    ) {
      document.select("div[class=govuk-error-summary]").select("div").attr("role") mustBe "alert"
      document.select("div[class=govuk-error-summary]").select("h2").text mustBe "There is a problem"
      document.select("p[id=startDate-Error]").text() mustBe s"Error: ${BusinessStartDateMessages.minDate}"
    }
  }
}
