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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.NextIncomeSourceForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.formatters.DateModelMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ViewSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.NextIncomeSource

class NextIncomeSourceViewSpec extends ViewSpec {

  val form: Form[(String, String, DateModel)] = NextIncomeSourceForm.nextIncomeSourceForm(_.toString)

  val testClientDetails: ClientDetails = ClientDetails("FirstName LastName", "ZZ111111Z")

  val nextIncomeSource: NextIncomeSource = app.injector.instanceOf[NextIncomeSource]

  def view(errors: Boolean = false): HtmlFormat.Appendable = nextIncomeSource(
    nextIncomeSourceForm = if (errors) {
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

  "NextIncomeSource" must {
    "use the correct template" when {
      "there are no errors" in new TemplateViewTest(
        view = view(),
        title = NextIncomeSourceMessages.heading,
        isAgent = true,
        backLink = Some(testBackUrl),
        hasSignOutLink = true
      )
      "there are errors" in new TemplateViewTest(
        view = view(errors = true),
        title = NextIncomeSourceMessages.heading,
        isAgent = true,
        backLink = Some(testBackUrl),
        hasSignOutLink = true,
        errors = Some(Seq(
          NextIncomeSourceForm.businessTradeName -> "Enter the trade of your client’s business",
          NextIncomeSourceForm.businessName -> "Enter your client’s name or the name of their business",
          s"${NextIncomeSourceForm.startDate}-${DateModelMapping.day}" -> "Enter the date your client’s business started trading"
        ))
      )
    }

    "have the correct heading and caption" in {
      mainContent.mustHaveHeadingAndCaption(
        heading = NextIncomeSourceMessages.heading,
        caption = NextIncomeSourceMessages.caption(testClientDetails.name, testClientDetails.formattedNino),
        isSection = false
      )
    }

    "have a form" which {
      def form: Element = mainContent.getForm

      "has the correct attributes" in {
        form.attr("method") mustBe testCall.method
        form.attr("action") mustBe testCall.url
      }

      "has a text input to capture a trade name" in {
        form.mustHaveTextInput(".govuk-form-group:nth-of-type(1)")(
          NextIncomeSourceForm.businessTradeName,
          NextIncomeSourceMessages.Trade.label,
          isLabelHidden = false,
          isPageHeading = false,
          hint = Some(NextIncomeSourceMessages.Trade.hint)
        )
      }

      "has a text input to capture a business name" in {
        form.mustHaveTextInput(".govuk-form-group:nth-of-type(2)")(
          NextIncomeSourceForm.businessName,
          NextIncomeSourceMessages.Name.label,
          isLabelHidden = false,
          isPageHeading = false,
          hint = Some(NextIncomeSourceMessages.Name.hint)
        )
      }

      "has a section to capture a start date" which {
        def dateFormGroup: Element = mainContent.selectHead(".govuk-form-group:nth-of-type(3)")

        "has a fieldset" which {
          def fieldset: Element = dateFormGroup.selectHead("fieldset")

          "has the correct attributes" in {
            fieldset.attr("role") mustBe "group"
            fieldset.attr("aria-describedby") mustBe s"${NextIncomeSourceForm.startDate}-hint"
          }

          "has a legend" in {
            fieldset.selectHead("legend").text mustBe NextIncomeSourceMessages.Date.legend
          }

          "has a hint" in {
            val hint = fieldset.selectHead(".govuk-hint")
            hint.text mustBe NextIncomeSourceMessages.Date.hint
            hint.id mustBe s"${NextIncomeSourceForm.startDate}-hint"
          }

          "has a group of inputs for the date" which {
            def dateGroup: Element = fieldset.selectHead(".govuk-date-input")

            "has the correct attributes" in {
              dateGroup.id mustBe NextIncomeSourceForm.startDate
            }

            "has a field for the day input" which {
              def dayField: Element = dateGroup.selectNth(".govuk-date-input__item", 1)

              "has a label" in {
                val label = dayField.selectHead("label")
                label.text mustBe NextIncomeSourceMessages.Date.Day.label
                label.attr("for") mustBe s"${NextIncomeSourceForm.startDate}-${DateModelMapping.day}"
              }

              "has an input" in {
                val input = dayField.selectHead("input")
                input.id mustBe s"${NextIncomeSourceForm.startDate}-${DateModelMapping.day}"
                input.attr("name") mustBe s"${NextIncomeSourceForm.startDate}-${DateModelMapping.day}"
                input.attr("type") mustBe "text"
                input.attr("inputmode") mustBe "numeric"
              }
            }

            "has a field for the month input" which {
              def monthField: Element = dateGroup.selectNth(".govuk-date-input__item", 2)

              "has a label" in {
                val label = monthField.selectHead("label")
                label.text mustBe NextIncomeSourceMessages.Date.Month.label
                label.attr("for") mustBe s"${NextIncomeSourceForm.startDate}-${DateModelMapping.month}"
              }

              "has an input" in {
                val input = monthField.selectHead("input")
                input.id mustBe s"${NextIncomeSourceForm.startDate}-${DateModelMapping.month}"
                input.attr("name") mustBe s"${NextIncomeSourceForm.startDate}-${DateModelMapping.month}"
                input.attr("type") mustBe "text"
                input.attr("inputmode") mustBe "numeric"
              }
            }

            "has a field for the year input" which {
              def yearField: Element = dateGroup.selectNth(".govuk-date-input__item", 3)

              "has a label" in {
                val label = yearField.selectHead("label")
                label.text mustBe NextIncomeSourceMessages.Date.Year.label
                label.attr("for") mustBe s"${NextIncomeSourceForm.startDate}-${DateModelMapping.year}"
              }

              "has an input" in {
                val input = yearField.selectHead("input")
                input.id mustBe s"${NextIncomeSourceForm.startDate}-${DateModelMapping.year}"
                input.attr("name") mustBe s"${NextIncomeSourceForm.startDate}-${DateModelMapping.year}"
                input.attr("type") mustBe "text"
                input.attr("inputmode") mustBe "numeric"
              }
            }
          }
        }
      }
    }
  }

  object NextIncomeSourceMessages {

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

    object Date {
      val legend = "When did your client’s business start trading?"
      val hint = "We’ll use this date to calculate any Class 2 National Insurance charge. For example, 17 8 2014"

      object Day {
        val label = "Day"
      }

      object Month {
        val label = "Month"
      }

      object Year {
        val label = "Year"
      }
    }

    object Buttons {
      val saveAndContinue = "Save and continue"
      val saveAndComeBackLater = "Save and come back later"
    }

  }

}
