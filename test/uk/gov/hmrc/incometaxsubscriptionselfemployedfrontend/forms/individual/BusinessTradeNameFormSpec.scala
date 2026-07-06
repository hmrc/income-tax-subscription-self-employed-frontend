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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessTradeNameForm._

class BusinessTradeNameFormSpec extends PlaySpec {

  def form: Form[String] = businessTradeNameForm

  lazy val testTradeNameMinLength = "AA"
  lazy val testTradeNameMaxLength: String = "A" * 35
  lazy val testTradeNameInvalidChar = "!@£$%^*():;"

  "businessTradeNameForm" should {
    "bind valid data" when {
      "business trade name has the minimum 2 characters" in {
        val testInput = Map(businessTradeName -> testTradeNameMinLength)
        val actual = form.bind(testInput).value

        actual mustBe Some(testTradeNameMinLength)
      }

      "business trade name is exactly 35 characters" in {
        val testInput = Map(businessTradeName -> testTradeNameMaxLength)
        val actual = form.bind(testInput).value

        actual mustBe Some(testTradeNameMaxLength)
      }
    }

    "fail to bind" when {
      "business trade name is empty" in {
        val testInput = Map(businessTradeName -> "")
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(businessTradeName, s"individual.error.$pageIdentifier.$businessTradeName.empty"))
      }

      "business trade name has just a space" in {
        val testInput = Map(businessTradeName -> " ")
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(businessTradeName, s"individual.error.$pageIdentifier.$businessTradeName.empty"))
      }

      "business trade name is too long (over 35 characters)" in {
        val testInput = Map(businessTradeName -> (testTradeNameMaxLength + "A"))
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(businessTradeName, s"individual.error.$pageIdentifier.$businessTradeName.max-length"))
      }

      "business trade name has invalid characters" in {
        val testInput = Map(businessTradeName -> testTradeNameInvalidChar)
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(businessTradeName, s"individual.error.$pageIdentifier.$businessTradeName.invalid"))
      }

      "business trade name is too short" in {
        val testInput = Map(businessTradeName -> "A")
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(businessTradeName, s"individual.error.$pageIdentifier.$businessTradeName.min-length"))
      }
    }
  }

  "unbind data correctly" in {
    val filledForm = form.fill("Plumbing")

    filledForm.data must contain(businessTradeName -> "Plumbing")
  }

  "createBusinessTradeNameData" should {
    "return an empty mapping" when {
      "nothing is supplied" in {
        val result = createBusinessTradeNameData(None)

        result mustBe Map.empty[String, String]
      }
    }
    "return a mapping of the trade name key to the value" when {
      "a trade name is supplied" in {
        val result = createBusinessTradeNameData(Some("Plumbing"))

        result mustBe Map(businessTradeName -> "Plumbing")
      }
    }
  }
}