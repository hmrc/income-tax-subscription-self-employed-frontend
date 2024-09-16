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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.FirstIncomeSourceForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.formatters.DateModelMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ViewSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.FirstIncomeSource

class FirstIncomeSourceViewSpec extends ViewSpec {

  val form: Form[(String, String, DateModel, AccountingMethod)] = FirstIncomeSourceForm.firstIncomeSourceForm(_.toString)

  val testClientDetails: ClientDetails = ClientDetails("FirstName LastName", "ZZ111111Z")

  val firstIncomeSource: FirstIncomeSource = app.injector.instanceOf[FirstIncomeSource]

  def view(errors: Boolean = false): HtmlFormat.Appendable = firstIncomeSource(
    firstIncomeSourceForm = if (errors) {
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

  "FirstIncomeSource" must {
    "use the correct template" when {
      "there are no errors" in new TemplateViewTest(
        view = view(),
        title = FirstIncomeSourceMessages.heading,
        isAgent = true,
        backLink = Some(testBackUrl),
        hasSignOutLink = true
      )
      "there are errors" in new TemplateViewTest(
        view = view(errors = true),
        title = FirstIncomeSourceMessages.heading,
        isAgent = true,
        backLink = Some(testBackUrl),
        hasSignOutLink = true,
        errors = Some(Seq(
          FirstIncomeSourceForm.businessTradeName -> "Enter the trade of your client’s business",
          FirstIncomeSourceForm.businessName -> "Enter your client’s name or the name of their business",
          s"${FirstIncomeSourceForm.startDate}-${DateModelMapping.day}" -> "Enter the date your client’s business started trading",
          FirstIncomeSourceForm.accountingMethodBusiness -> "Select if your client uses cash basis accounting or traditional accounting"
        ))
      )
    }

    "have the correct heading and caption" in {
      mainContent.mustHaveHeadingAndCaption(
        heading = FirstIncomeSourceMessages.heading,
        caption = FirstIncomeSourceMessages.caption(testClientDetails.name, testClientDetails.formattedNino),
        isSection = false
      )
    }

    "have a form" which {
      def form: Element = mainContent.getForm

      "has the correct attributes" in {
        form.attr("method") mustBe testCall.method
        form.attr("action") mustBe testCall.url
      }

      "has a section to capture a trade" which {
        def tradeFormGroup: Element = mainContent.selectHead(".govuk-form-group:nth-of-type(1)")

        "has a label with the correct attributes and text" in {
          val label = tradeFormGroup.selectHead("label")
          label.text mustBe FirstIncomeSourceMessages.Trade.label
          label.attr("for") mustBe FirstIncomeSourceForm.businessTradeName
        }

        "has hint text with a reference id" in {
          val hint = tradeFormGroup.selectHead(".govuk-hint")
          hint.text mustBe FirstIncomeSourceMessages.Trade.hint
          hint.id mustBe s"${FirstIncomeSourceForm.businessTradeName}-hint"
        }

        "has an input field with the correct attributes" in {
          val input = tradeFormGroup.selectHead("input")
          input.id mustBe FirstIncomeSourceForm.businessTradeName
          input.attr("name") mustBe FirstIncomeSourceForm.businessTradeName
          input.attr("type") mustBe "text"
          input.attr("aria-describedby") mustBe s"${FirstIncomeSourceForm.businessTradeName}-hint"
        }
      }

      "has a section to capture a name" which {
        def nameFormGroup: Element = mainContent.selectHead(".govuk-form-group:nth-of-type(2)")

        "has a label with the correct attributes and text" in {
          val label = nameFormGroup.selectHead("label")
          label.text mustBe FirstIncomeSourceMessages.Name.label
          label.attr("for") mustBe FirstIncomeSourceForm.businessName
        }

        "has hint text with a reference id" in {
          val hint = nameFormGroup.selectHead(".govuk-hint")
          hint.text mustBe FirstIncomeSourceMessages.Name.hint
          hint.id mustBe s"${FirstIncomeSourceForm.businessName}-hint"
        }

        "has an input field with the correct attributes" in {
          val input = nameFormGroup.selectHead("input")
          input.id mustBe FirstIncomeSourceForm.businessName
          input.attr("name") mustBe FirstIncomeSourceForm.businessName
          input.attr("type") mustBe "text"
          input.attr("aria-describedby") mustBe s"${FirstIncomeSourceForm.businessName}-hint"
        }
      }

      "has a section to capture a start date" which {
        def dateFormGroup: Element = mainContent.selectHead(".govuk-form-group:nth-of-type(3)")

        "has a fieldset" which {
          def fieldset: Element = dateFormGroup.selectHead("fieldset")

          "has the correct attributes" in {
            fieldset.attr("role") mustBe "group"
            fieldset.attr("aria-describedby") mustBe s"${FirstIncomeSourceForm.startDate}-hint"
          }

          "has a legend" in {
            fieldset.selectHead("legend").text mustBe FirstIncomeSourceMessages.Date.legend
          }

          "has a hint" in {
            val hint = fieldset.selectHead(".govuk-hint")
            hint.text mustBe FirstIncomeSourceMessages.Date.hint
            hint.id mustBe s"${FirstIncomeSourceForm.startDate}-hint"
          }

          "has a group of inputs for the date" which {
            def dateGroup: Element = fieldset.selectHead(".govuk-date-input")

            "has the correct attributes" in {
              dateGroup.id mustBe FirstIncomeSourceForm.startDate
            }

            "has a field for the day input" which {
              def dayField: Element = dateGroup.selectNth(".govuk-date-input__item", 1)

              "has a label" in {
                val label = dayField.selectHead("label")
                label.text mustBe FirstIncomeSourceMessages.Date.Day.label
                label.attr("for") mustBe s"${FirstIncomeSourceForm.startDate}-${DateModelMapping.day}"
              }

              "has an input" in {
                val input = dayField.selectHead("input")
                input.id mustBe s"${FirstIncomeSourceForm.startDate}-${DateModelMapping.day}"
                input.attr("name") mustBe s"${FirstIncomeSourceForm.startDate}-${DateModelMapping.day}"
                input.attr("type") mustBe "text"
                input.attr("inputmode") mustBe "numeric"
              }
            }

            "has a field for the month input" which {
              def monthField: Element = dateGroup.selectNth(".govuk-date-input__item", 2)

              "has a label" in {
                val label = monthField.selectHead("label")
                label.text mustBe FirstIncomeSourceMessages.Date.Month.label
                label.attr("for") mustBe s"${FirstIncomeSourceForm.startDate}-${DateModelMapping.month}"
              }

              "has an input" in {
                val input = monthField.selectHead("input")
                input.id mustBe s"${FirstIncomeSourceForm.startDate}-${DateModelMapping.month}"
                input.attr("name") mustBe s"${FirstIncomeSourceForm.startDate}-${DateModelMapping.month}"
                input.attr("type") mustBe "text"
                input.attr("inputmode") mustBe "numeric"
              }
            }

            "has a field for the year input" which {
              def yearField: Element = dateGroup.selectNth(".govuk-date-input__item", 3)

              "has a label" in {
                val label = yearField.selectHead("label")
                label.text mustBe FirstIncomeSourceMessages.Date.Year.label
                label.attr("for") mustBe s"${FirstIncomeSourceForm.startDate}-${DateModelMapping.year}"
              }

              "has an input" in {
                val input = yearField.selectHead("input")
                input.id mustBe s"${FirstIncomeSourceForm.startDate}-${DateModelMapping.year}"
                input.attr("name") mustBe s"${FirstIncomeSourceForm.startDate}-${DateModelMapping.year}"
                input.attr("type") mustBe "text"
                input.attr("inputmode") mustBe "numeric"
              }
            }
          }
        }
      }

      "has a section to capture an accounting method" which {
        def accountingMethodLabel: Element = mainContent.selectHead("p.govuk-body")

        def accountingMethodDetails: Element = mainContent.selectHead("details")

        def accountingMethodFormGroup: Element = mainContent.selectHead(".govuk-form-group:nth-of-type(4)")

        "has a label outside of the fieldset" in {
          accountingMethodLabel.text mustBe FirstIncomeSourceMessages.AccountingMethod.legend
        }

        "has a details block about accounting method" which {
          "has a summary" in {
            accountingMethodDetails.selectHead("summary").text mustBe FirstIncomeSourceMessages.AccountingMethod.Details.summary
          }
          "has details" which {
            def detail: Element = accountingMethodDetails.selectHead(".govuk-details__text")

            "has a first paragraph" in {
              detail.selectNth("p", 1).text mustBe FirstIncomeSourceMessages.AccountingMethod.Details.paraOne
            }
            "has a secondary paragraph" in {
              detail.selectNth("p", 2).text mustBe FirstIncomeSourceMessages.AccountingMethod.Details.paraTwo
            }
            "has a bullet list" which {
              def bulletList: Element = detail.selectHead("ul")

              "has a first bullet point" in {
                bulletList.selectNth("li", 1).text mustBe FirstIncomeSourceMessages.AccountingMethod.Details.bulletOne
              }
              "has a secondary bullet point" in {
                bulletList.selectNth("li", 2).text mustBe FirstIncomeSourceMessages.AccountingMethod.Details.bulletTwo
              }
            }
          }
        }

        "has a fieldset" which {
          def fieldset: Element = accountingMethodFormGroup.selectHead("fieldset")

          "has the correct attributes" in {
            fieldset.attr("aria-describedby") mustBe s"${FirstIncomeSourceForm.accountingMethodBusiness}-hint"
          }

          "has a legend" in {
            fieldset.selectHead("legend").text mustBe FirstIncomeSourceMessages.AccountingMethod.legend
          }

          "has a hint" in {
            val hint = fieldset.selectHead(".govuk-hint")
            hint.text mustBe FirstIncomeSourceMessages.AccountingMethod.hint
            hint.id mustBe s"${FirstIncomeSourceForm.accountingMethodBusiness}-hint"
          }

          "has a group of radio buttons" which {
            def radioGroup: Element = fieldset.selectHead(".govuk-radios")

            "has a radio button for Cash" which {
              def cashRadio: Element = radioGroup.selectNth(".govuk-radios__item", 1)

              "has an input" in {
                val input = cashRadio.selectHead("input")
                input.id mustBe FirstIncomeSourceForm.accountingMethodBusiness
                input.attr("name") mustBe FirstIncomeSourceForm.accountingMethodBusiness
                input.attr("type") mustBe "radio"
                input.attr("value") mustBe Cash.toString
              }
              "has a label" in {
                val label = cashRadio.selectHead("label")
                label.text mustBe FirstIncomeSourceMessages.AccountingMethod.Cash.label
                label.attr("for") mustBe FirstIncomeSourceForm.accountingMethodBusiness
              }
            }

            "has a radio button for Accruals" which {
              def cashRadio: Element = radioGroup.selectNth(".govuk-radios__item", 2)

              "has an input" in {
                val input = cashRadio.selectHead("input")
                input.id mustBe s"${FirstIncomeSourceForm.accountingMethodBusiness}-2"
                input.attr("name") mustBe FirstIncomeSourceForm.accountingMethodBusiness
                input.attr("type") mustBe "radio"
                input.attr("value") mustBe Accruals.toString
              }
              "has a label" in {
                val label = cashRadio.selectHead("label")
                label.text mustBe FirstIncomeSourceMessages.AccountingMethod.Accruals.label
                label.attr("for") mustBe s"${FirstIncomeSourceForm.accountingMethodBusiness}-2"
              }
            }
          }
        }
      }
    }
  }

  object FirstIncomeSourceMessages {

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

    object AccountingMethod {
      val legend = "What accounting method does your client use for their sole trader businesses?"
      val hint = "All your client’s sole trader businesses must use the same accounting method."

      object Details {
        val summary = "Help with accounting method"
        val paraOne = "Example"
        val paraTwo = "Your client created an invoice for someone in March 2017, but did not receive the money until May 2017. If your client tells HMRC they received this income in:"
        val bulletOne = "May 2017, they use cash basis accounting"
        val bulletTwo = "March 2017, you use traditional accounting"
      }

      object Cash {
        val label = "Cash basis accounting"
      }

      object Accruals {
        val label = "Traditional accounting"
      }

    }

    object Buttons {
      val saveAndContinue = "Save and continue"
      val saveAndComeBackLater = "Save and come back later"
    }

  }

}
