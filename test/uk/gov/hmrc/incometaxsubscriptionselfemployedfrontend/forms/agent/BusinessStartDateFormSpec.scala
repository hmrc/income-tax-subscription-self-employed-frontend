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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent

import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.{Form, FormError}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.BusinessStartDateForm.{businessStartDateForm, startDate}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.formatters.DateModelMapping._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.validation.testutils.DataMap.DataMap
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.DateModel
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.AccountingPeriodUtil

import java.time.LocalDate

class BusinessStartDateFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  def form: Form[DateModel] = {
    businessStartDateForm(BusinessStartDateForm.minStartDate, BusinessStartDateForm.maxStartDate, d => d.toString)
  }

  "The Business Start Date Form" should {
    "transform a valid request to the date form case class" in {

      val validDate: LocalDate = LocalDate.now

      val testInput = Map(
        s"$startDate-$day" -> validDate.getDayOfMonth.toString,
        s"$startDate-$month" -> validDate.getMonthValue.toString,
        s"$startDate-$year" -> validDate.getYear.toString
      )

      val expected = DateModel(
        validDate.getDayOfMonth.toString,
        validDate.getMonthValue.toString,
        validDate.getYear.toString
      )

      val actual = form.bind(testInput).value

      actual shouldBe Some(expected)
    }

    "when testing the validation" should {

      "output the appropriate error messages for the start date" when {
        val empty = "agent.error.business-start-date.day-month-year.empty"
        val beforeMax = "agent.error.business-start-date.day-month-year.max-date"
        val beforeMin = "agent.error.business-start-date.day-month-year.min-date"

        val dayKeyError: String = s"$startDate-$day"
        val monthKeyError: String = s"$startDate-$month"
        val yearKeyError: String = s"$startDate-$year"

        val errorContext: String = "agent.error.business-start-date"

        "the date is not supplied to the map" in {
          form.bind(DataMap.EmptyMap).errors must contain(FormError(dayKeyError, empty))
        }
        "it is not within 7 days from the current date" in {
          val sevenDaysInFuture: LocalDate = LocalDate.now.plusDays(7)
          val maxTest = form.bind(DataMap.date(startDate)(
            sevenDaysInFuture.getDayOfMonth.toString,
            sevenDaysInFuture.getMonthValue.toString,
            sevenDaysInFuture.getYear.toString
          ))
          maxTest.errors must contain(FormError(dayKeyError, beforeMax, Seq(BusinessStartDateForm.maxStartDate.toString)))
        }

        "it is before 1900" in {
          val minTest = form.bind(DataMap.date(startDate)("31", "12", "1899"))
          minTest.errors must contain(FormError(dayKeyError, beforeMin, Seq(BusinessStartDateForm.minStartDate.toString)))
        }
        "it is missing the day" in {
          val test = form.bind(DataMap.date(startDate)("", "4", "2017"))
          test.errors must contain(FormError(dayKeyError, s"$errorContext.day.empty"))
        }
        "it is missing the month" in {
          val test = form.bind(DataMap.date(startDate)("1", "", "2017"))
          test.errors must contain(FormError(monthKeyError, s"$errorContext.month.empty"))
        }
        "it is missing the year" in {
          val test = form.bind(DataMap.date(startDate)("1", "1", ""))
          test.errors must contain(FormError(yearKeyError, s"$errorContext.year.empty"))
        }
        "it is missing multiple fields" in {
          val test = form.bind(DataMap.date(startDate)("", "", "2017"))
          test.errors must contain(FormError(dayKeyError, s"$errorContext.day-month.empty"))
        }
        "it has an invalid day" in {
          val test = form.bind(DataMap.date(startDate)("0", "1", "2017"))
          test.errors must contain(FormError(dayKeyError, s"$errorContext.day.invalid"))
        }
        "it has an invalid month" in {
          val test = form.bind(DataMap.date(startDate)("1", "13", "2017"))
          test.errors must contain(FormError(monthKeyError, s"$errorContext.month.invalid"))
        }
        "it has an invalid year" in {
          val test = form.bind(DataMap.date(startDate)("1", "1", "invalid"))
          test.errors must contain(FormError(yearKeyError, s"$errorContext.year.invalid"))
        }
        "it has multiple invalid fields" in {
          val test = form.bind(DataMap.date(startDate)("0", "0", "2017"))
          test.errors must contain(FormError(dayKeyError, s"$errorContext.day-month.invalid"))
        }
        "the year provided is not the correct length" when {
          "the year is 3 digits" in {
            val test = form.bind(DataMap.date(startDate)("1", "1", "123"))
            test.errors must contain(FormError(yearKeyError, s"$errorContext.year.length"))
          }
          "the year is 5 digits" in {
            val test = form.bind(DataMap.date(startDate)("1", "1", "12345"))
            test.errors must contain(FormError(yearKeyError, s"$errorContext.year.length"))
          }
        }
      }
    }

    "accept a valid date" when {
      "the date is within 7 days from the current date" in {
        val sevenDaysInPresent: LocalDate = LocalDate.now.plusDays(6)
        val maxTest = form.bind(DataMap.date(startDate)(
          sevenDaysInPresent.getDayOfMonth.toString,
          sevenDaysInPresent.getMonthValue.toString,
          sevenDaysInPresent.getYear.toString
        ))
        maxTest.errors mustBe List()
        maxTest.value mustBe Some(DateModel.dateConvert(sevenDaysInPresent))
      }


      "the date is the earliest allowed date" in {
        val earliestAllowedDate: LocalDate = AccountingPeriodUtil.getStartDateLimit

        val testData = DataMap.date(startDate)(
          day = earliestAllowedDate.getDayOfMonth.toString,
          month = earliestAllowedDate.getMonthValue.toString,
          year = earliestAllowedDate.getYear.toString
        )

        val validated = form.bind(testData)

        validated.errors mustBe List()
        validated.value mustBe Some(DateModel.dateConvert(earliestAllowedDate))
      }
    }
  }


}
