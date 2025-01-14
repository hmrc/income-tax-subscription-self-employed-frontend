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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities

import java.time.LocalDate
import java.time.Month.APRIL

object AccountingPeriodUtil {

  private val SIXTH = 6

  def getCurrentTaxYearStartDate: LocalDate = {
    val now: LocalDate = LocalDate.now

    if (now.isBefore(LocalDate.of(now.getYear, APRIL.getValue, SIXTH))) {
      LocalDate.of(now.getYear - 1, APRIL.getValue, SIXTH)
    } else {
      LocalDate.of(now.getYear, APRIL.getValue, SIXTH)
    }
  }

  def getStartDateLimit: LocalDate = getCurrentTaxYearStartDate.minusYears(2)

}
