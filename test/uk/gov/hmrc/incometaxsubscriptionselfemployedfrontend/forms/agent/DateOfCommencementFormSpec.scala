/*
 * Copyright 2020 HM Revenue & Customs
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

import org.scalatest.Matchers._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.{Form, FormError}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.submapping.AgentDateMapping._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.DateOfCommencementForm._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.validation.testutils.DataMap.DataMap
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{BusinessStartDate, DateModel}

class DateOfCommencementFormSpec extends PlaySpec with GuiceOneAppPerSuite {


  def form: Form[BusinessStartDate] = {
    dateOfCommencementForm(DateOfCommencementForm.minStartDate.toString)
  }

  "The DateOfCommencementForm" should {
    "transform a valid request to the date form case class" in {

      val testDateDay = "31"
      val testDateMonth = "05"
      val testDateYear = "2017"

      val testInput = Map(
        s"$startDate.$dateDay" -> testDateDay, s"$startDate.$dateMonth" -> testDateMonth, s"$startDate.$dateYear" -> testDateYear
      )

      val expected = BusinessStartDate(
        DateModel(testDateDay, testDateMonth, testDateYear)
      )

      val actual = form.bind(testInput).value

      actual shouldBe Some(expected)
    }

    "when testing the validation" should {

      "output the appropriate error messages for the start date" when {
        val empty = "error.agent.date.empty"
        val invalid = "error.agent.date.empty"
        val beforeMin = "error.agent.business_accounting_period.minStartDate"

        "the date is not supplied to the map" in {
          form.bind(DataMap.EmptyMap).errors must contain(FormError(startDate, empty))
        }

        "the date supplied is empty" in {
          form.bind(DataMap.emptyDate(startDate)).errors must contain(FormError(startDate, empty))
        }

        "it is an invalid date" in {
          val invalidTest = form.bind(DataMap.date(startDate)("29", "2", "2017"))
          invalidTest.errors must contain(FormError(startDate, invalid))
        }

        "it is older than 2 years" in {
          val minTest = form.bind(DataMap.date(startDate)("06", "4", "2017"))
          minTest.errors mustNot contain(FormError(startDate, beforeMin))
        }
        "it is is missing the day" in {
          val minTest = form.bind(DataMap.date(startDate)("", "4", "2017"))
          minTest.errors must contain(FormError(startDate, "error.agent.day.empty"))
        }
        "it is is missing the month" in {
          val minTest = form.bind(DataMap.date(startDate)("06", "", "2017"))
          minTest.errors must contain(FormError(startDate, "error.agent.month.empty"))
        }
        "it is is missing the year" in {
          val minTest = form.bind(DataMap.date(startDate)("06", "4", ""))
          minTest.errors must contain(FormError(startDate, "error.agent.year.empty"))
        }
        "it is is missing the day and month" in {
          val minTest = form.bind(DataMap.date(startDate)("", "", "2017"))
          minTest.errors must contain(FormError(startDate, "error.agent.day.month.empty"))
        }
        "it is is missing the day and year" in {
          val minTest = form.bind(DataMap.date(startDate)("", "4", ""))
          minTest.errors must contain(FormError(startDate, "error.agent.day.year.empty"))
        }
        "it is is missing the month and year" in {
          val minTest = form.bind(DataMap.date(startDate)("06", "", ""))
          minTest.errors must contain(FormError(startDate, "error.agent.month.year.empty"))
        }
      }
    }

    "accept a valid date" in {
      val testData = DataMap.date(startDate)("28", "5", "2017")
      val validated = form.bind(testData)
      validated.hasErrors shouldBe false
      validated.hasGlobalErrors shouldBe false
    }
  }


}