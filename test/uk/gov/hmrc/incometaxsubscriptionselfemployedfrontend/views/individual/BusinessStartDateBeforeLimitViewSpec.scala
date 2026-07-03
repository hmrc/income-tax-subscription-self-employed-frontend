/*
 * Copyright 2026 HM Revenue & Customs
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
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessStartDateBeforeLimitForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.submapping.YesNoMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.YesNo
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.{AccountingPeriodUtil, ViewSpec}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.individual.BusinessStartDateBeforeLimit

class BusinessStartDateBeforeLimitViewSpec extends ViewSpec {

  val businessStartDateBeforeLimitView: BusinessStartDateBeforeLimit = app.injector.instanceOf[BusinessStartDateBeforeLimit]

  val form: Form[YesNo] = BusinessStartDateBeforeLimitForm.businessStartDateBeforeLimitForm

  def view(errors: Boolean = false): HtmlFormat.Appendable = {
    businessStartDateBeforeLimitView(
      businessStartDateBeforeLimitForm = if (errors) form.bind(Map.empty[String, String]) else form,
      postAction = testCall,
      isEditMode = false
    )(fakeTestRequest, implicitly)
  }

  val document: Document = Jsoup.parse(view().body)
  def mainContent: Element = document.mainContent

  "BusinessStartDateBeforeLimit" must {
    import BusinessStartDateBeforeLimitMessages.*

    "use the correct template" when {
      "there are no errors" in new TemplateViewTest(
        view = view(),
        title = heading,
        isAgent = false,
        hasSignOutLink = true
      )

      "there are errors" in new TemplateViewTest(
        view = view(errors = true),
        title = heading,
        isAgent = false,
        hasSignOutLink = true,
        errors = Some(Seq(
          BusinessStartDateBeforeLimitForm.startDateBeforeLimit -> s"Select if the business started before ${AccountingPeriodUtil.getStartDateLimit.getYear}"
        ))
      )
    }

    "have the correct caption" in {
      mainContent.selectHead(".govuk-caption-l").text mustBe caption
    }

    "have the correct heading" in {
      mainContent.getH1Element.text mustBe heading
    }

    "have a form" which {
      def formElement: Element = mainContent.getForm

      "has the correct attributes" in {
        document.getForm.attr("method") mustBe testCall.method
        document.getForm.attr("action") mustBe testCall.url
      }

      "have radio buttons to capture if the business started before the limit" in {
        formElement.selectHead(".govuk-form-group").mustHaveRadioInput("fieldset")(
          name = BusinessStartDateBeforeLimitForm.startDateBeforeLimit,
          legend = heading,
          isHeading = true,
          isLegendHidden = false,
          hint = None,
          errorMessage = None,
          radioContents = Seq(
            RadioItem(
              content = Text("Yes"),
              value = Some(YesNoMapping.option_yes)
            ),
            RadioItem(
              content = Text("No"),
              value = Some(YesNoMapping.option_no)
            )
          ),
          isInline = true
        )
      }

      "have a save and continue button" in {
        formElement.selectNth(".govuk-button", 1).text mustBe Buttons.saveAndContinue
      }

      "have a save and come back later button" in {
        formElement.selectNth(".govuk-button", 2).text mustBe Buttons.saveAndComeBackLater
      }
    }
  }

  object BusinessStartDateBeforeLimitMessages {
    val heading: String = s"Did the business start before 6 April ${AccountingPeriodUtil.getStartDateLimit.getYear}?"
    val caption: String = "Sole trader"

    object Buttons {
      val saveAndContinue = "Save and continue"
      val saveAndComeBackLater = "Save and come back later"
    }
  }
}
