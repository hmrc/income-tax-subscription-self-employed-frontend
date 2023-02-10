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

import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.constraints.StringConstraints.{maxLength, minLength, nonEmpty, validateCharAgainst}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.ConstraintUtil.{ConstraintUtil, constraint}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.MappingUtil.trimmedText
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.BusinessTradeNameModel

object BusinessTradeNameForm {

  val businessTradeName: String = "businessTradeName"

  val businessTradeNameMaxLength = 35
  val businessTradeNameMinLength = 2

  val tradeNameEmpty: Constraint[String] = nonEmpty("error.business-trade-name.empty")
  val nameTooLong: Constraint[String] = maxLength(businessTradeNameMaxLength, "error.business-trade-name.max-length")
  val nameTooShort: Constraint[String] = minLength(businessTradeNameMinLength, "error.business-trade-name.min-length")
  val businessTradeNameSpec = """^[A-Za-z0-9 ,.&'\\/-]*$"""
  val nameValidChars: Constraint[String] = validateCharAgainst(businessTradeNameSpec, "error.business-trade-name.invalid")

  def hasDuplicateTradeNames(excludedNames: Seq[BusinessTradeNameModel]): Constraint[String] = constraint[String] { tradeName =>
    if (excludedNames.exists(_.businessTradeName == tradeName)) Invalid("error.business-trade-name.duplicate")
    else Valid
  }

  def businessTradeNameValidationForm(excludedBusinessTradeNames: Seq[BusinessTradeNameModel]): Form[BusinessTradeNameModel] = Form(
    mapping(
      businessTradeName -> trimmedText.verifying(
        tradeNameEmpty andThen nameTooLong andThen nameTooShort andThen nameValidChars andThen hasDuplicateTradeNames(excludedBusinessTradeNames)
      )
    )(BusinessTradeNameModel.apply)(BusinessTradeNameModel.unapply)
  )
}
