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

import play.api.data.Form
import play.api.data.Forms.single
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.constraints.StringConstraints._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.ConstraintUtil.{ConstraintUtil, constraint}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.MappingUtil.trimmedText

object BusinessTradeNameForm {

  val businessTradeName: String = "businessTradeName"

  val businessTradeNameMaxLength = 35
  val businessTradeNameMinLength = 2

  val tradeNameEmpty: Constraint[String] = nonEmpty("error.business-trade-name.empty")
  val nameTooLong: Constraint[String] = maxLength(businessTradeNameMaxLength, "error.business-trade-name.max-length")
  val nameTooShort: Constraint[String] = minLettersLength(businessTradeNameMinLength, "error.business-trade-name.min-length")
  val nameCharsValid: Constraint[String] = businessNameValidateChar("error.business-trade-name.invalid")

  def hasDuplicateTradeNames(excludedNames: Seq[String]): Constraint[String] = constraint[String] { tradeName =>
    if (excludedNames.contains(tradeName)) Invalid("error.business-trade-name.duplicate")
    else Valid
  }

  def businessTradeNameValidationForm(excludedBusinessTradeNames: Seq[String]): Form[String] = Form(
    single(
      businessTradeName -> trimmedText.verifying(
        tradeNameEmpty andThen nameCharsValid andThen nameTooLong andThen nameTooShort andThen hasDuplicateTradeNames(excludedBusinessTradeNames)
      )
    )
  )
}
