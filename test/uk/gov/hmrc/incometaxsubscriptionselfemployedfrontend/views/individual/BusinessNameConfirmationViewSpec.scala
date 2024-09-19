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
import play.api.data.FormError
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{RadioItem, Text}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessNameConfirmationForm._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.submapping.YesNoMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ViewSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.individual.BusinessNameConfirmation

class BusinessNameConfirmationViewSpec extends ViewSpec {

  val businessNameConfirmation: BusinessNameConfirmation = app.injector.instanceOf[BusinessNameConfirmation]
  val testFormError: FormError = FormError(key = fieldName, message = "error.individual.business-name-confirmation.empty")
  val testName: String = "FirstName LastName"
  val testBusinessName: String = "Business name"


  def page(hasFormError: Boolean = false, isBusinessName: Boolean = false): Html = {
    businessNameConfirmation(
      if (hasFormError) {
        businessNameConfirmationForm.withError(testFormError)
      } else {
        businessNameConfirmationForm
      },
      testCall,
      backUrl = testBackUrl,
      isBusinessName = isBusinessName,
      name = if (isBusinessName) testBusinessName else testName,
    )(fakeTestRequest, implicitly)
  }


  def document(hasFormError: Boolean = false, isBusinessName: Boolean = false): Document = {
    Jsoup.parse(page(hasFormError, isBusinessName).body)
  }

  object BusinessNameConfirmationMessages {
    val heading: String = "Is your business trading name the same as your own name?"
    val headingSecond: String = "Is your business trading name the same as the first one you added?"
    val captionHidden: String = "This section is"
    val captionVisible: String = "Sole trader"

    object Summary {
      val businessName: String = "Business name"
    }

    object Form {
      val legend: String = "Is this name correct?"
      val yes: String = "Yes"
      val no: String = "No"
      val emptyError: String = "Select ‘Yes’ if your business trading name is the same as your own name"
      val saveAndContinue: String = "Save and continue"
      val saveAndComeBackLater: String = "Save and come back later"
    }
  }

  "Business Name Confirmation" must {
    "use the correct page template" when {
      "the page is for a personal name" when {
        "there is no error on the page" in new TemplateViewTest(
          page(),
          title = BusinessNameConfirmationMessages.heading,
          backLink = Some(testBackUrl),
          hasSignOutLink = true
        )
        "there is an error on the page" in new TemplateViewTest(
          page(hasFormError = true),
          title = BusinessNameConfirmationMessages.heading,
          backLink = Some(testBackUrl),
          hasSignOutLink = true,
          errors = Some(Seq(testFormError.key -> BusinessNameConfirmationMessages.Form.emptyError))
        )
      }

      "the page is for a secondary business" when {
        "there is no error on the page" in new TemplateViewTest(
          page(isBusinessName = true),
          title = BusinessNameConfirmationMessages.headingSecond,
          backLink = Some(testBackUrl),
          hasSignOutLink = true
        )
        "there is an error on the page" in new TemplateViewTest(
          page(hasFormError = true, isBusinessName = true),
          title = BusinessNameConfirmationMessages.headingSecond,
          backLink = Some(testBackUrl),
          hasSignOutLink = true,
          errors = Some(Seq(testFormError.key -> BusinessNameConfirmationMessages.Form.emptyError))
        )
      }
    }

    "have the correct heading and caption" when {
      "the page is for a personal name" in {
        document().mainContent.mustHaveHeadingAndCaption(
          heading = BusinessNameConfirmationMessages.heading,
          caption = BusinessNameConfirmationMessages.captionVisible,
          isSection = true
        )
      }
      "the page is for a secondary business" in {
        document(isBusinessName = true).mainContent.mustHaveHeadingAndCaption(
          heading = BusinessNameConfirmationMessages.headingSecond,
          caption = BusinessNameConfirmationMessages.captionVisible,
          isSection = true
        )
      }
    }

    "have a summary list detailing their name as the business name" in {
      val businessNameRow: Element = document().mainContent.selectHead("dl").selectHead("div")
      businessNameRow.selectHead("dt").text mustBe BusinessNameConfirmationMessages.Summary.businessName
      businessNameRow.selectHead("dd").text mustBe testName
    }

    "have a form" which {
      def form: Element = document().mainContent.getForm

      "has the correct attributes" in {
        form.attr("method") mustBe testCall.method
        form.attr("action") mustBe testCall.url
      }

      "has the correct inline radio inputs" in {
        document().mustHaveRadioInput(
          selector = "fieldset"
        )(
          name = fieldName,
          legend = BusinessNameConfirmationMessages.Form.legend,
          isHeading = false,
          isLegendHidden = false,
          hint = None,
          errorMessage = None,
          radioContents = Seq(
            RadioItem(
              content = Text(BusinessNameConfirmationMessages.Form.yes),
              value = Some(YesNoMapping.option_yes)
            ),
            RadioItem(
              content = Text(BusinessNameConfirmationMessages.Form.no),
              value = Some(YesNoMapping.option_no)
            )
          ),
          isInline = true
        )
      }

      "has a save and continue button" in {
        val button = form.selectHead(".govuk-button")
        button.text mustBe BusinessNameConfirmationMessages.Form.saveAndContinue
      }

      "has a save and come back later button" in {
        val button = form.selectHead(".govuk-button--secondary")
        button.attr("href") mustBe s"${appConfig.subscriptionFrontendProgressSavedUrl}?location=business-name-confirmation"
        button.attr("role") mustBe "button"
        button.text mustBe BusinessNameConfirmationMessages.Form.saveAndComeBackLater
      }
    }
  }

}
