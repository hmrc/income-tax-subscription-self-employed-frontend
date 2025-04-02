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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.scalatest.Checkpoints.Checkpoint
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{Assertion, BeforeAndAfterEach, Succeeded}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.{AnyContentAsEmpty, Call}
import play.api.test.FakeRequest
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes

import scala.jdk.CollectionConverters._

trait ViewSpec extends AnyWordSpecLike with Matchers with GuiceOneAppPerSuite with BeforeAndAfterEach {

  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  implicit lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  implicit lazy val wrappedMessages: Messages = MessagesWrapper(Lang("en"), messagesApi)

  val testBackUrl = "/test-back-url"
  val testCall: Call = Call("POST", "/test-url")
  val fakeTestRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("POST", "/test-url")

  class TemplateViewTest(view: Html,
                         title: String,
                         isAgent: Boolean = false,
                         backLink: Option[String] = None,
                         hasSignOutLink: Boolean = false,
                         errors: Option[Seq[(String, String)]] = None) {

    val document: Document = Jsoup.parse(view.body)

    private val titlePrefix: String = if (errors.isDefined) "Error: " else ""
    private val titleSuffix: String = if (isAgent) {
      " - Use software to report your client’s Income Tax - GOV.UK"
    } else {
      " - Use software to send Income Tax updates - GOV.UK"
    }

    document.title mustBe s"$titlePrefix$title$titleSuffix"

    backLink.map { href =>
      val link = document.selectHead(".govuk-back-link")
      link.text mustBe "Back"
      link.attr("href") mustBe href
    }

    if (hasSignOutLink) {
      val signOutLink: Element = document.selectHead(".hmrc-sign-out-nav__link")
      signOutLink.text mustBe "Sign out"
      signOutLink.attr("href") mustBe routes.SignOutController.signOut.url
    } else {
      document.selectOptionally(".hmrc-sign-out-nav__link") mustBe None
    }

    errors.map { errorList =>
      val errorSummary: Element = document.selectHead(".govuk-error-summary")
      errorSummary.selectHead("h2").text mustBe "There is a problem"

      errorList.zip(1 to errorList.length).map { case ((errorKey, errorMessage), index) =>
        val errorLink: Element = errorSummary.selectNth("a", index)
        errorLink.text mustBe errorMessage
        errorLink.attr("href") mustBe s"#$errorKey"
      }
    }

  }

  implicit class CustomSelectors(element: Element) {

    def selectHead(selector: String): Element = {
      selectOptionally(selector) match {
        case Some(element) => element
        case None => fail(s"No elements returned for selector: $selector")
      }
    }

    def selectOptionally(selector: String): Option[Element] = {
      selectSeq(selector).headOption
    }

    def selectNth(selector: String, nth: Int): Element = {
      element.selectSeq(selector).lift(nth - 1) match {
        case Some(element) => element
        case None => fail(s"$selector number $nth was not found")
      }
    }

    def selectSeq(selector: String): Seq[Element] = {
      element.select(selector).asScala.toSeq
    }

    def content: Element = element.getElementsByTag("article").asScala.head

    def mainContent: Element = element.selectHead("main")

    def getParagraphs: Elements = element.getElementsByTag("p")

    def getBulletPoints: Elements = element.getElementsByTag("li")

    def getH1Element: Elements = element.getElementsByTag("h1")

    def getHeader: Element = element.selectHead("header")

    def getH2Elements: Elements = element.getElementsByTag("h2")

    def getH2Element(nth: Int = 1): Element = element.selectHead(s"h2:nth-of-type($nth)")

    def getFormElements: Elements = element.getElementsByClass("form-field-group")

    def getErrorSummaryMessage: Elements = element.select("#error-summary-display ul")

    def getErrorSummary: Elements = element.select("#error-summary-display")

    def getErrorSummaryByNewGovUkClass: Elements = element.select(".govuk-error-summary")

    def getSubmitButton: Elements = element.select("button[type=submit]")

    def getGovukButton: Element = element.selectHead("button[class=govuk-button]")

    def getButtonByClass: String = element.select(s"""[class=govuk-button]""").text()

    def getHintText: String = element.select(s"""[class=form-hint]""").text()

    def getHintTextByClass: String = element.select(s"""[class=govuk-hint]""").text()

    def getFieldset: Element = element.selectHead("fieldset")

    def getForm: Element = element.selectHead("form")

    def getBackLink: Elements = element.select(s"a[class=link-back]")

    def getBackLinkByClass: Elements = element.select(s"a[class=govuk-back-link]")

    def getParagraphNth(index: Int = 0): String = {
      element.select("p").get(index).text()
    }

    def getBulletPointNth(index: Int = 0): String = element.select("ul[class=bullets] li").get(index).text()

    def getRadioButtonByIndex(index: Int = 0): Element = element.select("div .multiple-choice").get(index)

    def getGovukRadioButtonByIndex(index: Int = 0): Element = element.select(".govuk-radios__item").get(index)

    def getSpan(id: String): Elements = element.select(s"""span[id=$id]""")

    def getLink(id: String): Elements = element.select(s"""a[id=$id]""")

    def getTextFieldInput(id: String): Elements = element.select(s"""input[id=$id]""")

    def getFieldErrorMessage(id: String): Elements = element.select(s"""a[id=$id-error-summary]""")

    //Check your answers selectors
    def getSummaryList(nth: Int = 1): Element = element.selectHead(s"dl.govuk-summary-list:nth-of-type($nth)")

    def getSummaryListRow(nth: Int): Element = {
      element.selectHead(s"div.govuk-summary-list__row:nth-of-type($nth)")
    }

    def getSummaryListKey: Element = element.selectHead("dt.govuk-summary-list__key")

    def getSummaryListValue: Element = element.selectHead("dd.govuk-summary-list__value")

    def getSummaryListActions: Element = element.selectHead("dd.govuk-summary-list__actions")

  }

