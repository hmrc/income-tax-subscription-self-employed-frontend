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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.validation

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.data.validation.{Constraints, Invalid, Valid}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.constraints.StringConstraints

class StringConstraintsSpec extends Constraints with AnyWordSpecLike with Matchers {

  val maxLength = 2
  val errMsgMaxLength = "Too Long"
  val errMsgNonEmpty = "it is empty"
  val errMsgInvalidChar = "there are invalid chars"
  val errMsgNoLeadingSpace = "there are leading spaces"

  "The StringConstraints.maxLength method" when {

    "supplied with a string which exceeds the max length" should {
      "return invalid with the correct message" in {
        StringConstraints.maxLength(maxLength, errMsgMaxLength)("abc") shouldBe Invalid(errMsgMaxLength)
      }
    }

    "supplied with a string which equals the max length" should {
      "return valid" in {
        StringConstraints.maxLength(maxLength, errMsgMaxLength)("ab") shouldBe Valid
      }
    }

    "supplied with a string which is less than the max length" should {
      "return valid" in {
        StringConstraints.maxLength(maxLength, errMsgMaxLength)("a") shouldBe Valid
      }
    }
  }

  "The StringConstraints.nonEmpty method" when {

    "supplied with empty value" should {
      "return invalid" in {
        StringConstraints.nonEmpty(errMsgNonEmpty)("") shouldBe Invalid(errMsgNonEmpty)
      }
    }

    "supplied with some value" should {
      "return valid" in {
        StringConstraints.nonEmpty(errMsgNonEmpty)("someValue") shouldBe Valid
      }
    }
  }

  "The StringConstraints.validateChar method" when {

    "supplied with a valid string" should {
      "return valid" in {
        val lowerCaseAlphabet = ('a' to 'z').mkString
        val upperCaseAlphabet = lowerCaseAlphabet.toUpperCase()
        val oneToNine = (1 to 9).mkString
        val otherChar = "&@£$€¥#.,:;-"
        val space = ""

        StringConstraints.validateChar(errMsgInvalidChar)(lowerCaseAlphabet + upperCaseAlphabet + space + oneToNine + otherChar + space) shouldBe Valid
      }
    }

    "supplied with a string which contains invalid characters" should {
      "return invalid" in {
        StringConstraints.validateChar(errMsgInvalidChar)("!()+{}?^~") shouldBe Invalid(errMsgInvalidChar)
      }
    }
  }

  "The StringConstraints.noLeadingSpace method" when {

    "supplied with a string which contains no leading space" should {
      "return valid" in {

        StringConstraints.noLeadingSpace(errMsgNoLeadingSpace)("TEST Business") shouldBe Valid
      }
    }

    "supplied with a string which contains a leading space" should {
      "return invalid" in {
        StringConstraints.noLeadingSpace(errMsgNoLeadingSpace)(" TEST Business") shouldBe Invalid(errMsgNoLeadingSpace)
      }
    }

    "supplied with a string which contains two leading spaces" should {
      "return invalid" in {
        StringConstraints.noLeadingSpace(errMsgNoLeadingSpace)("  TEST Business") shouldBe Invalid(errMsgNoLeadingSpace)
      }
    }
  }

}
