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
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.StreamlineIncomeSourceForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.submapping.YesNoMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.YesNo
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.{AccountingPeriodUtil, ViewSpec}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.individual.{FullIncomeSource => FullIncomeSourceView}

class FullIncomeSourceViewSpec extends ViewSpec {

  val fullIncomeSourceView: FullIncomeSourceView = app.injector.instanceOf[FullIncomeSourceView]
  val form: Form[(String, String, YesNo)] = StreamlineIncomeSourceForm.fullIncomeSourceForm

  def view(errors: Boolean = false): HtmlFormat.Appendable = fullIncomeSourceView(
    fullIncomeSourceForm = if (errors) form.bind(Map.empty[String, String]) else form,
    postAction = testCall,
    isEditMode = false,
    backUrl = testBackUrl
  )(fakeTestRequest, implicitly)

  val document: Document = Jsoup.parse(view().body)

  def mainContent: Element = document.mainContent

  "FullIncomeSource" must {
    import FullIncomeSourceMessages._

    "use the correct template" when {
      "there are no errors" in new TemplateViewTest(
        view = view(),
        title = FullIncomeSourceMessages.heading,
        isAgent = false,
        backLink = Some(testBackUrl),
        hasSignOutLink = true
      )
      "there are errors" in new TemplateViewTest(
        view = view(errors = true),
        title = FullIncomeSourceMessages.heading,
        isAgent = false,
        backLink = Some(testBackUrl),
        hasSignOutLink = true,
        errors = Some(Seq(
          StreamlineIncomeSourceForm.businessTradeName -> "Enter the trade of this business",
          StreamlineIncomeSourceForm.businessName -> "Enter your name or the name of your business",
          StreamlineIncomeSourceForm.startDateBeforeLimit -> s"Select ‘Yes’ if this business started before 6 April ${AccountingPeriodUtil.getStartDateLimit.getYear}"
        ))
      )
    }

    "have the correct heading" in {
      mainContent.getH1Element.text mustBe heading
    }

    "have a form" which {
      def form: Element = mainContent.getForm

      "has the correct attributes" in {
        document.getForm.attr("method") mustBe testCall.method
        document.getForm.attr("action") mustBe testCall.url
      }

      "have a text input to capture a trade name" in {
        form.mustHaveTextInput(".govuk-form-group:nth-of-type(1)")(
          name = StreamlineIncomeSourceForm.businessTradeName,
          label = tradeLabel,
          isLabelHidden = false,
          isPageHeading = false,
          hint = Some(tradeHint)
        )
      }

      "have a text input to capture a business name" in {
        form.mustHaveTextInput(".govuk-form-group:nth-of-type(2)")(
          name = StreamlineIncomeSourceForm.businessName,
          label = businessNameLabel,
          isLabelHidden = false,
          isPageHeading = false,
          hint = Some(businessNameHint)
        )
      }

      "have radio buttons to capture if start date is before the limit" in {
        form.selectHead(".govuk-form-group:nth-of-type(3)").mustHaveRadioInput("fieldset")(
          name = StreamlineIncomeSourceForm.startDateBeforeLimit,
          legend = startDateLabel,
          isHeading = false,
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
        form.selectNth(".govuk-button", 1).text mustBe Buttons.saveAndContinue
      }

      "have a save and come back later button" in {
        form.selectNth(".govuk-button", 2).text mustBe Buttons.saveAndComeBackLater
      }
    }
  }


  object FullIncomeSourceMessages {
    val heading = "Your sole trader business"
    val tradeLabel = "Trade"
    val tradeHint = "For example plumbing, electrical work or hairdressing"
    val businessNameLabel = "Business name"
    val businessNameHint = "This is the business name you used to register for Self Assessment. If your business does not have a name, enter your full name."
    val startDateLabel = s"Did this business start before 6 April ${AccountingPeriodUtil.getStartDateLimit.getYear}?"

    object Buttons {
      val saveAndContinue = "Save and continue"
      val saveAndComeBackLater = "Save and come back later"
    }
  }
}
