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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.formatters

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.data.FormError
import play.api.data.format.Formatter
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.DateModel

class DateModelFormatterSpec extends AnyWordSpecLike with Matchers {

  def dateModelFormatter(isAgent: Boolean = false): Formatter[DateModel] = DateModelMapping.DateModelFormatter(
    isAgent = isAgent,
    minDate = None,
    maxDate = None,
    dateFormatter = None,
    errorContext = "test")

  val bindingKey: String = "testKey"

  private val dayKey = s"$bindingKey-${DateModelMapping.day}"
  private val monthKey = s"$bindingKey-${DateModelMapping.month}"
  private val yearKey = s"$bindingKey-${DateModelMapping.year}"

  def inputMap(day: Option[String], month: Option[String], year: Option[String]): Map[String, String] = List(
    dayKey -> day,
    monthKey -> month,
    yearKey -> year
  ).collect {
    case (key, Some(value)) => (key, value)
  }.toMap

  def errorKey(isAgent: Boolean)(error: String): String = if (isAgent) s"agent.error.$error" else s"error.$error"

  "dateModelFormatter.bind" when {
    for (isAgent <- Seq(true, false)) {
      s"isAgent is set to $isAgent" should {
        "return a date model" when {
          "a valid day, month and year are present" in {
            val result = dateModelFormatter(isAgent).bind(bindingKey, inputMap(day = Some("20"), month = Some("10"), year = Some("2020")))
            result mustBe Right(DateModel(day = "20", month = "10", year = "2020"))
          }
          "the date is 29th of feburary and it's a leap year" in {
            val result = dateModelFormatter(isAgent).bind(bindingKey, inputMap(day = Some("29"), month = Some("2"), year = Some("2020")))
            result mustBe Right(DateModel(day = "29", month = "2", year = "2020"))
          }
        }
        "return an error" when {
          "the day field is empty" in {
            val result = dateModelFormatter(isAgent).bind(bindingKey, inputMap(day = Some(""), month = Some("2"), year = Some("2020")))
            result mustBe Left(Seq(FormError(dayKey, errorKey(isAgent)("test.day.empty"))))
          }
          "the day field is not present" in {
            val result = dateModelFormatter(isAgent).bind(bindingKey, inputMap(day = None, month = Some("2"), year = Some("2020")))
            result mustBe Left(Seq(FormError(dayKey, errorKey(isAgent)("test.day.empty"))))
          }
          "the month field is empty" in {
            val result = dateModelFormatter(isAgent).bind(bindingKey, inputMap(day = Some("20"), month = Some(""), year = Some("2020")))
            result mustBe Left(Seq(FormError(monthKey, errorKey(isAgent)("test.month.empty"))))
          }
          "the month field is not present" in {
            val result = dateModelFormatter(isAgent).bind(bindingKey, inputMap(day = Some("20"), month = None, year = Some("2020")))
            result mustBe Left(Seq(FormError(monthKey, errorKey(isAgent)("test.month.empty"))))
          }
          "the year field is empty" in {
            val result = dateModelFormatter(isAgent).bind(bindingKey, inputMap(day = Some("20"), month = Some("10"), year = Some("")))
            result mustBe Left(Seq(FormError(yearKey, errorKey(isAgent)("test.year.empty"))))
          }
          "the year field is not present" in {
            val result = dateModelFormatter(isAgent).bind(bindingKey, inputMap(day = Some("20"), month = Some("10"), year = None))
            result mustBe Left(Seq(FormError(yearKey, errorKey(isAgent)("test.year.empty"))))
          }
          "the day and month fields are empty" in {
            val result = dateModelFormatter(isAgent).bind(bindingKey, inputMap(day = None, month = None, year = Some("2020")))
            result mustBe Left(Seq(FormError(dayKey, errorKey(isAgent)("test.day_month.empty"))))
          }
          "the day and year fields are empty" in {
            val result = dateModelFormatter(isAgent).bind(bindingKey, inputMap(day = None, month = Some("2"), year = None))
            result mustBe Left(Seq(FormError(dayKey, errorKey(isAgent)("test.day_year.empty"))))
          }
          "the month and year fields are empty" in {
            val result = dateModelFormatter(isAgent).bind(bindingKey, inputMap(day = Some("20"), month = None, year = None))
            result mustBe Left(Seq(FormError(monthKey, errorKey(isAgent)("test.month_year.empty"))))
          }
          "all fields are empty" in {
            val result = dateModelFormatter(isAgent).bind(bindingKey, inputMap(day = None, month = None, year = None))
            result mustBe Left(Seq(FormError(dayKey, errorKey(isAgent)("test.day_month_year.empty"))))
          }
          "the day field is invalid" when {
            "the day is text" in {
              val result = dateModelFormatter(isAgent).bind(bindingKey, inputMap(day = Some("invalid"), month = Some("10"), year = Some("2020")))
              result mustBe Left(Seq(FormError(dayKey, errorKey(isAgent)("test.day.invalid"))))
            }
            "the day is 0" in {
              (1 to 12) map { month =>
                val result = dateModelFormatter(isAgent).bind(bindingKey, inputMap(day = Some("0"), month = Some(month.toString), year = Some("2020")))
                result mustBe Left(Seq(FormError(dayKey, errorKey(isAgent)("test.day.invalid"))))
              }
            }
            "it's not a leap year, the month is february and day is 29th" in {
              val result = dateModelFormatter(isAgent).bind(bindingKey, inputMap(day = Some("29"), month = Some("2"), year = Some("2019")))
              result mustBe Left(Seq(FormError(dayKey, errorKey(isAgent)("test.day.invalid"))))
            }
            "its the 31st of month without 31 days" in {
              List(4, 6, 9, 11) map { month =>
                val result = dateModelFormatter(isAgent).bind(bindingKey, inputMap(day = Some("31"), month = Some(month.toString), year = Some("2020")))
                result mustBe Left(Seq(FormError(dayKey, errorKey(isAgent)("test.day.invalid"))))
              }
            }
            "its the 32nd of the months with only 31 days" in {
              List(1, 3, 5, 7, 8, 10, 12) map { month =>
                val result = dateModelFormatter(isAgent).bind(bindingKey, inputMap(day = Some("32"), month = Some(month.toString), year = Some("2020")))
                result mustBe Left(Seq(FormError(dayKey, errorKey(isAgent)("test.day.invalid"))))
              }
            }
          }
          "the month field is invalid" when {
            "the month is text" in {
              val result = dateModelFormatter(isAgent).bind(bindingKey, inputMap(day = Some("20"), month = Some("invalid"), year = Some("2020")))
              result mustBe Left(Seq(FormError(monthKey, errorKey(isAgent)("test.month.invalid"))))
            }
            "the month is 0" in {
              val result = dateModelFormatter(isAgent).bind(bindingKey, inputMap(day = Some("20"), month = Some("0"), year = Some("2020")))
              result mustBe Left(Seq(FormError(monthKey, errorKey(isAgent)("test.month.invalid"))))
            }
            "the month is 13" in {
              val result = dateModelFormatter(isAgent).bind(bindingKey, inputMap(day = Some("20"), month = Some("13"), year = Some("2020")))
              result mustBe Left(Seq(FormError(monthKey, errorKey(isAgent)("test.month.invalid"))))
            }
          }
          "the year field is invalid" when {
            "the year is text" in {
              val result = dateModelFormatter(isAgent).bind(bindingKey, inputMap(day = Some("20"), month = Some("10"), year = Some("invalid")))
              result mustBe Left(Seq(FormError(yearKey, errorKey(isAgent)("test.year.invalid"))))
            }
          }
        }
      }
    }
  }

  "dateModelFormatter.unbind" must {
    "return a map of values in the date model relating to the original inputs" in {
      dateModelFormatter().unbind(bindingKey, DateModel("20", "10", "2020")) mustBe Map(
        dayKey -> "20",
        monthKey -> "10",
        yearKey -> "2020"
      )
    }
  }

}

