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
import play.api.data.{Form, FormError}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.BusinessNameForm._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.BusinessNameModel
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.BusinessNameModel._

class BusinessNameFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  def form: Form[BusinessNameModel] = {
    businessNameValidationForm
  }

  lazy val testNameValid = "business"
  lazy val testNameEmpty = ""
  lazy val testNameTooLong = "Qwertyuioplkjhgfdsazxcvbnmawedfoinaeoifnaeoifnoienfoqenfoqenfefkjadbfoeifijqebfkjadnfvasasdasdasdasdasdasdsssasdasdasdasdasdasdasdasdasdasdasdwdqwcrwcrwerohwe8ryc2r8ywehcowehfcwuefhcwiuefhc;wuehfcwiueFCuwe"
  lazy val testNameInvalidChar = "!"

  "BusinessNameForm" should {
    "should correctly validate a business name" in {
      val testInput = Map(businessName -> testNameValid)

      val expected = BusinessNameModel(testNameValid)

      val actual = form.bind(testInput).value

      actual shouldBe Some(expected)
    }

    "should invalidate an empty business name" in {
      val testInput = Map(businessName -> testNameEmpty)

      val emptyTest = form.bind(testInput)
      emptyTest.errors must contain(FormError(businessName, "error.business_name.empty"))
    }

    "should invalidate a business name that is over 160 characters" in {

      val testInput = Map(businessName -> testNameTooLong)

      val tooLongTest = form.bind(testInput)
      tooLongTest.errors must contain(FormError(businessName, "error.business_name.max_length"))
    }

    "should invalidate a business name that includes invalid characters" in {

      val testInput = Map(businessName -> testNameInvalidChar)

      val invalidCharTest = form.bind(testInput)
      invalidCharTest.errors must contain(FormError(businessName, "error.business_name.invalid_character"))
    }

    "should invalidate a business name that includes invalid characters and is too long" in {
      val testNameMultipleErrors = testNameInvalidChar + testNameTooLong
      val testInput = Map(businessName -> testNameMultipleErrors)

      val multipleErrorsTest = form.bind(testInput)
      multipleErrorsTest.errors mustBe List(FormError(businessName, "error.business_name.max_length"), FormError(businessName,"error.business_name.invalid_character"))
    }

    "should remove a leading space from business name" in {

      val testInput = Map(businessName -> (" " + testNameValid))

      val expected = Map(businessName -> testNameValid)

      val leadingSpaceTest = form.bind(testInput).value
      val testExpected = form.bind(expected).value
      leadingSpaceTest shouldBe testExpected
    }
  }
}
