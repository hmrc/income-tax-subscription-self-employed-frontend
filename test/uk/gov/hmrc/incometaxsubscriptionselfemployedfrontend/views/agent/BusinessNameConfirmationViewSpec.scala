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
import org.jsoup.nodes.{Document, Element}
import play.api.data.FormError
import play.twirl.api.Html
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.BusinessNameConfirmationForm._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.submapping.YesNoMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.ClientDetails
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ViewSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.BusinessNameConfirmation

class BusinessNameConfirmationViewSpec extends ViewSpec {

  val businessNameConfirmation: BusinessNameConfirmation = app.injector.instanceOf[BusinessNameConfirmation]
  val testFormError: FormError = FormError(key = fieldName, message = "error.agent.business-name-confirmation.empty")
  val clientDetails: ClientDetails = ClientDetails(
    "FirstName LastName",
    "ZZ111111Z"
  )

  def page(hasFormError: Boolean = false): Html = {
    businessNameConfirmation(
      if (hasFormError) {
        businessNameConfirmationForm.withError(testFormError)
      } else {
        businessNameConfirmationForm
      },
      testCall,
      backUrl = testBackUrl,
      clientDetails = clientDetails
    )(fakeTestRequest, implicitly)
  }

  def document(hasFormError: Boolean = false): Document = {
    Jsoup.parse(page(hasFormError).body)
  }

  object BusinessNameConfirmationMessages {
    val heading: String = "Is your client’s business name the same as their own name?"
    val caption: String = s"${clientDetails.name} | ${clientDetails.formattedNino}"

    object Summary {
      val businessName: String = "Business name"
    }

    object Form {
      val legend: String = "Is this name correct?"
      val yes: String = "Yes"
      val no: String = "No"
      val emptyError: String = "Select ‘Yes’ if this is correct"
      val saveAndContinue: String = "Save and continue"
      val saveAndComeBackLater: String = "Save and come back later"
    }
  }

  "Business Name Confirmation" must {
    "use the correct page template" when {
      "there is no error on the page" in new TemplateViewTest(
        page(),
        title = BusinessNameConfirmationMessages.heading,
        isAgent = true,
        backLink = Some(testBackUrl),
        hasSignOutLink = true
      )
      "there is an error on the page" in new TemplateViewTest(
        page(hasFormError = true),
        title = BusinessNameConfirmationMessages.heading,
        isAgent = true,
        backLink = Some(testBackUrl),
        hasSignOutLink = true,
        error = Some(testFormError.key -> BusinessNameConfirmationMessages.Form.emptyError)
      )
    }

    "have a page heading with caption" in {
      val mainContent: Element = document().mainContent
      mainContent.selectHead("h1.govuk-heading-l").text() mustBe BusinessNameConfirmationMessages.heading
      mainContent.selectHead("span.govuk-caption-l").text() mustBe BusinessNameConfirmationMessages.caption
    }

    "have a summary list detailing their name as the business name" in {
      val businessNameRow: Element = document().mainContent.selectHead("dl").selectHead("div")
      businessNameRow.selectHead("dt").text mustBe BusinessNameConfirmationMessages.Summary.businessName
      businessNameRow.selectHead("dd").text mustBe clientDetails.name
    }

    "have a form" which {
      def form: Element = document().mainContent.getForm

      "has the correct attributes" in {
        form.attr("method") mustBe testCall.method
        form.attr("action") mustBe testCall.url
      }

      "has a radio button set" which {
        "has a fieldset" which {
          def fieldset: Element = form.selectHead("fieldset")

          "has a legend" in {
            fieldset.selectHead("legend").text mustBe BusinessNameConfirmationMessages.Form.legend
          }

          "has an inline radio button set" which {
            def radioSet: Element = fieldset.selectHead(".govuk-radios--inline")

            "has a yes radio button and label" in {
              val yesSet = radioSet.selectNth(".govuk-radios__item", 1)
              val radioButton = yesSet.selectHead("input")
              radioButton.attr("id") mustBe fieldName
              radioButton.attr("name") mustBe fieldName
              radioButton.attr("type") mustBe "radio"
              radioButton.attr("value") mustBe YesNoMapping.option_yes

              val label = yesSet.selectHead("label")
              label.attr("for") mustBe fieldName
              label.text mustBe BusinessNameConfirmationMessages.Form.yes
            }

            "has a no radio button and label" in {
              val noSet = radioSet.selectNth(".govuk-radios__item", 2)
              val radioButton = noSet.selectHead("input")
              radioButton.attr("id") mustBe s"$fieldName-2"
              radioButton.attr("name") mustBe fieldName
              radioButton.attr("type") mustBe "radio"
              radioButton.attr("value") mustBe YesNoMapping.option_no

              val label = noSet.selectHead("label")
              label.attr("for") mustBe s"$fieldName-2"
              label.text mustBe BusinessNameConfirmationMessages.Form.no
            }
          }
        }
      }

      "has a save and continue button" in {
        val button = form.selectHead(".govuk-button")
        button.text mustBe BusinessNameConfirmationMessages.Form.saveAndContinue
      }

      "has a save and come back later button" in {
        val button = form.selectHead(".govuk-button--secondary")
        button.attr("href") mustBe s"${appConfig.subscriptionFrontendClientProgressSavedUrl}?location=business-name-confirmation"
        button.attr("role") mustBe "button"
        button.text mustBe BusinessNameConfirmationMessages.Form.saveAndComeBackLater
      }
    }
  }

}
