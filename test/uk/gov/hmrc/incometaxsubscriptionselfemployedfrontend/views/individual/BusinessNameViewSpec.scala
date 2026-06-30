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
import play.api.data.Form
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.StreamlineIncomeSourceForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.YesNo
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ViewSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.individual.BusinessName as BusinessNameView

class BusinessNameViewSpec extends ViewSpec {

  val businessNameView: BusinessNameView = app.injector.instanceOf[BusinessNameView]
  val form: Form[(String, String, YesNo)] = StreamlineIncomeSourceForm.fullIncomeSourceForm

  def view(errors: Boolean = false): HtmlFormat.Appendable = businessNameView(
    fullIncomeSourceForm = if (errors) form.bind(Map.empty[String, String]) else form,
    postAction = testCall,
    isEditMode = false
  )(fakeTestRequest, implicitly)

  val document: Document = Jsoup.parse(view().body)

  def mainContent: Element = document.mainContent

  "FullIncomeSource" must {
    import BusinessNameMessages.*

    "use the correct template" when {
      "there are no errors" in new TemplateViewTest(
        view = view(),
        title = BusinessNameMessages.title,
        isAgent = false,
        hasSignOutLink = true
      )
      "there are errors" in new TemplateViewTest(
        view = view(errors = true),
        title = BusinessNameMessages.title,
        isAgent = false,
        hasSignOutLink = true,
        errors = Some(Seq(StreamlineIncomeSourceForm.businessName -> "Enter your name or the name of your business"))
      )
    }

    "have the correct heading and caption" in {
      mainContent.mustHaveHeadingAndCaption(
        heading = BusinessNameMessages.heading,
        caption = BusinessNameMessages.caption,
        isSection = true
      )
    }

    "have the correct paragraph" in {
      val text = mainContent.selectHead(".govuk-body").text
      text mustBe BusinessNameMessages.paragraph
    }

    "have a form" which {
      def form: Element = mainContent.getForm

      "has the correct attributes" in {
        document.getForm.attr("method") mustBe testCall.method
        document.getForm.attr("action") mustBe testCall.url
      }

      "have a text input to capture a business name" in {
        form.mustHaveTextInput(".govuk-form-group:nth-of-type(2)")(
          name = StreamlineIncomeSourceForm.businessName,
          label = businessNameLabel,
          isLabelHidden = false,
          isPageHeading = false,
          hint = Some(businessNameHint),
          autoComplete = Some("organization")
        )
      }

      "have a save and continue button" in {
        form.selectNth(".govuk-button", 1).text mustBe Buttons.saveAndContinue
      }

      "have a save and come back later button" in {
        form.selectNth(".govuk-button", 2).text mustBe Buttons.saveAndComeBackLater
      }
    }
  }


  object BusinessNameMessages {
    val heading = "Business name"
    val title = "Your sole trader business name"
    val caption = "Sole trader"
    val paragraph = "This is the business name you used to register for Self Assessment. If your business does not have a name, enter your own first and last name."
    val businessNameLabel = "What is your business name?"
    val businessNameHint = "The business name you enter can only include upper case or lower case letters, full stops, commas, digits, &, ’, /, \\, -."
    
    object Buttons {
      val saveAndContinue = "Save and continue"
      val saveAndComeBackLater = "Save and come back later"
    }
  }
}
