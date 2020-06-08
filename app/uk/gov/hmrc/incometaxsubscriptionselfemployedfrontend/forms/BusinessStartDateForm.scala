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

import java.time.LocalDate

import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationResult}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.submapping.DateMapping._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.validation.utils.ConstraintUtil._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{BusinessStartDate, DateModel}

import scala.util.Try

object BusinessStartDateForm {

  def minStartDate: LocalDate = LocalDate.now().minusYears(2)

  val startDate: String = "startDate"

  val dateValidation: Constraint[(Option[String], Option[String], Option[String])] = constraint[(Option[String], Option[String], Option[String])] {
    case (day, month, year) => {
      lazy val invalidDate = Invalid("error.date.empty")
      Try[ValidationResult] {
        LocalDate.of(year.get.toInt, month.get.toInt, day.get.toInt)
        Valid
      }.getOrElse(invalidDate)
    }
  }

  val validateDate: Constraint[(Option[String], Option[String], Option[String])] = constraint[(Option[String], Option[String], Option[String])] {
    case (None, None, None) => Invalid("error.date.empty")
    case (Some(_), None, None) => Invalid("error.month.year.empty")
    case (None, None, Some(_)) => Invalid("error.day.month.empty")
    case (None, Some(_), None) => Invalid("error.day.year.empty")
    case (Some(_), Some(_), None) => Invalid("error.year.empty")
    case (None, Some(_), Some(_)) => Invalid("error.day.empty")
    case (Some(_), None, Some(_)) => Invalid("error.month.empty")
    case (Some(_), Some(_), Some(_)) => Valid


  }

  private val toDateModel: (Option[String], Option[String], Option[String]) => DateModel = {
    case (day, month, year) => DateModel(day.get, month.get, year.get)
  }

  private val fromDateModel: DateModel => (Option[String], Option[String], Option[String]) = {
    dateModel => (Some(dateModel.day), Option(dateModel.month), Option(dateModel.year))
  }


  def startBeforeTwoYears(date: String): Constraint[DateModel] = constraint[DateModel](
    dateModel => {
      lazy val invalid = Invalid("error.business_accounting_period.minStartDate", date)
      if (DateModel.dateConvert(dateModel).isAfter(minStartDate)) invalid else Valid
    }
  )

  def businessStartDateForm(date: String): Form[BusinessStartDate] = Form(
    mapping(
      startDate -> optDateMapping.verifying(validateDate andThen dateValidation).
        transform[DateModel](toDateModel.tupled, fromDateModel).verifying(startBeforeTwoYears(date))
    )(BusinessStartDate.apply)(BusinessStartDate.unapply)
  )
}

