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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.formatters.DateModelMapping.dateModelMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.ConstraintUtil.{ConstraintUtil, constraint}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{BusinessStartDate, DateModel}

import java.time.LocalDate

object BusinessStartDateForm {

  def maxStartDate: LocalDate = LocalDate.now().minusYears(2)

  def minStartDate: LocalDate = LocalDate.of(1900, 1, 1)

  val startDate: String = "startDate"

  val errorContext: String = "business_start_date"

  def startBeforeTwoYears(date: String): Constraint[DateModel] = constraint[DateModel] { dateModel =>
    if (DateModel.dateConvert(dateModel).isAfter(maxStartDate)) {
      Invalid(s"error.$errorContext.maxStartDate", date)
    } else {
      Valid
    }
  }

  def earliestStartDate(date: String): Constraint[DateModel] = constraint[DateModel] { dateModel =>
    val earliestAllowedYear: Int = minStartDate.getYear
    if (dateModel.year.toInt < earliestAllowedYear) {
      Invalid(s"error.$errorContext.minStartDate", date)
    } else {
      Valid
    }
  }

  def businessStartDateForm(minStartDate: String, maxStartDate: String): Form[BusinessStartDate] = Form(
    mapping(
      startDate -> dateModelMapping(errorContext = errorContext).verifying(
        startBeforeTwoYears(maxStartDate) andThen earliestStartDate(minStartDate)
      )
    )(BusinessStartDate.apply)(BusinessStartDate.unapply)
  )

}
