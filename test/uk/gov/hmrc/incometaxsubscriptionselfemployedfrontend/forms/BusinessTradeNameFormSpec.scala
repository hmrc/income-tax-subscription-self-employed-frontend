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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms

import org.scalatest.Matchers._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.FormError
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.validation.testutils.DataMap.DataMap
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.BusinessTradeNameModel
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.TestModels._


class BusinessTradeNameFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.BusinessTradeNameForm._

  "The BusinessTradeNameForm" should {
    "transform a valid request to the case class" in {

      val testInput = Map(businessTradeName -> testValidBusinessTradeName)

      val expected = BusinessTradeNameModel(testValidBusinessTradeName)

      val actual = businessTradeNameValidationForm.bind(testInput).value

      actual shouldBe Some(expected)
    }

    "when testing the validation" should {

      val maxLength = 160

      val empty = "error.business_trade_name.empty"
      val maxLen = "error.business_trade_name.maxLength"
      val invalid = "error.business_trade_name.invalid"

      "the map be empty" in {
        val emptyInput0 = DataMap.EmptyMap
        val emptyTest0 = businessTradeNameValidationForm.bind(emptyInput0)
        emptyTest0.errors must contain(FormError(businessTradeName,empty))
      }

      "the name be empty" in {
        val emptyInput = DataMap.businessTradeNameMap("")
        val emptyTest = businessTradeNameValidationForm.bind(emptyInput)
        emptyTest.errors must contain(FormError(businessTradeName,empty))
      }

      "the name is too long" in {
        val maxLengthInput = DataMap.businessTradeNameMap("a" * maxLength + 1)
        val maxLengthTest = businessTradeNameValidationForm.bind(maxLengthInput)
        maxLengthTest.errors must contain(FormError(businessTradeName,maxLen))
      }

      "the name should be invalid" in {
        val invalidInput = DataMap.businessTradeNameMap("!()+{}?^~")
        val invalidTest = businessTradeNameValidationForm.bind(invalidInput)
        invalidTest.errors must contain(FormError(businessTradeName,invalid))
      }

      "the name is max characters and acceptable" in {
        val withinLimitInput = DataMap.businessTradeNameMap("a" * maxLength)
        val withinLimitTest = businessTradeNameValidationForm.bind(withinLimitInput)
        withinLimitTest.value mustNot contain(maxLen)
      }

      "The following submission should be valid" in {
        val valid = DataMap.businessTradeNameMap("Test business")
        val result = businessTradeNameValidationForm.bind(valid)
        result.hasErrors shouldBe false
        result.hasGlobalErrors shouldBe false
      }
    }
  }
}
