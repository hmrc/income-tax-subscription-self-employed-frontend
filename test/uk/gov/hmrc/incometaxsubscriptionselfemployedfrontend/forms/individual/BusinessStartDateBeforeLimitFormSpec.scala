/*
 * Copyright 2026 HM Revenue & Customs
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

import org.scalatestplus.play.PlaySpec
import play.api.data.{Form, FormError}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessStartDateBeforeLimitForm.{businessStartDateBeforeLimitForm, createStartDateBeforeLimitData, startDateBeforeLimit}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.submapping.YesNoMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{DateModel, No, Yes, YesNo}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.AccountingPeriodUtil

class BusinessStartDateBeforeLimitFormSpec extends PlaySpec {

  def form: Form[YesNo] = businessStartDateBeforeLimitForm

  "businessStartDateBeforeLimitForm" should {
    "bind valid data" when {
      "start date before limit is yes" in {
        val testInput = Map(
          startDateBeforeLimit -> YesNoMapping.option_yes
        )
        val expected = Yes
        val actual = form.bind(testInput).value

        actual mustBe Some(expected)
      }

      "start date before limit is no" in {
        val testInput = Map(
          startDateBeforeLimit -> YesNoMapping.option_no
        )
        val expected = No
        val actual = form.bind(testInput).value

        actual mustBe Some(expected)
      }

    }

    "fail to bind" when {
      "start date before limit has an invalid selection" in {
        val testInput = Map(
          startDateBeforeLimit -> "invalid"
        )
        val result = form.bind(testInput)

        result.errors must contain(FormError(
          key = startDateBeforeLimit,
          message = "individual.error.start-date-before-limit.empty",
          args = Seq(AccountingPeriodUtil.getStartDateLimit.getYear.toString)
        ))
      }

      "start date before limit has no selection" in {
        val result = form.bind(Map.empty[String, String])

        result.value mustBe None
        result.errors must contain(FormError(
          key = startDateBeforeLimit,
          message = "individual.error.start-date-before-limit.empty",
          args = Seq(AccountingPeriodUtil.getStartDateLimit.getYear.toString)
        ))
      }

    }
  }

  "unbind data correctly" in {
    val filledForm = form.fill(Yes)

    filledForm.data must contain(
      startDateBeforeLimit -> YesNoMapping.option_yes
    )
  }

  "createStartDateBeforeLimitData" should {
    "return an empty mapping" when {
      "nothing is supplied" in {
        val result = createStartDateBeforeLimitData(None, None)

        result mustBe Map.empty[String, String]
      }
    }
    "return a mapping of the start date before limit key to a value" when {
      "start date before limit is provided as true" in {
        val result = createStartDateBeforeLimitData(None, maybeStartDateBeforeLimit = Some(true))

        result mustBe Map(startDateBeforeLimit -> "Yes")
      }
      "start date before limit is provided as false" in {
        val result = createStartDateBeforeLimitData(None, maybeStartDateBeforeLimit = Some(false))

        result mustBe Map(startDateBeforeLimit -> "No")
      }
      "start date is provided as a date before the limit" in {
        val result = createStartDateBeforeLimitData(maybeStartDate = Some(DateModel.dateConvert(AccountingPeriodUtil.getStartDateLimit.minusDays(1))), None)

        result mustBe Map(startDateBeforeLimit -> "Yes")
      }
      "start date is provided as a date after the limit" in {
        val result = createStartDateBeforeLimitData(maybeStartDate = Some(DateModel.dateConvert(AccountingPeriodUtil.getStartDateLimit)), None)

        result mustBe Map(startDateBeforeLimit -> "No")
      }
    }
  }

}