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
import play.api.data.Form
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.{RadioItem, Text}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.StreamlineIncomeSourceForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.submapping.YesNoMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.{AccountingPeriodUtil, ViewSpec}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.FullIncomeSource

class FullIncomeSourceViewSpec extends ViewSpec {

  val form: Form[(String, String, YesNo)] = StreamlineIncomeSourceForm.fullIncomeSourceForm

  val testClientDetails: ClientDetails = ClientDetails("FirstName LastName", "ZZ111111Z")

  val nextIncomeSource: FullIncomeSource = app.injector.instanceOf[FullIncomeSource]

  def view(errors: Boolean = false): HtmlFormat.Appendable = nextIncomeSource(
    fullIncomeSourceForm = if (errors) {
      form.bind(Map.empty[String, String])
    } else {
      form
    },
    postAction = testCall,
    isEditMode = false,
    backUrl = testBackUrl,
    clientDetails = testClientDetails
  )(fakeTestRequest, implicitly)

  def document: Document = Jsoup.parse(view().body)

  def mainContent: Element = document.mainContent

  "FullIncomeSource" must {
    "use the correct template" when {
      "there are no errors" in new TemplateViewTest(
        view = view(),
        title = FullIncomeSourceMessages.heading,
        isAgent = true,
        backLink = Some(testBackUrl),
        hasSignOutLink = true
      )
      "there are errors" in new TemplateViewTest(
        view = view(errors = true),
        title = FullIncomeSourceMessages.heading,
        isAgent = true,
        backLink = Some(testBackUrl),
        hasSignOutLink = true,
        errors = Some(Seq(
          StreamlineIncomeSourceForm.businessTradeName -> "Enter the trade of your client’s business",
          StreamlineIncomeSourceForm.businessName -> "Enter your client’s name or the name of their business",
          StreamlineIncomeSourceForm.startDateBeforeLimit -> s"Select ‘Yes’ if this business started before 6 April ${AccountingPeriodUtil.getStartDateLimit.getYear}"
        ))
      )
    }

    "have the correct heading and caption" in {
      mainContent.mustHaveHeadingAndCaption(
        heading = FullIncomeSourceMessages.heading,
        caption = FullIncomeSourceMessages.caption(testClientDetails.name, testClientDetails.formattedNino),
        isSection = false
      )
    }

    "have a form" which {
      def form: Element = mainContent.getForm

      "has the correct attributes" in {
        form.attr("method") mustBe testCall.method
        form.attr("action") mustBe testCall.url
      }

      "have a text input to capture a trade name" in {
        form.mustHaveTextInput(".govuk-form-group:nth-of-type(1)")(
          StreamlineIncomeSourceForm.businessTradeName,
          FullIncomeSourceMessages.Trade.label,
          isLabelHidden = false,
          isPageHeading = false,
          hint = Some(FullIncomeSourceMessages.Trade.hint)
        )
      }

      "have a text input to capture a business name" in {
        form.mustHaveTextInput(".govuk-form-group:nth-of-type(2)")(
          StreamlineIncomeSourceForm.businessName,
          FullIncomeSourceMessages.Name.label,
          isLabelHidden = false,
          isPageHeading = false,
          hint = Some(FullIncomeSourceMessages.Name.hint)
        )
      }

      "have a section to capture if the users start date is before the limit" in {
        form.selectHead(".govuk-form-group:nth-of-type(3)").mustHaveRadioInput("fieldset")(
          name = StreamlineIncomeSourceForm.startDateBeforeLimit,
          legend = FullIncomeSourceMessages.DateBeforeLimit.legend,
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

      "have a button group" which {
        def buttonGroup: Element = form.selectHead(".govuk-button-group")

        "has a save and continue button" in {
          buttonGroup.selectHead(".govuk-button").text mustBe FullIncomeSourceMessages.Buttons.saveAndContinue
        }

        "has a save and come back later button" in {
          val saveAndComeBackLater: Element = buttonGroup.selectHead(".govuk-button--secondary")

          saveAndComeBackLater.text mustBe FullIncomeSourceMessages.Buttons.saveAndComeBackLater
          saveAndComeBackLater.attr("href") mustBe s"${appConfig.subscriptionFrontendClientProgressSavedUrl}?location=sole-trader-income-source"
        }
      }
    }
  }

  object FullIncomeSourceMessages {

    def caption(name: String, nino: String): String = s"$name | $nino"

    val heading = "Your client’s sole trader business"

    object Trade {
      val label = "What is the trade of your client’s business?"
      val hint = "For example plumbing, electrical work or hairdressing."
    }

    object Name {
      val label = "What is the name of your client’s business?"
      val hint = "This is the business name your client used to register for Self Assessment. If their business does not have a name, enter your client’s full name."
    }

    object DateBeforeLimit {
      val legend: String = s"Did this business start before 6 April ${AccountingPeriodUtil.getStartDateLimit.getYear}?"
    }

    object Buttons {
      val saveAndContinue = "Save and continue"
      val saveAndComeBackLater = "Save and come back later"
    }

  }

}
