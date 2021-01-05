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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms

import java.time.LocalDate

import org.scalatest.Matchers._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.{Form, FormError}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.formatters.DateModelMapping._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessStartDateForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.validation.testutils.DataMap.DataMap
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{BusinessStartDate, DateModel}

class BusinessStartDateFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessStartDateForm._


  def form: Form[BusinessStartDate] = {
    businessStartDateForm(BusinessStartDateForm.minStartDate.toString, BusinessStartDateForm.maxStartDate.toString)
  }

  "The BusinessStartDateForm" should {
    "transform a valid request to the date form case class" in {

      val testDateDay = "31"
      val testDateMonth = "05"
      val testDateYear = "2017"

      val testInput = Map(
        s"$startDate.$day" -> testDateDay, s"$startDate.$month" -> testDateMonth, s"$startDate.$year" -> testDateYear
      )

      val expected = BusinessStartDate(
        DateModel(testDateDay, testDateMonth, testDateYear)
      )

      val actual = form.bind(testInput).value

      actual shouldBe Some(expected)
    }

    "when testing the validation" should {

      "output the appropriate error messages for the start date" when {
        val empty = "error.date.empty"
        val invalid = "error.date.empty"
        val beforeMax = "error.business_start_date.maxStartDate"
        val beforeMin = "error.business_start_date.minStartDate"

        val dayKeyError: String = s"$startDate.$day"
        val monthKeyError: String = s"$startDate.$month"
        val yearKeyError: String = s"$startDate.$year"

        "the date is not supplied to the map" in {
          form.bind(DataMap.EmptyMap).errors must contain(FormError(dayKeyError, empty))
        }
        "the date supplied is empty" in {
          form.bind(DataMap.emptyDate(startDate)).errors must contain(FormError(dayKeyError, empty))
        }
        "it is an invalid date" in {
          val invalidTest = form.bind(DataMap.date(startDate)("29", "2", "2017"))
          invalidTest.errors must contain(FormError(startDate, invalid))
        }
        "it is within 2 years" in {
          val oneYearAgo: LocalDate = LocalDate.now.minusYears(1)
          val maxTest = form.bind(DataMap.date(startDate)(
            oneYearAgo.getDayOfMonth.toString,
            oneYearAgo.getMonthValue.toString,
            oneYearAgo.getYear.toString
          ))
          maxTest.errors must contain(FormError(startDate, beforeMax, Seq(BusinessStartDateForm.maxStartDate.toString)))
        }
        "it is before 1900" in {
          val minTest = form.bind(DataMap.date(startDate)("31", "12", "1899"))
          minTest.errors must contain(FormError(startDate, beforeMin, Seq(BusinessStartDateForm.minStartDate.toString)))
        }
        "it is is missing the day" in {
          val minTest = form.bind(DataMap.date(startDate)("", "4", "2017"))
          minTest.errors must contain(FormError(dayKeyError, "error.day.empty"))
        }
        "it is is missing the month" in {
          val minTest = form.bind(DataMap.date(startDate)("06", "", "2017"))
          minTest.errors must contain(FormError(monthKeyError, "error.month.empty"))
        }
        "it is is missing the year" in {
          val minTest = form.bind(DataMap.date(startDate)("06", "4", ""))
          minTest.errors must contain(FormError(yearKeyError, "error.year.empty"))
        }
        "it is is missing the day and month" in {
          val minTest = form.bind(DataMap.date(startDate)("", "", "2017"))
          minTest.errors must contain(FormError(dayKeyError, "error.day_month.empty"))
        }
        "it is is missing the day and year" in {
          val minTest = form.bind(DataMap.date(startDate)("", "4", ""))
          minTest.errors must contain(FormError(dayKeyError, "error.day_year.empty"))
        }
        "it is is missing the month and year" in {
          val minTest = form.bind(DataMap.date(startDate)("06", "", ""))
          minTest.errors must contain(FormError(monthKeyError, "error.month_year.empty"))
        }
      }
    }

    "accept a valid date" when {
      "the date is exactly two years ago" in {
        val twoYearsAgo: LocalDate = LocalDate.now.minusYears(2)
        val testData = DataMap.date(startDate)(
          day = twoYearsAgo.getDayOfMonth.toString,
          month = twoYearsAgo.getMonthValue.toString,
          year = twoYearsAgo.getYear.toString
        )
        val validated = form.bind(testData)
        validated.hasErrors shouldBe false
        validated.hasGlobalErrors shouldBe false
      }
      "the date is the first of january 1900" in {
        val earliestAllowedDate: LocalDate = LocalDate.of(1900, 1, 1)
        val testData = DataMap.date(startDate)(
          day = earliestAllowedDate.getDayOfMonth.toString,
          month = earliestAllowedDate.getMonthValue.toString,
          year = earliestAllowedDate.getYear.toString
        )
        val validated = form.bind(testData)
        validated.hasErrors shouldBe false
        validated.hasGlobalErrors shouldBe false
      }
    }
  }


}
