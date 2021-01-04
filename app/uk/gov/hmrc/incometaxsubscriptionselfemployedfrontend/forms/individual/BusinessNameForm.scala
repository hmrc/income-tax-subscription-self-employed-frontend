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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual

import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.constraints.StringConstraints.{maxLength, nonEmpty, validateChar}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.ConstraintUtil.{ConstraintUtil, constraint}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.MappingUtil.trimmedText
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.BusinessNameModel

object BusinessNameForm {

  val businessName = "businessName"

  private val businessNameMaxLength: Int = 160

  val nameNotEmpty: Constraint[String] = nonEmpty("error.business_name.empty")
  val nameMaxLength: Constraint[String] = maxLength(businessNameMaxLength, "error.business_name.max_length")
  val nameValidChars: Constraint[String] = validateChar("error.business_name.invalid_character")

  def nameIsNotExcluded(excludedNames: Seq[BusinessNameModel]): Constraint[String] = constraint[String] { name =>
    if (excludedNames.exists(_.businessName == name)) Invalid("error.business_trade_name.duplicate")
    else Valid
  }

  def businessNameValidationForm(excludedBusinessNames: Seq[BusinessNameModel]): Form[BusinessNameModel] = Form(
    mapping(
      businessName -> trimmedText.verifying(
        nameNotEmpty andThen nameMaxLength andThen nameValidChars andThen nameIsNotExcluded(excludedBusinessNames)
      )
    )(BusinessNameModel.apply)(BusinessNameModel.unapply)
  )
}
