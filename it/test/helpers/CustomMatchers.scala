/*
 * Copyright 2022 HM Revenue & Customs
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

package helpers

import org.jsoup.Jsoup
import org.scalatest.matchers._
import play.api.libs.json.Reads
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.DateModel

trait CustomMatchers {

  def httpStatus(expectedValue: Int): HavePropertyMatcher[WSResponse, Int] =
    (response: WSResponse) => HavePropertyMatchResult(
      response.status == expectedValue,
      "httpStatus",
      expectedValue,
      response.status
    )

  def jsonBodyAs[T](expectedValue: T)(implicit reads: Reads[T]): HavePropertyMatcher[WSResponse, T] =
    (response: WSResponse) => HavePropertyMatchResult(
      response.json.as[T] == expectedValue,
      "jsonBodyAs",
      expectedValue,
      response.json.as[T]
    )

  val emptyBody: HavePropertyMatcher[WSResponse, String] =
    (response: WSResponse) => HavePropertyMatchResult(
      response.body == "",
      "emptyBody",
      "",
      response.body
    )

  def pageTitle(expectedValue: String): HavePropertyMatcher[WSResponse, String] =
    (response: WSResponse) => {
      val body = Jsoup.parse(response.body)

      HavePropertyMatchResult(
        body.title == expectedValue,
        "pageTitle",
        expectedValue,
        body.title
      )
    }

  def elementTextByClass(cssClass: String)(expectedValue: String): HavePropertyMatcher[WSResponse, String] = {
    (response: WSResponse) => {
      val body = Jsoup.parse(response.body)

      val elementText = body.getElementsByClass(cssClass).first().text
      HavePropertyMatchResult(
        elementText == expectedValue,
        s"elementByID($cssClass)",
        expectedValue,
        elementText
      )
    }
  }

  def elementSecondTextByClass(cssClass: String)(expectedValue: String): HavePropertyMatcher[WSResponse, String] = {
    (response: WSResponse) => {
      val body = Jsoup.parse(response.body)

      val elementText = body.getElementsByClass(cssClass).get(1).text
      HavePropertyMatchResult(
        elementText == expectedValue,
        s"elementByID($cssClass)",
        expectedValue,
        elementText
      )
    }
  }

  def dateField(id: String, expectedValue: DateModel): HavePropertyMatcher[WSResponse, String] =
    (response: WSResponse) => {
      val body = Jsoup.parse(response.body)
      val day = body.getElementById(s"$id-dateDay").`val`()
      val month = body.getElementById(s"$id-dateMonth").`val`()
      val year = body.getElementById(s"$id-dateYear").`val`()
      HavePropertyMatchResult(
        day == expectedValue.day && month == expectedValue.month && year == expectedValue.year,
        "day",
        expectedValue.toString,
        day + " / " + month + " / " + year
      )
    }

  def textField(id: String, expectedValue: String): HavePropertyMatcher[WSResponse, String] =
    (response: WSResponse) => {
      val body = Jsoup.parse(response.body)
      val text = body.getElementById(id).`val`()
      HavePropertyMatchResult(
        text == expectedValue,
        "text field",
        expectedValue,
        text
      )
    }

  def radioButtonSet(id: String, selectedRadioButton: Option[String]): HavePropertyMatcher[WSResponse, String] =
    (response: WSResponse) => {
      val body = Jsoup.parse(response.body)
      val radios = body.select(s"input[id^=$id]")
      val checkedAttr = "checked"

      def textForSelectedButton(idForSelectedRadio: String) =
        if (idForSelectedRadio.isEmpty) ""
        else body.select(s"label[for=$idForSelectedRadio]").text()

      val matchCondition = selectedRadioButton match {
        case Some(expectedOption) =>
          val idForSelectedRadio = radios.select(s"input[checked]").attr("id")
          textForSelectedButton(idForSelectedRadio) == expectedOption
        case None => !radios.hasAttr(checkedAttr)
      }

      HavePropertyMatchResult(
        matches = matchCondition,
        propertyName = "radioButton",
        expectedValue = selectedRadioButton.fold("")(identity),
        actualValue = {
          val selected = radios.select("input[checked]")
          selected.size() match {
            case 0 =>
              "no radio button is selected"
            case 1 =>
              val idForSelectedRadio = selected.attr("id")
              s"""The "${textForSelectedButton(idForSelectedRadio)}" selected"""
            case _ =>
              s"multiple radio buttons are selected: [$radios]"
          }
        }
      )
    }

  def govukRadioButtonSet(id: String, expectedLabel: String, expectedHint: String): HavePropertyMatcher[WSResponse, String] =
    (response: WSResponse) => {
      val body = Jsoup.parse(response.body)
      val radios = body.select(s"input[id^=$id]")

      def labelForSelectedButton(idForSelectedRadio: String) =
        if (idForSelectedRadio.isEmpty) ""
        else body.select(s"label[for=$idForSelectedRadio]").text()

      def hintForSelectedButton(idForSelectedRadio: String) =
        if (idForSelectedRadio.isEmpty) ""
        else body.select(s"#$idForSelectedRadio-item-hint").text()

      val idForSelectedRadio = radios.select(s"input[checked]").attr("id")
      val matchCondition =
        labelForSelectedButton(idForSelectedRadio) == expectedLabel && hintForSelectedButton(idForSelectedRadio) == expectedHint

      HavePropertyMatchResult(
        matches = matchCondition,
        propertyName = "radioButton",
        expectedValue = s"$expectedLabel $expectedHint",
        actualValue = {
          val selected = radios.select("input[checked]")
          selected.size() match {
            case 0 =>
              "no radio button is selected"
            case 1 =>
              val idForSelectedRadio = selected.attr("id")
              s"${labelForSelectedButton(idForSelectedRadio)} ${hintForSelectedButton(idForSelectedRadio)}"
            case _ =>
              s"multiple radio buttons are selected: [$radios]"
          }
        }
      )
    }

  def redirectURI(expectedValue: String): HavePropertyMatcher[WSResponse, String] =
    (response: WSResponse) => {
      val redirectLocation: Option[String] = response.header("Location")

      val matchCondition = redirectLocation.exists(_.contains(expectedValue))
      HavePropertyMatchResult(
        matchCondition,
        "redirectUri",
        expectedValue,
        redirectLocation.getOrElse("")
      )
    }

}