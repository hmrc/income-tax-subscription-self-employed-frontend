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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessNameForm._

class BusinessNameFormSpec extends PlaySpec {

  def form: Form[String] = businessNameForm

  lazy val testNameMinLength = "AA"
  lazy val testNameMaxLength: String = "A" * 105
  lazy val testNameInvalidChar = "!@£$%^*():;"

  "businessNameForm" should {
    "bind valid data" when {
      "business name has the minimum 2 characters" in {
        val testInput = Map(businessName -> testNameMinLength)
        val actual = form.bind(testInput).value

        actual mustBe Some(testNameMinLength)
      }

      "business name is exactly 105 characters" in {
        val testInput = Map(businessName -> testNameMaxLength)
        val actual = form.bind(testInput).value

        actual mustBe Some(testNameMaxLength)
      }
    }

    "fail to bind" when {
      "business name is empty" in {
        val testInput = Map(businessName -> "")
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(businessName, s"individual.error.$pageIdentifier.$businessName.empty"))
      }

      "business name has just a space" in {
        val testInput = Map(businessName -> " ")
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(businessName, s"individual.error.$pageIdentifier.$businessName.empty"))
      }

      "business name is too short" in {
        val testInput = Map(businessName -> "A")
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(businessName, s"individual.error.$pageIdentifier.$businessName.min-length"))
      }

      "business name is too long (over 105 characters)" in {
        val testInput = Map(businessName -> (testNameMaxLength + "A"))
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(businessName, s"individual.error.$pageIdentifier.$businessName.max-length"))
      }

      "business name has invalid characters" in {
        val testInput = Map(businessName -> testNameInvalidChar)
        val result = form.bind(testInput)
        result.value mustBe None

        result.errors must contain(FormError(businessName, s"individual.error.$pageIdentifier.$businessName.invalid-character"))
      }
    }
  }

  "unbind data correctly" in {
    val filledForm = form.fill("Test Business Name")

    filledForm.data must contain(businessName -> "Test Business Name")
  }

  "createBusinessNameData" should {
    "return an empty mapping" when {
      "nothing is supplied" in {
        val result = createBusinessNameData(None)

        result mustBe Map.empty[String, String]
      }
    }
    "return a mapping of the name key to the value" when {
      "a name is supplied" in {
        val result = createBusinessNameData(Some("test name"))

        result mustBe Map(businessName -> "test name")
      }
    }
  }
}