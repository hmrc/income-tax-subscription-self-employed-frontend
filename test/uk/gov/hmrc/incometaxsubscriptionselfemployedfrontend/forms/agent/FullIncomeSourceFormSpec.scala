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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.StreamlineIncomeSourceForm._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.submapping.YesNoMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.AccountingPeriodUtil

class FullIncomeSourceFormSpec extends PlaySpec {

  val form: Form[(String, String, YesNo)] = fullIncomeSourceForm

  lazy val testNameEmpty = ""
  lazy val testTradeNameMaxLength: String = "A" * 35
  lazy val testTradeNameMinLength = "AA"
  lazy val testNameMinLength = "AA"
  lazy val testNameMaxLength: String = "A" * 105
  lazy val testNameInvalidChar = "!@£$%^*():;"

  "fullIncomeSourceForm" should {
    "bind valid data" when {
      "business trade name is has minimum 2 characters" in {
        val testInput = Map(
          businessTradeName -> testTradeNameMinLength,
          businessName -> "Test Business Name",
          startDateBeforeLimit -> YesNoMapping.option_yes
        )
        val expected = (testTradeNameMinLength, "Test Business Name", Yes)
        val actual = form.bind(testInput).value

        actual mustBe Some(expected)
      }

      "business trade name is exactly 35 characters" in {
        val testInput = Map(
          businessTradeName -> testTradeNameMaxLength,
          businessName -> "Test Business Name",
          startDateBeforeLimit -> YesNoMapping.option_yes
        )
        val expected = (testTradeNameMaxLength, "Test Business Name", Yes)
        val actual = form.bind(testInput).value

        actual mustBe Some(expected)
      }

      "business name has minimum 2 characters" in {
        val testInput = Map(
          businessTradeName -> "Test Trade Name",
          businessName -> testNameMinLength,
          startDateBeforeLimit -> YesNoMapping.option_yes
        )
        val expected = ("Test Trade Name", testNameMinLength, Yes)
        val actual = form.bind(testInput).value

        actual mustBe Some(expected)
      }

      "business name is exactly 105 characters" in {
        val testInput = Map(
          businessTradeName -> "Test Trade Name",
          businessName -> testNameMaxLength,
          startDateBeforeLimit -> YesNoMapping.option_yes
        )
        val expected = ("Test Trade Name", testNameMaxLength, Yes)
        val actual = form.bind(testInput).value

        actual mustBe Some(expected)
      }

      "the start date selection is Yes" in {
        val testInput = Map(
          businessTradeName -> "Test Trade Name",
          businessName -> "Test Business Name",
          startDateBeforeLimit -> YesNoMapping.option_yes
        )
        val expected = ("Test Trade Name", "Test Business Name", Yes)
        val actual = form.bind(testInput).value

        actual mustBe Some(expected)
      }

      "the start date selection is No" in {
        val testInput = Map(
          businessTradeName -> "Test Trade Name",
          businessName -> "Test Business Name",
          startDateBeforeLimit -> YesNoMapping.option_no
        )
        val expected = ("Test Trade Name", "Test Business Name", No)
        val actual = form.bind(testInput).value

        actual mustBe Some(expected)
      }
    }

    "fail to bind" when {
      "business trade name is empty" in {
        val testInput = Map(
          businessTradeName -> testNameEmpty,
          businessName -> "Test Business Name",
          startDateBeforeLimit -> YesNoMapping.option_yes
        )
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(businessTradeName, s"agent.error.$pageIdentifier.$businessTradeName.empty"))
      }

      "business trade name is too short" in {
        val testInput = Map(
          businessTradeName -> "A",
          businessName -> "Test Business Name",
          startDateBeforeLimit -> YesNoMapping.option_yes
        )
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(businessTradeName, s"agent.error.$pageIdentifier.$businessTradeName.min-length"))
      }

