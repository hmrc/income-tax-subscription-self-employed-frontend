/*
 * Copyright 2021 HM Revenue & Customs
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

import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.scalatest.{Assertion, MustMatchers, WordSpec}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Call
import play.api.test.FakeRequest
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig

import scala.collection.JavaConversions._

trait ViewSpec extends WordSpec with MustMatchers with GuiceOneAppPerSuite {

  implicit val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  implicit lazy val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  implicit lazy val mockMessages: Messages = messagesApi.preferred(FakeRequest())

  val testBackUrl = "/test-back-url"
  val testCall: Call = Call("POST", "/test-url")
  val fakeTestRequest = FakeRequest("POST", "/test-url")

  implicit class CustomSelectors(element: Element) {

    def selectHead(selector: String): Element = {
      element.select(selector).headOption match {
        case Some(element) => element
        case None => fail(s"No elements returned for selector: $selector")
      }
    }

    def content: Element = element.getElementsByTag("article").head

    def getParagraphs: Elements = element.getElementsByTag("p")

    def getBulletPoints: Elements = element.getElementsByTag("li")

    def getH1Element: Elements = element.getElementsByTag("h1")

    def getH2Elements: Elements = element.getElementsByTag("h2")

    def getH2Element(nth: Int = 1): Element = element.selectHead(s"h2:nth-of-type($nth)")

    def getFormElements: Elements = element.getElementsByClass("form-field-group")

    def getErrorSummaryMessage: Elements = element.select("#error-summary-display ul")

    def getErrorSummary: Elements = element.select("#error-summary-display")

    def getSubmitButton: Elements = element.select("button[type=submit]")

    def getHintText: String = element.select(s"""[class=form-hint]""").text()

    def getForm: Elements = element.select("form")

    def getBackLink: Elements = element.select(s"a[class=link-back]")

    def getParagraphNth(index: Int = 0): String = {
      element.select("p").get(index).text()
    }

    def getBulletPointNth(index: Int = 0): String = element.select("ul[class=bullets] li").get(index).text()

    def getRadioButtonByIndex(index: Int = 0): Element = element.select("div .multiple-choice").get(index)

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

  implicit class ElementTests(element: Element) {

    def mustHaveTextField(name: String, label: String): Assertion = {
      val eles = element.select(s"input[name=$name]")
      if (eles.isEmpty) fail(s"$name does not have an input field with name=$name\ncurrent list of inputs:\n[${element.select("input")}]")
      if (eles.size() > 1) fail(s"$name have multiple input fields with name=$name")
      val ele = eles.head
      ele.attr("type") mustBe "text"
      element.select(s"label[for=$name]").text() mustBe label
    }

    def listErrorMessages(errors: List[String]): Assertion = {
      errors.zipWithIndex.map {
        case (error, index) => element.selectHead(s"div.error-notification:nth-of-type(${index + 1})").text mustBe s"Error: $error"
      } forall (_ == succeed) mustBe true
    }

    def mustHaveDateField(id: String, legend: String, exampleDate: String, error: Option[String] = None): Assertion = {
      val ele = element.getElementById(id)
      ele.attr("aria-describedby") mustBe s"$id-hint${error.map(_ => s" $id-error").getOrElse("")}"
      ele.selectHead("legend").text mustBe legend
      ele.selectHead(s"div.form-hint[id=$id-hint]").text mustBe exampleDate
      ele.tag().toString mustBe "fieldset"
      mustHaveTextField(s"$id.dateDay", "Day")
      mustHaveTextField(s"$id.dateMonth", "Month")
      mustHaveTextField(s"$id.dateYear", "Year")
      error.map { message =>
        ele.selectHead(s"div[id=$id-error]").text mustBe s"Error: $message"
      }.getOrElse(succeed)
    }

    def mustHavePara(paragraph: String): Assertion = {
      element.getElementsByTag("p").text() must include(paragraph)
    }

    def mustHaveErrorSummary(errors: List[String]): Assertion = {
      element.getErrorSummary.attr("class") mustBe "flash error-summary error-summary--show"
      element.getErrorSummary.attr("role") mustBe "alert"
      element.getErrorSummary.attr("aria-labelledby") mustBe "error-summary-heading"
      element.getErrorSummary.attr("tabindex") mustBe "-1"
      element.getErrorSummary.select("h2").attr("id") mustBe "error-summary-heading"
      element.getErrorSummary.select("h2").text mustBe "There is a problem"
      element.getErrorSummary.select("ul > li").text mustBe errors.mkString(" ")
    }

    def mustHaveErrorNotificationMessage(error: String): Assertion = {
      element.selectHead(s"div.error-notification").text mustBe s"Error: $error"
    }


  }

}
