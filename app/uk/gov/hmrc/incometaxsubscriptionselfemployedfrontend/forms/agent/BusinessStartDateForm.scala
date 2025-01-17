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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.formatters.DateModelMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.DateModel
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.AccountingPeriodUtil

import java.time.LocalDate

object BusinessStartDateForm {

  def maxStartDate: LocalDate = LocalDate.now().plusDays(6)

  def minStartDate: LocalDate = AccountingPeriodUtil.getStartDateLimit

  val startDate: String = "startDate"

  val errorContext: String = "business-start-date"

  def businessStartDateForm(minStartDate: LocalDate, maxStartDate: LocalDate, f: LocalDate => String): Form[DateModel] = Form(
    single(
      startDate -> DateModelMapping.dateModelMapping(
        isAgent = true,
        errorContext = errorContext,
        minDate = Some(minStartDate),
        maxDate = Some(maxStartDate),
        dateFormatter = Some(f)
      )
    )
  )

}
