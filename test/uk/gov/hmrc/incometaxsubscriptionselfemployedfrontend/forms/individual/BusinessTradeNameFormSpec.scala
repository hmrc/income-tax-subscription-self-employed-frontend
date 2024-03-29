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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.validation.testutils.DataMap.DataMap
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.TestModels._

class BusinessTradeNameFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessTradeNameForm._

  def businessTradeForm(excludedBusinessTradeNames: Seq[String] = Nil): Form[String] = {
    businessTradeNameValidationForm(excludedBusinessTradeNames)
  }

  "The BusinessTradeNameForm" should {
    "transform a valid request to the case class" in {

      val testInput = Map(businessTradeName -> testValidBusinessTradeName)

      val expected = testValidBusinessTradeName

      val actual = businessTradeForm().bind(testInput).value

      actual shouldBe Some(expected)
    }

    "when testing the validation" should {

      val maxLength = 35
      val minLength = 2
      val testTradeInvalidChar = "!@£#$%^*()_+={}<>?~`"

      val empty = "error.business-trade-name.empty"
      val invalidChar = "error.business-trade-name.invalid"
      val maxLen = "error.business-trade-name.max-length"
      val minLen = "error.business-trade-name.min-length"
      val duplicate = "error.business-trade-name.duplicate"

      "the map be empty" in {
        val emptyInput0 = DataMap.EmptyMap
        val emptyTest0 = businessTradeForm().bind(emptyInput0)
        emptyTest0.errors must contain(FormError(businessTradeName, empty))
      }

      "the name be empty" in {
        val emptyInput = DataMap.businessTradeNameMap("")
        val emptyTest = businessTradeForm().bind(emptyInput)
        emptyTest.errors must contain(FormError(businessTradeName, empty))
      }

      "name has an invalid character" which {
        for (char <- testTradeInvalidChar) {
          s"is the $char symbol" in {
            val invalidInput = DataMap.businessTradeNameMap(char.toString)
            val invalidTest = businessTradeForm().bind(invalidInput)
            invalidTest.errors must contain(FormError(businessTradeName, invalidChar))
          }
        }
      }

      "the name is too long" in {
        val maxLengthInput = DataMap.businessTradeNameMap("a" * maxLength + 1)
        val maxLengthTest = businessTradeForm().bind(maxLengthInput)
        maxLengthTest.errors must contain(FormError(businessTradeName, maxLen))
      }

      "the name is too short" in {
        val minLengthInput = DataMap.businessTradeNameMap("a" * (minLength - 1))
        val minLengthTest = businessTradeForm().bind(minLengthInput)
        minLengthTest.errors must contain(FormError(businessTradeName, minLen))
      }

      "the name should not allow just a space" in {
        val emptyInput = DataMap.businessTradeNameMap(" ")
        val invalidTest = businessTradeForm().bind(emptyInput)
        invalidTest.errors must contain(FormError(businessTradeName, empty))
      }

      "the name be max characters and acceptable" in {
        val withinLimitInput = DataMap.businessTradeNameMap("a" * maxLength)
        val withinLimitTest = businessTradeForm().bind(withinLimitInput)
        withinLimitTest.errors mustNot contain(FormError(businessTradeName, maxLen))
      }

      "invalidate a business trade which is in the list of excluded business trade" in {
        val testInput = Map(businessTradeName -> "nameOne")
        val actual = businessTradeForm(excludedBusinessTradeNames = Seq(
          "nameOne", "nameTwo"
        )).bind(testInput)
        actual.errors must contain(FormError(businessTradeName, duplicate))
      }

      "The following submission should be valid" when {
        "there are no other businesses" in {
          val valid = DataMap.businessTradeNameMap("Test business")
          val result = businessTradeForm().bind(valid)
          result.hasErrors shouldBe false
          result.hasGlobalErrors shouldBe false
        }

        "the name contains special characters" in {
          val valid = DataMap.businessTradeNameMap("""aa&'/\.,-""")
          val result = businessTradeForm().bind(valid)
          print(result.errors)
          result.hasErrors shouldBe false
          result.hasGlobalErrors shouldBe false
        }
      }
    }
  }
}