      "business trade name is too long" in {
        val testInput = Map(
          businessTradeName -> (testTradeNameMaxLength + 1),
          businessName -> "Test Business Name",
          startDateBeforeLimit -> YesNoMapping.option_yes
        )
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(businessTradeName, s"agent.error.$pageIdentifier.$businessTradeName.max-length"))
      }

      "business trade name has invalid characters" in {
        val testInput = Map(
          businessTradeName -> "!@£#$%^*()_+={}<>?~`",
          businessName -> "Test Business Name",
          startDateBeforeLimit -> YesNoMapping.option_yes
        )
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(businessTradeName, s"agent.error.$pageIdentifier.$businessTradeName.invalid"))
      }

      "business trade name has just a space" in {
        val testInput = Map(
          businessTradeName -> " ",
          businessName -> "Test Business Name",
          startDateBeforeLimit -> YesNoMapping.option_yes
        )
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(businessTradeName, s"agent.error.$pageIdentifier.$businessTradeName.empty"))
      }

      "business name is empty" in {
        val testInput = Map(
          businessTradeName -> "Test Trade Name",
          businessName -> "",
          startDateBeforeLimit -> YesNoMapping.option_yes
        )
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(businessName, s"agent.error.$pageIdentifier.$businessName.empty"))
      }

      "business name is too long (over 105 characters)" in {
        val testInput = Map(
          businessTradeName -> "Test Trade Name",
          businessName -> (testNameMaxLength + 1),
          startDateBeforeLimit -> YesNoMapping.option_yes
        )
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(businessName, s"agent.error.$pageIdentifier.$businessName.max-length"))
      }

      "business name has invalid characters" in {
        val testInput = Map(
          businessTradeName -> "Test Trade Name",
          businessName -> testNameInvalidChar,
          startDateBeforeLimit -> YesNoMapping.option_yes
        )
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(businessName, s"agent.error.$pageIdentifier.$businessName.invalid-character"))
      }

      "business name has just a space" in {
        val testInput = Map(
          businessTradeName -> "Test Trade Name",
          businessName -> " ",
          startDateBeforeLimit -> YesNoMapping.option_yes
        )
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(businessName, s"agent.error.$pageIdentifier.$businessName.empty"))
      }

      "start date before limit has an invalid selection" in {
        val testInput = Map(
          businessTradeName -> "Test Trade Name",
          businessName -> "Test Business Name",
          startDateBeforeLimit -> "invalid"
        )
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(
          key = startDateBeforeLimit,
          message = s"agent.error.$pageIdentifier.$startDateBeforeLimit.invalid",
          args = Seq(AccountingPeriodUtil.getStartDateLimit.getYear.toString)
        ))
      }

      "start date before limit has no selection" in {
        val testInput = Map(
          businessTradeName -> "Test Trade Name",
          businessName -> "Test Business Name"
        )
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(
          key = startDateBeforeLimit,
          message = s"agent.error.$pageIdentifier.$startDateBeforeLimit.invalid",
          args = Seq(AccountingPeriodUtil.getStartDateLimit.getYear.toString)
        ))
      }
    }

    "unbind data correctly" in {
      val filledForm = form.fill(("Test Trade Name", "Test Business Name", Yes))

      filledForm.data mustBe Map(
        businessTradeName -> "Test Trade Name",
        businessName -> "Test Business Name",
        startDateBeforeLimit -> YesNoMapping.option_yes
      )
    }
  }

  "createIncomeSourceData" should {
    "return an empty mapping" when {
      "nothing is supplied" in {
        val result = createIncomeSourceData(None, None, None, None)

        result mustBe Map.empty[String, String]
      }
    }
    "return a mapping of the trade key to the value" when {
      "a trade is supplied" in {
        val result = createIncomeSourceData(maybeTradeName = Some("test trade"), None, None, None)

        result mustBe Map(businessTradeName -> "test trade")
      }
    }
    "return a mapping of the name key to the value" when {
      "a name is supplied" in {
        val result = createIncomeSourceData(None, maybeBusinessName = Some("test name"), None, None)

        result mustBe Map(businessName -> "test name")
      }
    }
    "return a mapping of the start date before limit key to a value" when {
      "start date before limit is provided as true" in {
        val result = createIncomeSourceData(None, None, None, maybeStartDateBeforeLimit = Some(true))

        result mustBe Map(startDateBeforeLimit -> "Yes")
      }
      "start date before limit is provided as false" in {
        val result = createIncomeSourceData(None, None, None, maybeStartDateBeforeLimit = Some(false))

        result mustBe Map(startDateBeforeLimit -> "No")
      }
      "start date is provided as a date before the limit" in {
        val result = createIncomeSourceData(None, None, maybeStartDate = Some(DateModel.dateConvert(AccountingPeriodUtil.getStartDateLimit.minusDays(1))), None)

        result mustBe Map(startDateBeforeLimit -> "Yes")
      }
      "start date is provided as a date after the limit" in {
        val result = createIncomeSourceData(None, None, maybeStartDate = Some(DateModel.dateConvert(AccountingPeriodUtil.getStartDateLimit)), None)

        result mustBe Map(startDateBeforeLimit -> "No")
      }
    }
  }
}