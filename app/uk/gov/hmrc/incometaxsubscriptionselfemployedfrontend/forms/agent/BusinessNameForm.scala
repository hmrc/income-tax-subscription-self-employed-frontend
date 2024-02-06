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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent

import play.api.data.Form
import play.api.data.Forms.single
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.constraints.StringConstraints._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.ConstraintUtil._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.MappingUtil._

object BusinessNameForm {

  /**
   * Spec:
   * "tradingName": {
   * "description": "Trading name",
   * "type": "string",
   * "pattern": "^[A-Za-z0-9 ,.&'\/-]{1,105}$"
   * }
   */
  val businessName = "businessName"

  private val businessNameMaxLength: Int = 105

  val nameNotEmpty: Constraint[String] = nonEmpty("error.agent.business-name.empty")
  val nameMaxLength: Constraint[String] = maxLength(businessNameMaxLength, "error.agent.business-name.max-length")
  val businessTradeNameSpec = """^[A-Za-z0-9 ,.&'\\/-]*$"""
  val nameValidChars: Constraint[String] = validateCharAgainst(businessTradeNameSpec, "error.agent.business-name.invalid-character")

  def nameIsNotExcluded(excludedNames: Seq[String]): Constraint[String] = constraint[String] { name =>
    if (excludedNames.contains(name)) Invalid("error.agent.business-trade-name.duplicate")
    else Valid
  }

  //Default value for excludedBusinessNames can be removed when multiple self-employed is implemented
  def businessNameValidationForm(excludedBusinessNames: Seq[String] = Seq()): Form[String] = Form(
    single(
      businessName -> trimmedText.verifying(
        nameNotEmpty andThen nameMaxLength andThen nameValidChars andThen nameIsNotExcluded(excludedBusinessNames)
      )
    )
  )
}
