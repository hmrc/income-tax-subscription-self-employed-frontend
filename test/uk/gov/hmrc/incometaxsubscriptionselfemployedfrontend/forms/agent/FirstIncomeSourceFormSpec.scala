/*
 * Copyright 2024 HM Revenue & Customs
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

import org.scalatestplus.play.PlaySpec
import play.api.data.{Form, FormError}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.FirstIncomeSourceForm.{accountingMethodBusiness, businessName, businessTradeName, firstIncomeSourceForm, startDate}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.Cash.CASH
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{AccountingMethod, Cash, DateModel}

import java.time.LocalDate

class FirstIncomeSourceFormSpec extends PlaySpec {
  def dateFormatter(date: LocalDate): String = date.toString
  val form: Form[(String, String, DateModel, AccountingMethod)] = firstIncomeSourceForm(_.toString)

  lazy val testNameEmpty = ""
  lazy val testNameNotTooLong = "123456789-123456789-123456789-123456789-123456789-123456789-123456789-123456789-123456789-123456789-12345"
  lazy val testTradeNameNotTooLong = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
  lazy val testTradeNameMinLength = "AA"
  lazy val testNameMinLength = "AA"
  lazy val testNameTooLong = "123456789-123456789-123456789-123456789-123456789-123456789-123456789-123456789-123456789-123456789-123456"
  lazy val testNameInvalidChar = "!@£$%^*():;"

  "FirstIncomeSourceForm" should {

    "bind valid data" in {
      val date = DateModel("10", "6", "2023")
      val testInput = Map(
        businessTradeName -> "Test Trade Name",
        businessName -> "Test Business Name",
        s"$startDate-dateDay" -> date.day,
        s"$startDate-dateMonth" -> date.month,
        s"$startDate-dateYear" -> date.year,
        accountingMethodBusiness -> CASH
      )
      val expected = ("Test Trade Name", "Test Business Name", date, Cash)
      val actual = form.bind(testInput).value

      actual mustBe Some(expected)
    }

    "fail to bind when business trade name is empty" in {
      val date = DateModel("10", "6", "2023")
      val testInput = Map(
        businessTradeName -> testNameEmpty,
        businessName -> "Test Business Name",
        s"$startDate-dateDay" -> date.day,
        s"$startDate-dateMonth" -> date.month,
        s"$startDate-dateYear" -> date.year,
        accountingMethodBusiness -> CASH
      )
      val result = form.bind(testInput)
      result.value mustBe None

      result.errors must contain(FormError(businessTradeName, "error.agent.business-trade-name.empty"))
    }

    "fail to bind when business trade name is too short" in {
      val date = DateModel("10", "6", "2023")
      val testInput = Map(
        businessTradeName -> "A",
        businessName -> "Test Business Name",
        s"$startDate-dateDay" -> date.day,
        s"$startDate-dateMonth" -> date.month,
        s"$startDate-dateYear" -> date.year,
        accountingMethodBusiness -> CASH
      )
      val result = form.bind(testInput)
      result.value mustBe None

      result.errors must contain(FormError(businessTradeName, "error.agent.business-trade-name.min-length"))
    }

    "fail to bind when business trade name is too long" in {
      val date = DateModel("10", "6", "2023")
      val testInput = Map(
        businessTradeName -> ("A" * 36),
        businessName -> "Test Business Name",
        s"$startDate-dateDay" -> date.day,
        s"$startDate-dateMonth" -> date.month,
        s"$startDate-dateYear" -> date.year,
        accountingMethodBusiness -> CASH
      )
      val result = form.bind(testInput)
      result.value mustBe None

      result.errors must contain(FormError(businessTradeName, "error.agent.business-trade-name.max-length"))
    }

    "fail to bind when business trade name has invalid characters" in {
      val date = DateModel("10", "6", "2023")
      val testInput = Map(
        businessTradeName -> "!@£#$%^*()_+={}<>?~`",
        businessName -> "Test Business Name",
        s"$startDate-dateDay" -> date.day,
        s"$startDate-dateMonth" -> date.month,
        s"$startDate-dateYear" -> date.year,
        accountingMethodBusiness -> CASH
      )
      val result = form.bind(testInput)
      result.value mustBe None

      result.errors must contain(FormError(businessTradeName, "error.agent.business-trade-name.invalid"))
    }

    "fail to bind when business trade name has just a space" in {
      val date = DateModel("10", "6", "2023")
      val testInput = Map(
        businessTradeName -> " ",
        businessName -> "Test Business Name",
        s"$startDate-dateDay" -> date.day,
        s"$startDate-dateMonth" -> date.month,
        s"$startDate-dateYear" -> date.year,
        accountingMethodBusiness -> CASH
      )
      val result = form.bind(testInput)
      result.value mustBe None

      result.errors must contain(FormError(businessTradeName, "error.agent.business-trade-name.empty"))
    }

    "bind successfully when business trade name is has minimum 2 characters" in {
      val date = DateModel("10", "6", "2023")
      val testInput = Map(
        businessTradeName -> testTradeNameMinLength,
        businessName -> "Test Business Name",
        s"$startDate-dateDay" -> date.day,
        s"$startDate-dateMonth" -> date.month,
        s"$startDate-dateYear" -> date.year,
        accountingMethodBusiness -> CASH
      )
      val expected = (testTradeNameMinLength, "Test Business Name", date, Cash)
      val actual = form.bind(testInput).value

      actual mustBe Some(expected)
    }

    "bind successfully when business trade name is exactly 35 characters" in {
      val date = DateModel("10", "6", "2023")
      val testInput = Map(
        businessTradeName -> testTradeNameNotTooLong,
        businessName -> "Test Business Name",
        s"$startDate-dateDay" -> date.day,
        s"$startDate-dateMonth" -> date.month,
        s"$startDate-dateYear" -> date.year,
        accountingMethodBusiness -> CASH
      )
      val expected = (testTradeNameNotTooLong, "Test Business Name", date, Cash)
      val actual = form.bind(testInput).value

      actual mustBe Some(expected)
    }

    "fail to bind when business name is empty" in {
      val date = DateModel("10", "6", "2023")
      val testInput = Map(
        businessTradeName -> "Test Trade Name",
        businessName -> "",
        s"$startDate-dateDay" -> date.day,
        s"$startDate-dateMonth" -> date.month,
        s"$startDate-dateYear" -> date.year,
        accountingMethodBusiness -> CASH
      )
      val result = form.bind(testInput)
      result.value mustBe None

      result.errors must contain(FormError(businessName, "error.agent.business-name.empty"))
    }

    "fail to bind when business name is too long (over 105 characters)" in {
      val date = DateModel("10", "6", "2023")
      val testInput = Map(
        businessTradeName -> "Test Trade Name",
        businessName -> testNameTooLong,
        s"$startDate-dateDay" -> date.day,
        s"$startDate-dateMonth" -> date.month,
        s"$startDate-dateYear" -> date.year,
        accountingMethodBusiness -> CASH
      )
      val result = form.bind(testInput)
      result.value mustBe None

      result.errors must contain(FormError(businessName, "error.agent.business-name.max-length"))
    }

    "bind successfully when business name has minimum 2 characters" in {
      val date = DateModel("10", "6", "2023")
      val testInput = Map(
        businessTradeName -> "Test Trade Name",
        businessName -> testNameMinLength,
        s"$startDate-dateDay" -> date.day,
        s"$startDate-dateMonth" -> date.month,
        s"$startDate-dateYear" -> date.year,
        accountingMethodBusiness -> CASH
      )
      val expected = ("Test Trade Name", testNameMinLength, date, Cash)
      val actual = form.bind(testInput).value

      actual mustBe Some(expected)
    }

    "bind successfully when business name is exactly 105 characters" in {
      val date = DateModel("10", "6", "2023")
      val testInput = Map(
        businessTradeName -> "Test Trade Name",
        businessName -> testNameNotTooLong,
        s"$startDate-dateDay" -> date.day,
        s"$startDate-dateMonth" -> date.month,
        s"$startDate-dateYear" -> date.year,
        accountingMethodBusiness -> CASH
      )
      val expected = ("Test Trade Name", testNameNotTooLong, date, Cash)
      val actual = form.bind(testInput).value

      actual mustBe Some(expected)
    }

    "fail to bind when business name has invalid characters" in {
      val date = DateModel("10", "6", "2023")
      val testInput = Map(
        businessTradeName -> "Test Trade Name",
        businessName -> testNameInvalidChar,
        s"$startDate-dateDay" -> date.day,
        s"$startDate-dateMonth" -> date.month,
        s"$startDate-dateYear" -> date.year,
        accountingMethodBusiness -> CASH
      )
      val result = form.bind(testInput)
      result.value mustBe None

      result.errors must contain(FormError(businessName, "error.agent.business-name.invalid-character"))
    }

    "fail to bind when business name has just a space" in {
      val date = DateModel("10", "6", "2023")
      val testInput = Map(
        businessTradeName -> "Test Trade Name",
        businessName -> " ",
        s"$startDate-dateDay" -> date.day,
        s"$startDate-dateMonth" -> date.month,
        s"$startDate-dateYear" -> date.year,
        accountingMethodBusiness -> CASH
      )
      val result = form.bind(testInput)
      result.value mustBe None

      result.errors must contain(FormError(businessName, "error.agent.business-name.empty"))
    }

    "fail to bind when date is missing" in {
      val testInput = Map(
        businessTradeName -> "Test Trade Name",
        businessName -> "Test Business Name",
        s"$startDate-dateDay" -> "",
        s"$startDate-dateMonth" -> "",
        s"$startDate-dateYear" -> "",
        accountingMethodBusiness -> CASH
      )
      val result = form.bind(testInput)
      result.value mustBe None

      result.errors must contain(FormError(s"$startDate-dateDay", "agent.error.business.day-month-year.empty"))
    }

    "fail to bind when date is out of bounds (too early)" in {
      val testInput = Map(
        businessTradeName -> "Test Trade Name",
        businessName -> "Test Business Name",
        s"$startDate-dateDay" -> "31",
        s"$startDate-dateMonth" -> "12",
        s"$startDate-dateYear" -> "1899",
        accountingMethodBusiness -> CASH
      )
      val result = form.bind(testInput)
      result.value mustBe None

      result.errors must contain(FormError(s"$startDate-dateDay", "agent.error.business.day-month-year.min-date", Seq("1900-01-01")))
    }

    "fail to bind when date is out of bounds (too late)" in {
      val maxDate = LocalDate.now().plusDays(7)
      val testInput = Map(
        businessTradeName -> "Test Trade Name",
        businessName -> "Test Business Name",
        s"$startDate-dateDay" -> maxDate.getDayOfMonth.toString,
        s"$startDate-dateMonth" -> maxDate.getMonthValue.toString,
        s"$startDate-dateYear" -> maxDate.getYear.toString,
        accountingMethodBusiness -> CASH
      )
      val result = form.bind(testInput)
      result.value mustBe None

      result.errors must contain(FormError(s"$startDate-dateDay", "agent.error.business.day-month-year.max-date", Seq(maxDate.minusDays(1).toString)))
    }

    "unbind data correctly" in {
      val filledForm = form.fill(("Test Trade Name", "Test Business Name", DateModel("10", "6", "2023"), Cash))

      filledForm.data must contain allOf (
        businessTradeName -> "Test Trade Name",
        businessName -> "Test Business Name",
        s"$startDate-dateDay" -> "10",
        s"$startDate-dateMonth" -> "6",
        s"$startDate-dateYear" -> "2023",
        accountingMethodBusiness -> CASH
      )
    }

    "show an error when date is not supplied" in {
      val result = form.bind(Map.empty[String, String])
      result.errors must contain(FormError(s"$startDate-dateDay", "agent.error.business.day-month-year.empty"))
    }

    "show an error when date is invalid" in {
      val result = form.bind(Map(
        businessTradeName -> "Test Trade Name",
        businessName -> "Test Business Name",
        s"$startDate-dateDay" -> "31",
        s"$startDate-dateMonth" -> "13",
        s"$startDate-dateYear" -> "1899",
        accountingMethodBusiness -> CASH
      ))
      result.errors must contain(FormError(s"$startDate-dateMonth", "agent.error.business.month.invalid"))
    }

    "show an error when day is missing" in {
      val result = form.bind(Map(
        businessTradeName -> "Test Trade Name",
        businessName -> "Test Business Name",
        s"$startDate-dateDay" -> "",
        s"$startDate-dateMonth" -> "4",
        s"$startDate-dateYear" -> "2017",
        accountingMethodBusiness -> CASH
      ))
      result.errors must contain(FormError(s"$startDate-dateDay", "agent.error.business.day.empty"))
    }

    "show an error when month is missing" in {
      val result = form.bind(Map(
        businessTradeName -> "Test Trade Name",
        businessName -> "Test Business Name",
        s"$startDate-dateDay" -> "1",
        s"$startDate-dateMonth" -> "",
        s"$startDate-dateYear" -> "2017",
        accountingMethodBusiness -> CASH
      ))
      result.errors must contain(FormError(s"$startDate-dateMonth", "agent.error.business.month.empty"))
    }

    "show an error when year is missing" in {
      val result = form.bind(Map(
        businessTradeName -> "Test Trade Name",
        businessName -> "Test Business Name",
        s"$startDate-dateDay" -> "1",
        s"$startDate-dateMonth" -> "1",
        s"$startDate-dateYear" -> "",
        accountingMethodBusiness -> CASH
      ))
      result.errors must contain(FormError(s"$startDate-dateYear", "agent.error.business.year.empty"))
    }

    "show an error when multiple fields are missing" in {
      val result = form.bind(Map(
        businessTradeName -> "Test Trade Name",
        businessName -> "Test Business Name",
        s"$startDate-dateDay" -> "",
        s"$startDate-dateMonth" -> "",
        s"$startDate-dateYear" -> "2017",
        accountingMethodBusiness -> CASH
      ))
      result.errors must contain(FormError(s"$startDate-dateDay", "agent.error.business.day-month.empty"))
    }

    "show an error when day is invalid" in {
      val result = form.bind(Map(
        businessTradeName -> "Test Trade Name",
        businessName -> "Test Business Name",
        s"$startDate-dateDay" -> "0",
        s"$startDate-dateMonth" -> "1",
        s"$startDate-dateYear" -> "2017",
        accountingMethodBusiness -> CASH
      ))
      result.errors must contain(FormError(s"$startDate-dateDay", "agent.error.business.day.invalid"))
    }

    "show an error when month is invalid" in {
      val result = form.bind(Map(
        businessTradeName -> "Test Trade Name",
        businessName -> "Test Business Name",
        s"$startDate-dateDay" -> "1",
        s"$startDate-dateMonth" -> "13",
        s"$startDate-dateYear" -> "2017",
        accountingMethodBusiness -> CASH
      ))
      result.errors must contain(FormError(s"$startDate-dateMonth", "agent.error.business.month.invalid"))
    }

    "show an error when year is invalid" in {
      val result = form.bind(Map(
        businessTradeName -> "Test Trade Name",
        businessName -> "Test Business Name",
        s"$startDate-dateDay" -> "1",
        s"$startDate-dateMonth" -> "1",
        s"$startDate-dateYear" -> "invalid",
        accountingMethodBusiness -> CASH
      ))
      result.errors must contain(FormError(s"$startDate-dateYear", "agent.error.business.year.invalid"))
    }

    "show an error when multiple fields are invalid" in {
      val result = form.bind(Map(
        businessTradeName -> "Test Trade Name",
        businessName -> "Test Business Name",
        s"$startDate-dateDay" -> "0",
        s"$startDate-dateMonth" -> "0",
        s"$startDate-dateYear" -> "2017",
        accountingMethodBusiness -> CASH
      ))
      result.errors must contain(FormError(s"$startDate-dateDay", "agent.error.business.day-month.invalid"))
    }

    "show an error when year length is incorrect" when {
      "year has 3 digits" in {
        val result = form.bind(Map(
          businessTradeName -> "Test Trade Name",
          businessName -> "Test Business Name",
          s"$startDate-dateDay" -> "1",
          s"$startDate-dateMonth" -> "1",
          s"$startDate-dateYear" -> "123",
          accountingMethodBusiness -> CASH
        ))
        result.errors must contain(FormError(s"$startDate-dateYear", "agent.error.business.year.length"))
      }

      "year has 5 digits" in {
        val result = form.bind(Map(
          businessTradeName -> "Test Trade Name",
          businessName -> "Test Business Name",
          s"$startDate-dateDay" -> "1",
          s"$startDate-dateMonth" -> "1",
          s"$startDate-dateYear" -> "12345",
          accountingMethodBusiness -> CASH
        ))
        result.errors must contain(FormError(s"$startDate-dateYear", "agent.error.business.year.length"))
      }
    }

    "fail to bind when Accounting Method is invalid" in {
      val testInput = Map(
        businessTradeName -> "Test Trade Name",
        businessName -> "Test Business Name",
        s"$startDate-dateDay" -> "10",
        s"$startDate-dateMonth" -> "6",
        s"$startDate-dateYear" -> "2023",
        accountingMethodBusiness -> "invalid_method"
      )
      val result = form.bind(testInput)
      result.value mustBe None

      result.errors must contain(FormError(accountingMethodBusiness, "agent.error.accounting-method-property.invalid"))
    }


  }

  "accept a valid date" when {
    "the date is 7 days ahead from current date" in {
      val sevenDaysInPast = LocalDate.now().plusDays(6)
      val testData = Map(
        businessTradeName -> "Test Trade Name",
        businessName -> "Test Business Name",
        s"$startDate-dateDay" -> sevenDaysInPast.getDayOfMonth.toString,
        s"$startDate-dateMonth" -> sevenDaysInPast.getMonthValue.toString,
        s"$startDate-dateYear" -> sevenDaysInPast.getYear.toString,
        accountingMethodBusiness -> CASH
      )
      val validated = form.bind(testData)
      validated.hasErrors mustBe false
      validated.hasGlobalErrors mustBe false
    }

    "the date is the 1 January 1900" in {
      val earliestAllowedDate = LocalDate.of(1900, 1, 1)
      val testData = Map(
        businessTradeName -> "Test Trade Name",
        businessName -> "Test Business Name",
        s"$startDate-dateDay" -> earliestAllowedDate.getDayOfMonth.toString,
        s"$startDate-dateMonth" -> earliestAllowedDate.getMonthValue.toString,
        s"$startDate-dateYear" -> earliestAllowedDate.getYear.toString,
        accountingMethodBusiness -> CASH
      )
      val validated = form.bind(testData)
      validated.hasErrors mustBe false
      validated.hasGlobalErrors mustBe false
    }
  }
}

