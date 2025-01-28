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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual

import org.scalatestplus.play.PlaySpec
import play.api.data.{Form, FormError}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.StreamlineIncomeSourceForm._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.submapping.YesNoMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.AccountingPeriodUtil


class StreamlineIncomeSourceFormSpec extends PlaySpec {

  def form: Form[(String, String, YesNo)] = fullIncomeSourceForm

  lazy val testTradeEmpty = ""
  lazy val testTradeMaxLength: String = "A" * 35
  lazy val testTradeMinLength = "AA"
  lazy val testNameMinLength = "AA"
  lazy val testNameMaxLength: String = "A" * 105
  lazy val testNameInvalidChar = "!@£$%^*():;"

  "fullIncomeSourceForm" should {
    "bind valid data" when {
      "business trade has minimum 2 characters" in {
        val testInput = Map(
          businessTradeName -> testTradeMinLength,
          businessName -> "Test Business Name",
          startDateBeforeLimit -> YesNoMapping.option_yes
        )
        val expected = (testTradeMinLength, "Test Business Name", Yes)
        val actual = form.bind(testInput).value

        actual mustBe Some(expected)
      }

      "business trade name is exactly 35 characters" in {
        val testInput = Map(
          businessTradeName -> testTradeMaxLength,
          businessName -> "Test Business Name",
          startDateBeforeLimit -> YesNoMapping.option_yes
        )
        val expected = (testTradeMaxLength, "Test Business Name", Yes)
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

      "start date before limit is yes" in {
        val testInput = Map(
          businessTradeName -> "Test Trade Name",
          businessName -> testNameMaxLength,
          startDateBeforeLimit -> YesNoMapping.option_yes
        )
        val expected = ("Test Trade Name", testNameMaxLength, Yes)
        val actual = form.bind(testInput).value

        actual mustBe Some(expected)
      }

      "start date before limit is no" in {
        val testInput = Map(
          businessTradeName -> "Test Trade Name",
          businessName -> testNameMaxLength,
          startDateBeforeLimit -> YesNoMapping.option_no
        )
        val expected = ("Test Trade Name", testNameMaxLength, No)
        val actual = form.bind(testInput).value

        actual mustBe Some(expected)
      }

    }

    "fail to bind" when {
      "business trade name is empty" in {
        val testInput = Map(
          businessTradeName -> testTradeEmpty,
          businessName -> "Test Trade Name",
          startDateBeforeLimit -> YesNoMapping.option_yes
        )
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(businessTradeName, s"individual.error.$pageIdentifier.business-trade.empty"))
      }

      "business trade name is too short" in {
        val testInput = Map(
          businessTradeName -> "A",
          businessName -> "Test Business Name",
          startDateBeforeLimit -> YesNoMapping.option_yes
        )
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(businessTradeName, s"individual.error.$pageIdentifier.business-trade.min-length"))
      }

      "business trade name is too long" in {
        val testInput = Map(
          businessTradeName -> (testTradeMaxLength + 1),
          businessName -> "Test Business Name",
          startDateBeforeLimit -> YesNoMapping.option_yes
        )
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(businessTradeName, s"individual.error.$pageIdentifier.business-trade.max-length"))
      }

      "business trade name has invalid characters" in {
        val testInput = Map(
          businessTradeName -> "!@£#$%^*()_+={}<>?~`",
          businessName -> "Test Business Name",
          startDateBeforeLimit -> YesNoMapping.option_yes
        )
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(businessTradeName, s"individual.error.$pageIdentifier.business-trade.invalid"))
      }

      "business trade name has just a space" in {
        val testInput = Map(
          businessTradeName -> " ",
          businessName -> "Test Business Name",
          startDateBeforeLimit -> YesNoMapping.option_yes
        )
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(businessTradeName, s"individual.error.$pageIdentifier.business-trade.empty"))
      }

      "business name is empty" in {
        val testInput = Map(
          businessTradeName -> "Test Trade Name",
          businessName -> "",
          startDateBeforeLimit -> YesNoMapping.option_yes
        )
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(businessName, s"individual.error.$pageIdentifier.$businessName.empty"))
      }

      "business name is too short" in {
        val testInput = Map(
          businessTradeName -> "Test Trade Name",
          businessName -> "A",
          startDateBeforeLimit -> YesNoMapping.option_yes
        )
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(businessName, s"individual.error.$pageIdentifier.$businessName.min-length"))
      }

      "business name is too long (over 105 characters)" in {
        val testInput = Map(
          businessTradeName -> "Test Trade Name",
          businessName -> (testNameMaxLength + 1),
          startDateBeforeLimit -> YesNoMapping.option_yes
        )
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(businessName, s"individual.error.$pageIdentifier.$businessName.max-length"))
      }

      "business name has invalid characters" in {
        val testInput = Map(
          businessTradeName -> "Test Trade Name",
          businessName -> testNameInvalidChar,
          startDateBeforeLimit -> YesNoMapping.option_yes
        )
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(businessName, s"individual.error.$pageIdentifier.$businessName.invalid"))
      }

      "business name has just a space" in {
        val testInput = Map(
          businessTradeName -> "Test Trade Name",
          businessName -> " ",
          startDateBeforeLimit -> YesNoMapping.option_yes
        )
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(businessName, s"individual.error.$pageIdentifier.$businessName.empty"))
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
          message = s"individual.error.$pageIdentifier.$startDateBeforeLimit.empty",
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
          message = s"individual.error.$pageIdentifier.$startDateBeforeLimit.empty",
          args = Seq(AccountingPeriodUtil.getStartDateLimit.getYear.toString)
        ))
      }

    }
  }

  "unbind data correctly" in {
    val filledForm = form.fill(("Test Trade Name", "Test Business Name", Yes))

    filledForm.data must contain allOf(
      businessTradeName -> "Test Trade Name",
      businessName -> "Test Business Name",
      startDateBeforeLimit -> YesNoMapping.option_yes
    )
  }
}

