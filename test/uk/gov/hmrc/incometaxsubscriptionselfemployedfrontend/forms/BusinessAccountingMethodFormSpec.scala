/*
 * Copyright 2021 HM Revenue & Customs
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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.submapping.AccountingMethodMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.validation.testutils.DataMap.DataMap
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{AccountingMethodModel, Cash}


class BusinessAccountingMethodFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessAccountingMethodForm._

  "The BusinessAccountingMethodForm" should {
    "transform a valid request to the case class" in {

      val testInput = Map(businessAccountingMethod -> AccountingMethodMapping.option_cash)

      val expected = AccountingMethodModel(Cash)

      val actual = businessAccountingMethodForm.bind(testInput).value

      actual shouldBe Some(expected)
    }

    "when testing the validation" should {

      val maxLength = 160

      val empty = "error.business_accounting_method.empty"
      val invalid = "error.business_accounting_method.invalid"

      "the map be empty" in {
        val emptyInput0 = DataMap.EmptyMap
        val emptyTest0 = businessAccountingMethodForm.bind(emptyInput0)
        emptyTest0.errors must contain(FormError(businessAccountingMethod,empty))
      }

      "the name be empty" in {
        val emptyInput = DataMap.businessAccountingMethod("")
        val emptyTest = businessAccountingMethodForm.bind(emptyInput)
        emptyTest.errors must contain(FormError(businessAccountingMethod,empty))
      }

      "the name should be invalid" in {
        val invalidInput = DataMap.businessAccountingMethod("invalid")
        val invalidTest = businessAccountingMethodForm.bind(invalidInput)
        invalidTest.errors must contain(FormError(businessAccountingMethod, invalid))
      }

      "The following Cash submission should be valid" in {
        val valid = DataMap.businessAccountingMethod(AccountingMethodMapping.option_cash)
        val result = businessAccountingMethodForm.bind(valid)
        result.hasErrors shouldBe false
        result.hasGlobalErrors shouldBe false
      }

      "The following Accruals submission should be valid" in {
        val valid = DataMap.businessAccountingMethod(AccountingMethodMapping.option_accruals)
        val result = businessAccountingMethodForm.bind(valid)
        result.hasErrors shouldBe false
        result.hasGlobalErrors shouldBe false
      }
    }
  }
}
