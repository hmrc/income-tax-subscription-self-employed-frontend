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
import play.api.data.validation.Constraint
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.constraints.StringConstraints.*
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.ConstraintUtil.ConstraintUtil
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.MappingUtil.trimmedText

object BusinessNameForm {

  val businessName: String = "business-name"
  val pageIdentifier: String = "full-income-source"

  private val businessNameAndTradeAllowedCharacters = """^[A-Za-z0-9 ,.&'\\/-]*$"""
  private val businessNameMaxLength: Int = 105
  private val businessNameMinLength: Int = 2

  val nameNotEmpty: Constraint[String] = nonEmpty(s"individual.error.$pageIdentifier.$businessName.empty")
  val nameMaxLength: Constraint[String] = maxLength(businessNameMaxLength, s"individual.error.$pageIdentifier.$businessName.max-length")
  val nameMinLength: Constraint[String] = minLettersLength(businessNameMinLength, s"individual.error.$pageIdentifier.$businessName.min-length")
  val nameValidChars: Constraint[String] = validateCharAgainst(businessNameAndTradeAllowedCharacters, s"individual.error.$pageIdentifier.$businessName.invalid-character")

  val businessNameForm: Form[String] = Form(
    single(
      businessName -> trimmedText.verifying(nameNotEmpty andThen nameMaxLength andThen nameValidChars andThen nameMinLength)
    )
  )

  def createBusinessNameData(maybeBusinessName: Option[String]): Map[String, String] =
    maybeBusinessName.fold(Map.empty[String, String])(name => Map(businessName -> name))

}