  case class SummaryListActionValues(href: String, text: String, visuallyHidden: String)

  case class SummaryListRowValues(key: String, value: Option[String], actions: Seq[SummaryListActionValues])

  case class DateInputFieldValues(label: String, value: Option[String])

  implicit class ElementTests(element: Element) {

    def mustHaveSummaryList(selector: String)(rows: Seq[SummaryListRowValues]): Assertion = {
      val checkpoint: Checkpoint = new Checkpoint()

      val summaryList = element.selectHead(selector)

      rows.zip(1 to rows.length) foreach { case (rowData, rowIndex) =>
        val row = summaryList.selectHead(s".govuk-summary-list__row:nth-of-type($rowIndex)")

        checkpoint {
          row.selectHead("dt.govuk-summary-list__key").text mustBe rowData.key
        }

        checkpoint {
          row.selectHead("dd.govuk-summary-list__value").text mustBe rowData.value.getOrElse("")
        }

        rowData.actions match {
          case Nil =>
            checkpoint {
              row.selectOptionally("dd.govuk-summary-list__actions") mustBe None
            }
          case actionValues :: Nil =>
            val link = row.selectHead("dd.govuk-summary-list__actions").selectHead("a")

            checkpoint {
              link.attr("href") mustBe actionValues.href
            }
            checkpoint {
              link.text mustBe actionValues.text
            }
            checkpoint {
              link.selectHead("span.govuk-visually-hidden").text mustBe actionValues.visuallyHidden
            }
          case actionsValues =>
            actionsValues.zip(1 to actionsValues.length) foreach { case (actionValues, actionIndex) =>
              val link = row.selectHead("dd.govuk-summary-list__actions").selectHead(s"a:nth-of-type($actionIndex)")
              checkpoint {
                link.attr("href") mustBe actionValues.href
              }
              checkpoint {
                link.text mustBe actionValues.text
              }
              checkpoint {
                link.selectHead("span.govuk-visually-hidden").text mustBe actionValues.visuallyHidden
              }
            }
        }
      }

      checkpoint.reportAll()
      Succeeded
    }

    def mustHaveRadioInput(selector: String)(name: String,
                                             legend: String,
                                             isHeading: Boolean,
                                             isLegendHidden: Boolean,
                                             hint: Option[String],
                                             errorMessage: Option[String],
                                             radioContents: Seq[RadioItem],
                                             isInline: Boolean = false): Assertion = {

      val checkpoint: Checkpoint = new Checkpoint()
      val radioFieldSet: Element = element.selectHead(selector)


      validateFieldSetLegend(radioFieldSet, legend, isHeading, isLegendHidden, checkpoint)

      hint.foreach { hint =>
        val radioFieldSetHint: Element = radioFieldSet.selectHead(".govuk-hint")
        checkpoint {
          radioFieldSet.attr("aria-describedby") must include(radioFieldSetHint.attr("id"))
        }
        checkpoint {
          radioFieldSetHint.text mustBe hint
        }
      }

      errorMessage.foreach { errorMessage =>
        val radioFieldSetError: Element = radioFieldSet.selectHead(".govuk-error-message")
        checkpoint {
          radioFieldSet.attr("aria-describedby") must include(radioFieldSetError.attr("id"))
        }
        checkpoint {
          radioFieldSetError.text must include(errorMessage)
        }
      }

      val radioField: Element = if (isInline) element.selectHead(".govuk-radios--inline") else element.selectHead(".govuk-radios")

      radioContents.zipWithIndex foreach { case (radioContent, index) =>
        if (radioContent.divider.isDefined) {
          validateRadioDivider(radioField, radioContent, index, checkpoint)
        } else {
          validateRadioItem(radioField, name, radioContent, index, checkpoint)
        }
      }
      checkpoint.reportAll()
      Succeeded
    }

    private def validateFieldSetLegend(radioFieldSet: Element,
                                       legend: String,
                                       isHeading: Boolean,
                                       isLegendHidden: Boolean,
                                       checkpoint: Checkpoint): Unit = {
      val radioFieldSetLegend: Element = radioFieldSet.selectHead("legend")
      if (isHeading) {
        checkpoint {
          radioFieldSetLegend.getH1Element.text mustBe legend
        }
      } else {
        checkpoint {
          radioFieldSetLegend.text mustBe legend
        }
        if (isLegendHidden) {
          checkpoint {
            radioFieldSetLegend.attr("class") must include("govuk-visually-hidden")
          }
        } else {
          checkpoint {
            radioFieldSetLegend.attr("class") mustNot include("govuk-visually-hidden")
          }
        }
      }
    }

    private def validateRadioItem(radioField: Element, name: String, radioItem: RadioItem, index: Int, checkpoint: Checkpoint): Unit = {
      val radioItemElement: Element = radioField.child(index)
      val radioInput: Element = radioItemElement.selectHead("input")
      val radioLabel: Element = radioItemElement.selectHead("label")
      val radioInputId: String = if (index == 0) name else s"$name-${index + 1}"

      checkpoint {
        radioItemElement.className() mustBe "govuk-radios__item"
      }
      checkpoint {
        radioInput.attr("id") mustBe radioInputId
      }
      checkpoint {
        radioInput.attr("name") mustBe name
      }
      checkpoint {
        radioInput.attr("type") mustBe "radio"
      }
      checkpoint {
        radioInput.attr("value") mustBe radioItem.value.getOrElse("")
      }
      checkpoint {
        radioLabel.attr("for") mustBe radioInput.attr("id")
      }
      checkpoint {
        Text(radioLabel.text) mustBe radioItem.content
      }
      radioItem.hint.foreach { hint =>
        checkpoint {
          Text(radioItemElement.selectHead(".govuk-radios__hint").text) mustBe hint.content
        }
      }
    }

    private def validateRadioDivider(radioField: Element, radioDivider: RadioItem, index: Int, checkpoint: Checkpoint): Unit = {
      val dividerElement: Element = radioField.child(index)
      checkpoint {
        dividerElement.className() mustBe "govuk-radios__divider"
      }
      checkpoint {
        dividerElement.text() mustBe radioDivider.divider.get
      }
    }

    def mustHaveTextInput(selector: String = ".govuk-form-group")(name: String,
                          label: String,
                          isLabelHidden: Boolean,
                          isPageHeading: Boolean,
                          hint: Option[String] = None,
                          error: Option[String] = None,
                          autoComplete: Option[String] = None): Assertion = {
      val checkpoint: Checkpoint = new Checkpoint
      val formGroup: Element = element.selectHead(selector)
      val textInput: Element = formGroup.selectHead(s"input[name=$name]")

      validateTextInputLabel(name, label, isPageHeading, isLabelHidden, checkpoint)

      checkpoint {
        textInput.attr("type") mustBe "text"
      }
      checkpoint {
        textInput.attr("name") mustBe name
      }

      autoComplete.foreach(value =>
        checkpoint {
          textInput.attr("autocomplete") mustBe value
        })

      hint.foreach { value =>
        checkpoint {
          element.selectHead(s"#$name-hint").text mustBe value
        }
        checkpoint {
          textInput.attr("aria-describedby") must include(s"$name-hint")
        }
      }

      error.foreach { errorMessage =>
        checkpoint {
          element.selectHead(s"#$name-error").text mustBe s"Error: $errorMessage"
        }
        checkpoint {
          textInput.attr("aria-describedby") must include(s"$name-error")
        }
      }

      checkpoint.reportAll()
      Succeeded
    }

    def validateTextInputLabel(name: String,
                               label: String,
                               isPageHeading: Boolean,
                               isLabelHidden: Boolean,
                               checkpoint: Checkpoint): Unit = {
      val textInputLabel: Element = element.selectHead(s"label[for=$name]")

      checkpoint {
        textInputLabel.text mustBe label
      }
      checkpoint {
        textInputLabel.attr("for") mustBe name
      }
      checkpoint {
        textInputLabel.className() must include("govuk-label")
      }

      if (isPageHeading) {
        checkpoint {
          textInputLabel.className() must include("govuk-label--l")
        }
      } else if (isLabelHidden) {
        checkpoint {
          textInputLabel.className() must include("govuk-visually-hidden")
        }
      } else {
        checkpoint {
          textInputLabel.className() must include("govuk-!-font-weight-bold")
        }
      }
    }


    def listErrorMessages(errors: List[String]): Assertion = {
      errors.zipWithIndex.map {
        case (error, index) => element.selectHead(s"div.error-notification:nth-of-type(${index + 1})").text mustBe s"Error: $error"
      } forall (_ == succeed) mustBe true
    }

    def mustHaveDateInput(id: String,
                          legend: String,
                          exampleDate: String,
                          isHeading: Boolean,
                          isLegendHidden: Boolean,
                          errorMessage: Option[String] = None,
                          dateInputsValues: Seq[DateInputFieldValues]): Assertion = {

      val checkpoint: Checkpoint = new Checkpoint()

      val dateInputField: Element = element.selectHead(s"#$id")

      val dateLegend: Element = element.selectHead(".govuk-fieldset__legend")
      if (isHeading) {
        checkpoint {
          dateLegend.getH1Element.text mustBe legend
        }
      } else {
        checkpoint {
          dateLegend.text mustBe legend
        }
        if (isLegendHidden) {
          checkpoint {
            dateLegend.attr("class") must include("govuk-visually-hidden")
          }
        }
      }

      val hintText: Element = element.selectHead(".govuk-hint")
      checkpoint {
        hintText.text mustBe exampleDate
      }

      dateInputsValues zip (1 to dateInputsValues.length) foreach { case (dateInputValues, index) =>
        val item: Element = dateInputField.selectNth(".govuk-date-input__item", index)
        val label = item.selectHead("label")
        val input = item.selectHead("input")

        checkpoint {
          label.text mustBe dateInputValues.label
        }
        checkpoint {
          label.attr("for") mustBe input.id
        }
        checkpoint {
          input.id mustBe input.attr("name")
        }
        checkpoint {
          input.attr("type") mustBe "text"
        }
        checkpoint {
          input.attr("inputmode") mustBe "numeric"
        }

        dateInputValues.value foreach { value =>
          input.attr("value") mustBe value
        }

      }

      errorMessage foreach { message =>

        val fieldset: Element = element.getFieldset
        val errorMessage: Element = element.selectHead(".govuk-error-message")
        checkpoint {
          fieldset.attr("aria-describedby") must include("startDate-error")
        }
        checkpoint {
          errorMessage.selectHead("p").attr("id") mustBe "startDate-error"
        }
        checkpoint {
          errorMessage.text mustBe s"Error: $message"
        }
      }

      checkpoint.reportAll()
      Succeeded

    }

    def mustHavePara(paragraph: String): Assertion = {
      element.getElementsByTag("p").text() must include(paragraph)
    }

    def mustHaveErrorSummary(errors: List[String]): Assertion = {
      element.getErrorSummary.attr("class") mustBe "flash error-summary error-summary--show"
      element.getErrorSummary.attr("role") mustBe "alert"
      element.getErrorSummary.attr("aria-labelledby") mustBe "govuk-error-summary"
      element.getErrorSummary.attr("tabindex") mustBe "-1"
      element.getErrorSummary.select("h2").attr("id") mustBe "govuk-error-summary"
      element.getErrorSummary.select("h2").text mustBe "There is a problem"
      element.getErrorSummary.select("ul > li").text mustBe errors.mkString(" ")
    }

    def mustHaveErrorSummaryByNewGovUkClass(errors: List[String]): Assertion = {
      element.getErrorSummaryByNewGovUkClass.select("div").attr("role") mustBe "alert"
      element.getErrorSummaryByNewGovUkClass.select("h2").text mustBe "There is a problem"
      element.getErrorSummaryByNewGovUkClass.select("ul > li").text mustBe errors.mkString(" ")
    }

    def mustHaveErrorNotificationMessage(error: String): Assertion = {
      element.selectHead(s"div.error-notification").text mustBe s"Error: $error"
    }

    def mustHaveGovUkErrorNotificationMessage(error: String): Assertion = {
      element.selectHead(s".govuk-error-message").text mustBe s"Error: $error"
    }

    def mustHaveHeadingAndCaption(heading: String, caption: String, isSection: Boolean): Assertion = {
      val checkpoint: Checkpoint = new Checkpoint()

      checkpoint {
        element.selectHead("h1.govuk-heading-l").text mustBe heading
      }

      if (isSection) {
        checkpoint {
          element.selectHead(".govuk-caption-l").text mustBe s"This section is $caption"
        }
        checkpoint {
          element.selectHead(".govuk-caption-l").selectHead("span.govuk-visually-hidden").text mustBe "This section is"
        }
      } else {
        checkpoint {
          element.selectHead("h2.govuk-caption-l").text mustBe caption
        }
      }

      checkpoint.reportAll()
      Succeeded
    }


  }

}
