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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual

import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.{Form, FormError}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.formatters.DateModelMapping.{day, month, year}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessStartDateForm.{businessStartDateForm, startDate}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.validation.testutils.DataMap.DataMap
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.DateModel
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.AccountingPeriodUtil

import java.time.LocalDate

class BusinessStartDateFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  val startDate: String = BusinessStartDateForm.startDate
  val dayKeyError: String = s"$startDate-dateDay"
  val monthKeyError: String = s"$startDate-dateMonth"
  val yearKeyError: String = s"$startDate-dateYear"

  "The BusinessStartDateForm" when {
    def form: Form[DateModel] = {
      businessStartDateForm(BusinessStartDateForm.maxStartDate, _.toString)
    }

    "transform a valid request to the date form case class" in {
      val validDate = LocalDate.now
      val testInput = Map(
        s"$startDate-dateDay"   -> validDate.getDayOfMonth.toString,
        s"$startDate-dateMonth" -> validDate.getMonthValue.toString,
        s"$startDate-dateYear"  -> validDate.getYear.toString
      )

      val expected = DateModel(
        validDate.getDayOfMonth.toString,
        validDate.getMonthValue.toString,
        validDate.getYear.toString
      )
      form.bind(testInput).value mustBe Some(expected)
    }

    "when testing the validation" should {
      val errorContext = "business.start-date"
      val empty        = s"error.$errorContext.empty"
      val required     = s"error.$errorContext.required"
      val requiredTwo  = s"error.$errorContext.required.two"
      val invalid      = s"error.$errorContext.invalid"
      val yearLength   = s"error.$errorContext.year.length"
      val beforeMax    = s"error.$errorContext.day-month-year.max-date"
      val beforeMin    = s"error.$errorContext.day-month-year.min-date"

      "output the appropriate error messages for the start date" when {
        "the date is not supplied to the map" in {
          form.bind(DataMap.EmptyMap).errors must contain(FormError(startDate, empty))
        }

        "it is not within 7 days from current date" in {
          val sevenDaysInFuture: LocalDate = LocalDate.now.plusDays(7)
          val maxTest = form.bind(DataMap.date(startDate)(
            sevenDaysInFuture.getDayOfMonth.toString,
            sevenDaysInFuture.getMonthValue.toString,
            sevenDaysInFuture.getYear.toString
          ))
          maxTest.errors must contain(FormError(startDate, beforeMax, Seq(BusinessStartDateForm.maxStartDate.plusDays(1).toString)))
        }

        "it is before 1900" in {
          val minTest = form.bind(DataMap.date(startDate)("31", "12", "1899"))
          minTest.errors must contain(FormError(startDate, beforeMin, Seq(BusinessStartDateForm.minStartDate.minusDays(1).toString)))
        }
        "it is before the start date limit" in {
          val minTest = form.bind(DataMap.date(startDate)("5", "4", AccountingPeriodUtil.getStartDateLimit.getYear.toString))
          minTest.errors must contain(FormError(startDate, beforeMin, Seq(BusinessStartDateForm.minStartDate.minusDays(1).toString)))
        }
        "it is missing the day" in {
          form.bind(DataMap.date(startDate)("", "4", "2017")).errors must contain(FormError(startDate, required, Seq("day")))
        }
        "it is missing the month" in {
          form.bind(DataMap.date(startDate)("1", "", "2017")).errors must contain(FormError(startDate, required, Seq("month")))
        }
        "it is missing the year" in {
          form.bind(DataMap.date(startDate)("1", "1", "")).errors must contain(FormError(startDate, required, Seq("year")))
        }
        "it is missing multiple fields" in {
          form.bind(DataMap.date(startDate)("", "", "2017")).errors must contain(FormError(startDate, requiredTwo, Seq("day", "month")))
        }
        "it has an invalid day" in {
          form.bind(DataMap.date(startDate)("0", "1", "2017")).errors must contain(FormError(dayKeyError, invalid, Seq("day")))
        }
        "it has an invalid month" in {
          form.bind(DataMap.date(startDate)("1", "13", "2017")).errors must contain(FormError(monthKeyError, invalid, Seq("month")))
        }
        "it has an invalid year" in {
          form.bind(DataMap.date(startDate)("1", "1", "abcd")).errors must contain(FormError(yearKeyError, yearLength, Seq("year")))
        }
        "it has multiple invalid fields" in {
          form.bind(DataMap.date(startDate)("0", "0", "2017")).errors must contain(FormError(startDate, invalid))
        }
        "the year provided is not the correct length" when {
          "the year is 3 digits" in {
            form.bind(DataMap.date(startDate)("1", "1", "123")).errors must contain(FormError(yearKeyError, yearLength, Seq("year")))
          }
          "the year is 5 digits" in {
            form.bind(DataMap.date(startDate)("1", "1", "12345")).errors must contain(FormError(yearKeyError, yearLength, Seq("year")))
          }
        }
      }

      "accept a valid date" when {
        "it is not within 7 days from current date" in {
          val sevenDaysInPresent: LocalDate = LocalDate.now.plusDays(6)
          val maxTest = form.bind(DataMap.date(startDate)(
            sevenDaysInPresent.getDayOfMonth.toString,
            sevenDaysInPresent.getMonthValue.toString,
            sevenDaysInPresent.getYear.toString
          ))
          maxTest.errors mustBe List()
          maxTest.value mustBe Some(DateModel.dateConvert(sevenDaysInPresent))
        }

        "the date is the minimum allowed" in {
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

}
