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

import play.api.data.Forms.tuple
import play.api.data.validation.{Constraint, Invalid}
import play.api.data.{Form, Mapping}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.constraints.StringConstraints._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.formatters.DateModelMapping.dateModelMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.submapping.YesNoMapping.yesNoMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.submapping.{AccountingMethodMapping, YesNoMapping}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.ConstraintUtil.ConstraintUtil
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.MappingUtil.trimmedText
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.Accruals.ACCRUALS
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.Cash.CASH
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.AccountingPeriodUtil
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.AccountingPeriodUtil.getStartDateLimit

import java.time.LocalDate

object StreamlineIncomeSourceForm {
  val pageIdentifier: String = "full-income-source"

  val businessNameAndTradeAllowedCharacters = """^[A-Za-z0-9 ,.&'\\/-]*$"""

  val businessTradeName: String = "business-trade"
  val businessTradeNameMaxLength = 35
  val businessTradeNameMinLength = 2
  val tradeNameEmpty: Constraint[String] = nonEmpty(s"agent.error.$pageIdentifier.$businessTradeName.empty")
  val tradeTooLong: Constraint[String] = maxLength(businessTradeNameMaxLength, s"agent.error.$pageIdentifier.$businessTradeName.max-length")
  val tradeTooShort: Constraint[String] = minLettersLength(businessTradeNameMinLength, s"agent.error.$pageIdentifier.$businessTradeName.min-length")
  val tradeCharsValid: Constraint[String] = validateCharAgainst(businessNameAndTradeAllowedCharacters, s"agent.error.$pageIdentifier.$businessTradeName.invalid")

  val businessName = "business-name"
  private val businessNameMaxLength: Int = 105
  val nameNotEmpty: Constraint[String] = nonEmpty(s"agent.error.$pageIdentifier.$businessName.empty")
  val nameMaxLength: Constraint[String] = maxLength(businessNameMaxLength, s"agent.error.$pageIdentifier.$businessName.max-length")
  val nameValidChars: Constraint[String] = validateCharAgainst(businessNameAndTradeAllowedCharacters, s"agent.error.$pageIdentifier.$businessName.invalid-character")

  val startDate: String = "start-date"
  val startDateBeforeLimit: String = "start-date-before-limit"

  def maxStartDate: LocalDate = LocalDate.now().plusDays(6)

  def minStartDate: LocalDate = LocalDate.of(1900, 1, 1)

  def businessStartDate(f: LocalDate => String): Mapping[DateModel] = dateModelMapping(
    isAgent = true,
    errorContext = s"$pageIdentifier.$startDate",
    minDate = Some(minStartDate),
    maxDate = Some(maxStartDate),
    Some(f)
  )

  val accountingMethodBusiness: String = "accounting-method"
  val businessAccountingMethod: Mapping[AccountingMethod] = AccountingMethodMapping(
    errInvalid = Invalid(s"agent.error.$pageIdentifier.$accountingMethodBusiness.invalid"),
    errEmpty = Some(Invalid(s"agent.error.$pageIdentifier.$accountingMethodBusiness.invalid"))
  )

  def firstIncomeSourceForm(f: LocalDate => String): Form[(String, String, DateModel, AccountingMethod)] = Form(
    tuple(
      businessTradeName -> trimmedText.verifying(tradeNameEmpty andThen tradeCharsValid andThen tradeTooLong andThen tradeTooShort),
      businessName -> trimmedText.verifying(nameNotEmpty andThen nameMaxLength andThen nameValidChars),
      startDate -> businessStartDate(f),
      accountingMethodBusiness -> businessAccountingMethod
    )
  )

  def nextIncomeSourceForm(f: LocalDate => String): Form[(String, String, DateModel)] = Form(
    tuple(
      businessTradeName -> trimmedText.verifying(tradeNameEmpty andThen tradeCharsValid andThen tradeTooLong andThen tradeTooShort),
      businessName -> trimmedText.verifying(nameNotEmpty andThen nameMaxLength andThen nameValidChars),
      startDate -> businessStartDate(f)
    )
  )

  def firstIncomeSourceFormNoDate: Form[(String, String, YesNo, AccountingMethod)] = Form(
    tuple(
      businessTradeName -> trimmedText.verifying(tradeNameEmpty andThen tradeCharsValid andThen tradeTooLong andThen tradeTooShort),
      businessName -> trimmedText.verifying(nameNotEmpty andThen nameMaxLength andThen nameValidChars),
      startDateBeforeLimit -> yesNoMapping(
        yesNoInvalid = Invalid(s"agent.error.$pageIdentifier.$startDateBeforeLimit.invalid", AccountingPeriodUtil.getStartDateLimit.getYear.toString)
      ),
      accountingMethodBusiness -> businessAccountingMethod
    )
  )

  def nextIncomeSourceFormNoDate: Form[(String, String, YesNo)] = Form(
    tuple(
      businessTradeName -> trimmedText.verifying(tradeNameEmpty andThen tradeCharsValid andThen tradeTooLong andThen tradeTooShort),
      businessName -> trimmedText.verifying(nameNotEmpty andThen nameMaxLength andThen nameValidChars),
      startDateBeforeLimit -> yesNoMapping(
        yesNoInvalid = Invalid(s"agent.error.$pageIdentifier.$startDateBeforeLimit.invalid", AccountingPeriodUtil.getStartDateLimit.getYear.toString)
      )
    )
  )

  def createIncomeSourceData(maybeTradeName: Option[String],
                                  maybeBusinessName: Option[String],
                                  maybeStartDate: Option[DateModel],
                                  maybeStartDateBeforeLimit: Option[Boolean],
                                  maybeAccountingMethod: Option[AccountingMethod]): Map[String, String] = {

    val tradeNameMap: Map[String, String] = maybeTradeName.map(name => Map(businessTradeName -> name)).getOrElse(Map.empty)
    val businessNameMap: Map[String, String] = maybeBusinessName.map(name => Map(businessName -> name)).getOrElse(Map.empty)

    val dateMap: Map[String, String] = maybeStartDate.fold(Map.empty[String, String]) { date =>
      Map(
        s"$startDate-dateDay" -> date.day,
        s"$startDate-dateMonth" -> date.month,
        s"$startDate-dateYear" -> date.year
      )
    }

    val startDateBeforeLimitMap: Map[String, String] = {
      if (maybeStartDate.exists(_.toLocalDate.isBefore(getStartDateLimit))) {
        Map(startDateBeforeLimit -> YesNoMapping.option_yes)
      } else {
        maybeStartDateBeforeLimit.fold(
          if(maybeStartDate.exists(_.toLocalDate.isAfter(getStartDateLimit.minusDays(1)))) {
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

    val accountingMethodMap: Map[String, String] = maybeAccountingMethod.fold(Map.empty[String, String]) {
      case Cash => Map(accountingMethodBusiness -> CASH)
      case Accruals => Map(accountingMethodBusiness -> ACCRUALS)
    }

    tradeNameMap ++ businessNameMap ++ dateMap ++ startDateBeforeLimitMap ++ accountingMethodMap

  }
}