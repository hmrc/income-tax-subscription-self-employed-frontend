/*
 * Copyright 2026 HM Revenue & Customs
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
import play.api.data.Forms.seq
import play.api.data.validation.Constraint
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.constraints.StringConstraints.*
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.ConstraintUtil.ConstraintUtil
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.MappingUtil.trimmedText

object DisplayNameForm {

  val displayName: String = "business-display-name"
  val pageIdentifier: String = "display-name"

  private val displayNameAndTradeAllowedCharacters = """^[A-Za-z0-9 ,.&'\\/-]*$"""
  private val displayNameMaxLength: Int = 35
  private val displayNameMinLength: Int = 2

  val nameNotEmpty: Constraint[String] = nonEmpty(s"individual.error.$pageIdentifier.$displayName.empty")
  val nameMaxLength: Constraint[String] = maxLength(displayNameMaxLength, s"individual.error.$pageIdentifier.$displayName.max-length")
  val nameMinLength: Constraint[String] = minLettersLength(displayNameMinLength, s"individual.error.$pageIdentifier.$displayName.min-length")
  val nameValidChars: Constraint[String] = validateCharAgainst(displayNameAndTradeAllowedCharacters, s"individual.error.$pageIdentifier.$displayName.invalid")

  private val displayNameMapping =
    trimmedText.verifying(nameNotEmpty andThen nameMaxLength andThen nameValidChars andThen nameMinLength)

  val displayNameForm: Form[Seq[String]] =
    Form(displayName -> seq(displayNameMapping))

  def populateNameData(maybeDisplayNames: Seq[Option[String]]): Map[String, String] =
    maybeDisplayNames.zipWithIndex.map {
      case (maybeName, index) => s"$displayName[$index]" -> maybeName.getOrElse("")
    }.toMap

}