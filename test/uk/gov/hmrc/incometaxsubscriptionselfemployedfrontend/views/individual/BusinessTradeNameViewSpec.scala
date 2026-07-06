/*
 * Copyright 2024 HM Revenue & Customs
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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessTradeNameForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ViewSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.individual.BusinessTradeName as BusinessTradeNameView

class BusinessTradeNameViewSpec extends ViewSpec {

  val businessTradeNameView: BusinessTradeNameView = app.injector.instanceOf[BusinessTradeNameView]
  val form: Form[String] = BusinessTradeNameForm.businessTradeNameForm

  def view(errors: Boolean = false): HtmlFormat.Appendable = businessTradeNameView(
    businessTradeNameForm = if (errors) form.bind(Map.empty[String, String]) else form,
    postAction = testCall,
    isEditMode = false
  )(fakeTestRequest, implicitly)

  val document: Document = Jsoup.parse(view().body)

  def mainContent: Element = document.mainContent

  "BusinessTradeName" must {
    import BusinessTradeNameMessages.*

    "use the correct template" when {
      "there are no errors" in new TemplateViewTest(
        view = view(),
        title = title,
        isAgent = false,
        hasSignOutLink = true
      )
      "there are errors" in new TemplateViewTest(
        view = view(errors = true),
        title = title,
        isAgent = false,
        hasSignOutLink = true,
        errors = Some(Seq(BusinessTradeNameForm.businessTradeName -> "Add your trade"))
      )
    }

    "have the correct heading and caption" in {
      mainContent.mustHaveHeadingAndCaption(
        heading = heading,
        caption = caption,
        isSection = true
      )
    }

    "have the correct paragraph" in {
      mainContent.selectHead(".govuk-body").text mustBe paragraph
    }

    "have a form" which {
      def form: Element = mainContent.getForm

      "has the correct attributes" in {
        document.getForm.attr("method") mustBe testCall.method
        document.getForm.attr("action") mustBe testCall.url
      }

      "has a text input to capture a business trade name" in {
        form.mustHaveTextInput(".govuk-form-group")(
          name = BusinessTradeNameForm.businessTradeName,
          label = businessTradeNameLabel,
          isLabelHidden = true,
          isPageHeading = false,
          hint = Some(businessTradeNameHint),
          autoComplete = Some("organization")
        )
      }

      "has a save and continue button" in {
        form.selectNth(".govuk-button", 1).text mustBe Buttons.saveAndContinue
      }

      "has a save and come back later button" in {
        form.selectNth(".govuk-button", 2).text mustBe Buttons.saveAndComeBackLater
      }
    }
  }

  object BusinessTradeNameMessages {
    val heading = "What is the trade of your business?"
    val title = "What is the trade of your business?"
    val caption = "Sole trader"
    val paragraph = "The trade of your business is the goods or services that your business provides."
    val businessTradeNameLabel = "{0}"
    val businessTradeNameHint = "For example plumbing, electrical work, consulting, hairdressing, personal training, photography work."

    object Buttons {
      val saveAndContinue = "Save and continue"
      val saveAndComeBackLater = "Save and come back later"
    }
  }
}