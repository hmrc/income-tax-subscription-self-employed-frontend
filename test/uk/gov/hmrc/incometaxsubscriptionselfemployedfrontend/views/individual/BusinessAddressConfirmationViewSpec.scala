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
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessAddressConfirmationForm._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.submapping.YesNoMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.Address
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ViewSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.individual.BusinessAddressConfirmation

class BusinessAddressConfirmationViewSpec extends ViewSpec {

  val businessAddressConfirmation: BusinessAddressConfirmation = app.injector.instanceOf[BusinessAddressConfirmation]
  val testFormError: FormError = FormError(key = fieldName, message = "error.individual.business-address-confirmation.empty")
  val testAddress: Address = Address(
    Seq(
      "1 Long Road",
      "Lonely Town"
    ),
    Some("ZZ11ZZ")
  )

  def page(hasFormError: Boolean = false): Html = {
    businessAddressConfirmation(
      if (hasFormError) {
        businessAddressConfirmationForm.withError(testFormError)
      } else {
        businessAddressConfirmationForm
      },
      testCall,
      backUrl = testBackUrl,
      address = testAddress
    )(fakeTestRequest, implicitly)
  }

  def document(hasFormError: Boolean = false): Document = {
    Jsoup.parse(page(hasFormError).body)
  }

  object BusinessAddressConfirmationMessages {
    val heading: String = "Confirm business address"
    val captionHidden: String = "This section is"
    val captionVisible: String = "Sole trader"
    val para: String = "Does this business have the same address as the first one you added?"

    object Summary {
      val businessAddress: String = "Address"
    }

    object Form {
      val legend: String = "Is this address correct?"
      val yes: String = "Yes"
      val no: String = "No"
      val emptyError: String = "Select ‘Yes’ if this is your business address"
      val saveAndContinue: String = "Save and continue"
      val saveAndComeBackLater: String = "Save and come back later"
    }
  }

  "Business Address Confirmation" must {
    "use the correct page template" when {
      "there is no error on the page" in new TemplateViewTest(
        page(),
        title = BusinessAddressConfirmationMessages.heading,
        isAgent = false,
        backLink = Some(testBackUrl),
        hasSignOutLink = true
      )
      "there is an error on the page" in new TemplateViewTest(
        page(hasFormError = true),
        title = BusinessAddressConfirmationMessages.heading,
        isAgent = false,
        backLink = Some(testBackUrl),
        hasSignOutLink = true,
        errors = Some(Seq(testFormError.key -> BusinessAddressConfirmationMessages.Form.emptyError))
      )
    }

    "have the correct heading and caption" in {
      document().mainContent.mustHaveHeadingAndCaption(
        heading = BusinessAddressConfirmationMessages.heading,
        caption = BusinessAddressConfirmationMessages.captionVisible,
        isSection = true
      )
    }

    "have a page with a paragraph" in {
      document().mainContent.selectNth("p", 2).text mustBe BusinessAddressConfirmationMessages.para
    }

    "have a summary list detailing the previous address" in {
      val businessAddressRow: Element = document().mainContent.selectHead("dl").selectHead("div")
      businessAddressRow.selectHead("dt").text mustBe BusinessAddressConfirmationMessages.Summary.businessAddress
      businessAddressRow.selectHead("dd").text mustBe testAddress.toString.replace("<br>", " ")
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
          legend = BusinessAddressConfirmationMessages.Form.legend,
          isHeading = false,
          isLegendHidden = false,
          hint = None,
          errorMessage = None,
          radioContents = Seq(
            RadioItem(
              content = Text(BusinessAddressConfirmationMessages.Form.yes),
              value = Some(YesNoMapping.option_yes)
            ),
            RadioItem(
              content = Text(BusinessAddressConfirmationMessages.Form.no),
              value = Some(YesNoMapping.option_no)
            )
          )
        )
      }


      "has a save and continue button" in {
        val button = form.selectHead(".govuk-button")
        button.text mustBe BusinessAddressConfirmationMessages.Form.saveAndContinue
      }

      "has a save and come back later button" in {
        val button = form.selectHead(".govuk-button--secondary")
        button.attr("href") mustBe s"${appConfig.subscriptionFrontendProgressSavedUrl}?location=business-address-confirmation"
        button.attr("role") mustBe "button"
        button.text mustBe BusinessAddressConfirmationMessages.Form.saveAndComeBackLater
      }
    }
  }

}
