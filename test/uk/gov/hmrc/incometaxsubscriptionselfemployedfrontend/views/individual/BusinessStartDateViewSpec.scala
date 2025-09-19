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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.individual

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import play.api.data.{Form, FormError}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessStartDateForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.DateModel
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ViewSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.individual.BusinessStartDate

class BusinessStartDateViewSpec extends ViewSpec {

  object BusinessStartDateMessages {
    val title = "Start date for sole trader business"
    val titleSuffix = " - Use software to send Income Tax updates - GOV.UK"
    val heading: String = "Start date"
    val captionVisual = "Your sole trader business"
    val paragraph: String = "We need to know the exact start date."
    val hint = "For example, 27 9 2023"
    val backLink = "Back"
    val saveAndContinue = "Save and continue"
    val saveAndComeBack = "Save and come back later"
    val empty = "Enter the date your business started trading"

    val maxDate = s"The date cannot be more than 7 days in the future"
    val minDate = "The date must be on or after 6 April 2021"
  }

  private val emptyError: FormError = FormError("startDate", "error.business.start-date.day-month-year.empty")

  private val startDateBeforeLimitMaxDateError = FormError("startDate", "error.business.start-date.day-month-year.max-date", List("11 April 2021"))
  private val startDateBeforeLimitMinDateError = FormError("startDate", "error.business.start-date.day-month-year.min-date", List("6 April 2021"))

  val businessStartDateView: BusinessStartDate = app.injector.instanceOf[BusinessStartDate]

  class Setup(
               isEditMode: Boolean = false,
               form: Form[DateModel] = BusinessStartDateForm.businessStartDateForm(
                 BusinessStartDateForm.maxStartDate, _.toString
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

    "have the correct heading and caption" in new Setup {
      document.mainContent.mustHaveHeadingAndCaption(
        heading = BusinessStartDateMessages.heading,
        caption = BusinessStartDateMessages.captionVisual,
        isSection = true
      )
    }

    "have a paragraph" in new Setup {
      document.mainContent.getParagraphNth() mustBe BusinessStartDateMessages.paragraph
    }

    "have a Form" in new Setup {
      document.getForm.attr("method") mustBe testCall.method
      document.getForm.attr("action") mustBe testCall.url
    }

    "has the correct date input field with the legend as the page heading" when {
      "there is no error on the page" in new Setup {
        document.getForm.mustHaveDateInput(
          id = "startDate",
          legend = BusinessStartDateMessages.heading,
          exampleDate = BusinessStartDateMessages.hint,
          isHeading = false,
          isLegendHidden = true,
          dateInputsValues = Seq(
            DateInputFieldValues("Day", None),
            DateInputFieldValues("Month", None),
            DateInputFieldValues("Year", None)
          )
        )
      }

      "has a form empty error on page" in new Setup(
        isEditMode = false,
        form = BusinessStartDateForm.businessStartDateForm(
          BusinessStartDateForm.maxStartDate, _.toString
        ).withError(emptyError)
      ) {
        document.mustHaveDateInput(
          id = "startDate",
          legend = BusinessStartDateMessages.heading,
          exampleDate = BusinessStartDateMessages.hint,
          errorMessage = Some(BusinessStartDateMessages.empty),
          isHeading = false,
          isLegendHidden = true,
          dateInputsValues = Seq(
            DateInputFieldValues("Day", None),
            DateInputFieldValues("Month", None),
            DateInputFieldValues("Year", None)
          )
        )
      }

      "has a max date error on page" in new Setup(
        isEditMode = false,
        form = BusinessStartDateForm.businessStartDateForm(
          BusinessStartDateForm.maxStartDate, _.toString
        ).withError(startDateBeforeLimitMaxDateError)
      ) {

        document.mustHaveDateInput(
          id = "startDate",
          legend = BusinessStartDateMessages.heading,
          exampleDate = BusinessStartDateMessages.hint,
          errorMessage = Some(BusinessStartDateMessages.maxDate),
          isHeading = false,
          isLegendHidden = true,
          dateInputsValues = Seq(
            DateInputFieldValues("Day", None),
            DateInputFieldValues("Month", None),
            DateInputFieldValues("Year", None)
          )
        )
      }

      "has a min date error on page" in new Setup(
        isEditMode = false,
        form = BusinessStartDateForm.businessStartDateForm(
          BusinessStartDateForm.maxStartDate, _.toString
        ).withError(startDateBeforeLimitMinDateError)
      ) {
        {

          document.mustHaveDateInput(
            id = "startDate",
            legend = BusinessStartDateMessages.heading,
            exampleDate = BusinessStartDateMessages.hint,
            errorMessage = Some(BusinessStartDateMessages.minDate),
            isHeading = false,
            isLegendHidden = true,
            dateInputsValues = Seq(
              DateInputFieldValues("Day", None),
              DateInputFieldValues("Month", None),
              DateInputFieldValues("Year", None)
            )
          )
        }
      }
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

  }
}
