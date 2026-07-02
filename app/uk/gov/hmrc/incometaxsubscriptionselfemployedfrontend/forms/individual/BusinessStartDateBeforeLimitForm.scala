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
import play.api.data.validation.Invalid
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.submapping.YesNoMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.submapping.YesNoMapping.yesNoMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{DateModel, YesNo}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.AccountingPeriodUtil
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.AccountingPeriodUtil.getStartDateLimit

import scala.collection.Iterable.single

object BusinessStartDateBeforeLimitForm {

  val startDateBeforeLimit: String = "start-date-before-limit"

  def businessStartDateBeforeLimitForm: Form[YesNo] = Form(
      startDateBeforeLimit -> yesNoMapping(
        yesNoInvalid = Invalid(
          "individual.error.start-date-before-limit.empty", AccountingPeriodUtil.getStartDateLimit.getYear.toString
        )
    )
  )

  def createStartDateBeforeLimitData(maybeStartDate: Option[DateModel],
                                     maybeStartDateBeforeLimit: Option[Boolean]): Map[String, String] = {

    val startDateBeforeLimitMap: Map[String, String] = {
      if (maybeStartDate.exists(_.toLocalDate.isBefore(getStartDateLimit))) {
        Map(startDateBeforeLimit -> YesNoMapping.option_yes)
      } else {
        maybeStartDateBeforeLimit.fold(
          if (maybeStartDate.exists(_.toLocalDate.isAfter(getStartDateLimit.minusDays(1)))) {
            Map(startDateBeforeLimit -> YesNoMapping.option_no)
          } else {
            Map.empty[String, String]
          }
        ) {
          case true => Map(startDateBeforeLimit -> YesNoMapping.option_yes)
          case false => Map(startDateBeforeLimit -> YesNoMapping.option_no)
        }
      }
    }
    startDateBeforeLimitMap
  }
}
