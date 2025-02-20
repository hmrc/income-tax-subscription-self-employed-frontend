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
import play.api.data.Forms.tuple
import play.api.data.validation.{Constraint, Invalid}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.constraints.StringConstraints._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.submapping.YesNoMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.submapping.YesNoMapping.yesNoMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.ConstraintUtil.ConstraintUtil
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.MappingUtil.trimmedText
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.AccountingPeriodUtil
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.AccountingPeriodUtil.getStartDateLimit

object StreamlineIncomeSourceForm {
  val pageIdentifier: String = "full-income-source"
  val businessNameAndTradeAllowedCharacters = """^[A-Za-z0-9 ,.&'\\/-]*$"""

  val businessTradeName: String = "business-trade"
  val businessTradeNameMaxLength = 35
  val businessTradeNameMinLength = 2
  val tradeNameEmpty: Constraint[String] = nonEmpty(s"individual.error.$pageIdentifier.$businessTradeName.empty")
  val tradeTooLong: Constraint[String] = maxLength(businessTradeNameMaxLength, s"individual.error.$pageIdentifier.$businessTradeName.max-length")
  val tradeTooShort: Constraint[String] = minLettersLength(businessTradeNameMinLength, s"individual.error.$pageIdentifier.$businessTradeName.min-length")
  val tradeCharsValid: Constraint[String] = validateCharAgainst(
    businessNameAndTradeAllowedCharacters, s"individual.error.$pageIdentifier.$businessTradeName.invalid")

  val businessName = "business-name"
  private val businessNameMaxLength: Int = 105
  private val businessNameMinLength: Int = 2
  val nameNotEmpty: Constraint[String] = nonEmpty(s"individual.error.$pageIdentifier.$businessName.empty")
  val nameMaxLength: Constraint[String] = maxLength(businessNameMaxLength, s"individual.error.$pageIdentifier.$businessName.max-length")
  val nameMinLength: Constraint[String] = minLettersLength(businessNameMinLength, s"individual.error.$pageIdentifier.$businessName.min-length")
  val nameValidChars: Constraint[String] = validateCharAgainst(
    businessNameAndTradeAllowedCharacters, s"individual.error.$pageIdentifier.$businessName.invalid")

  val startDateBeforeLimit: String = "start-date-before-limit"

  def fullIncomeSourceForm: Form[(String, String, YesNo)] = Form(
    tuple(
      businessTradeName -> trimmedText.verifying(tradeNameEmpty andThen tradeCharsValid andThen tradeTooLong andThen tradeTooShort),
      businessName -> trimmedText.verifying(nameNotEmpty andThen nameValidChars andThen nameMaxLength andThen nameMinLength),
      startDateBeforeLimit -> yesNoMapping(
        yesNoInvalid = Invalid(s"individual.error.$pageIdentifier.$startDateBeforeLimit.empty", AccountingPeriodUtil.getStartDateLimit.getYear.toString)
      )
    )
  )

  def createIncomeSourceData(maybeTradeName: Option[String],
                             maybeBusinessName: Option[String],
                             maybeStartDate: Option[DateModel],
                             maybeStartDateBeforeLimit: Option[Boolean]): Map[String, String] = {

    val tradeNameMap: Map[String, String] = maybeTradeName.map(name => Map(businessTradeName -> name)).getOrElse(Map.empty)
    val businessNameMap: Map[String, String] = maybeBusinessName.map(name => Map(businessName -> name)).getOrElse(Map.empty)

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

    tradeNameMap ++ businessNameMap ++ startDateBeforeLimitMap

  }
}