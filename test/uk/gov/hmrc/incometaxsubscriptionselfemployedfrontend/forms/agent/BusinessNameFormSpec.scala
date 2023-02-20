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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent

import org.scalatest.matchers.should.Matchers._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.data.{Form, FormError}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.BusinessNameForm._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.BusinessNameModel

class BusinessNameFormSpec extends PlaySpec with GuiceOneAppPerSuite {

  def form(excludedBusinessNames: Seq[BusinessNameModel] = Nil): Form[BusinessNameModel] = {
    businessNameValidationForm(excludedBusinessNames)
  }

  lazy val testNameValid = "business"
  lazy val testNameEmpty = ""
  lazy val testNameNotTooLong = "123456789-123456789-123456789-123456789-123456789-123456789-123456789-123456789-123456789-123456789-12345"
  lazy val testNameTooLong = "123456789-123456789-123456789-123456789-123456789-123456789-123456789-123456789-123456789-123456789-123456"
  lazy val testNameInvalidChar = "!@£$%^*():;"
  lazy val testNameValidChar = """abcdefghijklmnopqrstuvwxyz1234567890 ,.&'\/-"""

  "BusinessNameForm" should {

    "correctly validate a business name" when {
      "there are no excluded names" in {
        val testInput = Map(businessName -> testNameValid)
        val expected = BusinessNameModel(testNameValid)
        val actual = form().bind(testInput).value

        actual shouldBe Some(expected)
      }
      "there are excluded names but the user does not enter one of them" in {
        val testInput = Map(businessName -> testNameValid)
        val expected = BusinessNameModel(testNameValid)
        val actual = form(excludedBusinessNames = Seq(
          BusinessNameModel("nameOne"), BusinessNameModel("nameTwo")
        )).bind(testInput).value

        actual shouldBe Some(expected)
      }
    }

    "invalidate an empty business name" in {
      val testInput = Map(businessName -> testNameEmpty)

      val emptyTest = form().bind(testInput)
      emptyTest.errors must contain(FormError(businessName, "error.agent.business-name.empty"))
    }

    "invalidate a business name that is over 105 characters" in {
      val testInput = Map(businessName -> testNameTooLong)

      val tooLongTest = form().bind(testInput)
      tooLongTest.errors must contain(FormError(businessName, "error.agent.business-name.max-length"))
    }

    "validate a business name that is 105 characters" in {
      val testInput = Map(businessName -> testNameNotTooLong)

      val tooLongTest = form().bind(testInput)
      tooLongTest.errors must not contain(FormError(businessName, "error.agent.business-name.max-length"))
    }

    "invalidate a business name that includes invalid characters" in {

      for (invalidChar <- testNameInvalidChar) {
        val testInput = Map(businessName -> s"Some valid name except for ${invalidChar.toString} which is not allowed")

        val invalidCharTest = form().bind(testInput)
        if (!invalidCharTest.errors.nonEmpty)
          println(s"$invalidChar is not allowed")
        invalidCharTest.errors must contain(FormError(businessName, "error.agent.business-name.invalid-character"))
      }
    }

    "validate a business name that includes only valid characters" in {

      for (validChar <- testNameValidChar) {
        val testInput = Map(businessName -> s"Some valid name except for ${validChar.toString} which is not allowed")

        val validCharTest = form().bind(testInput)
        if (!validCharTest.errors.isEmpty)
          println(s"${validChar.toString} should be allowed")
        validCharTest.errors must not contain(FormError(businessName, "error.agent.business-name.invalid-character"))
      }
    }

    "invalidate a business name which is in the list of excluded business names" in {
      val testInput = Map(businessName -> "nameOne")
      val actual = form(excludedBusinessNames = Seq(
        BusinessNameModel("nameOne"), BusinessNameModel("nameTwo")
      )).bind(testInput)

      actual.errors must contain(FormError(businessName, "error.agent.business-trade-name.duplicate"))
    }

    "remove a leading space from business name" in {
      val testInput = Map(businessName -> (" " + testNameValid))
      val expected = BusinessNameModel(testNameValid)
      val leadingSpaceTest = form().bind(testInput).value

      leadingSpaceTest shouldBe Some(expected)
    }
  }
}
