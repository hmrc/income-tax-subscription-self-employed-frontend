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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.constraints

import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.ConstraintUtil.constraint
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.DateModel

import java.time.LocalDate

object StringConstraints {


  val charRegex = """^([ A-Za-z0-9&@£$€¥#.,:;-])*$"""
  val businessNameChars = """^([ A-Za-z0-9&'\/\\.,-])*$"""


  val validateChar: String => Constraint[String] = msgKey => constraint[String](
    x => if (x.matches(charRegex)) Valid else Invalid(msgKey)
  )

  val businessNameValidateChar: String => Constraint[String] = msgKey => constraint[String](
    x => if (x.matches(businessNameChars)) Valid else Invalid(msgKey)
  )

  val validateCharAgainst: (String, String) => Constraint[String] = (test, msgKey) => constraint[String](
    x => if (x.matches(test)) Valid else Invalid(msgKey)
  )

  val nonEmpty: String => Constraint[String] = msgKey => constraint[String](
    x => if (x.isEmpty) Invalid(msgKey) else Valid
  )

  val maxLength: (Int, String) => Constraint[String] = (length, msgKey) => constraint[String](
    x => if (x.trim.length > length) Invalid(msgKey) else Valid
  )

  val minLettersLength: (Int, String) => Constraint[String] = (length, msgKey) => constraint[String](
    x => if (x.count(_.isLetter) < length) Invalid(msgKey) else Valid
  )

  val noLeadingSpace: String => Constraint[String] = msgKey => constraint[String](
    x => if (x.headOption.contains(" ".head)) Invalid(msgKey) else Valid
  )


  def isAfter(
    minDate: LocalDate,
    errorContext: String,
    convert: LocalDate => String,
    prefix: Option[String] = None
  ): Constraint[DateModel] =
    constraint[DateModel] { dateModel =>
      if (dateModel.toLocalDate.isAfter(minDate.minusDays(1))) {
        Valid
      } else {
        Invalid(s"${prefix.getOrElse("")}error.$errorContext.day-month-year.min-date", convert(minDate))
      }
    }

  def isBefore(
    maxDate: LocalDate,
    errorContext: String,
    convert: LocalDate => String,
    prefix: Option[String] = None
  ): Constraint[DateModel] =
    constraint[DateModel] { dateModel =>
      val date = maxDate.plusDays(1)
      if (dateModel.toLocalDate.isBefore(date)) {
        Valid
      } else {
        Invalid(s"${prefix.getOrElse("")}error.$errorContext.day-month-year.max-date", convert(date))
      }
    }

}
