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
import play.api.data.{Form, Mapping}
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.constraints.StringConstraints.{businessNameValidateChar, maxLength, minLettersLength, nonEmpty, validateCharAgainst}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.formatters.DateModelMapping.dateModelMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.submapping.AccountingMethodMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.ConstraintUtil.{ConstraintUtil, constraint}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.MappingUtil.trimmedText
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.Accruals.ACCRUALS
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.Cash.CASH
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{AccountingMethod, Accruals, Cash, DateModel}

import java.time.LocalDate

object FirstIncomeSourceForm {

  val businessTradeName: String = "businessTradeName"

  val businessTradeNameMaxLength = 35
  val businessTradeNameMinLength = 2
  val tradeNameEmpty: Constraint[String] = nonEmpty("error.agent.business-trade-name.empty")
  val nameTooLong: Constraint[String] = maxLength(businessTradeNameMaxLength, "error.agent.business-trade-name.max-length")
  val nameTooShort: Constraint[String] = minLettersLength(businessTradeNameMinLength, "error.agent.business-trade-name.min-length")
  val nameCharsValid: Constraint[String] = businessNameValidateChar("error.agent.business-trade-name.invalid")
  val businessName = "businessName"

  private val businessNameMaxLength: Int = 105

  val nameNotEmpty: Constraint[String] = nonEmpty("error.agent.business-name.empty")
  val nameMaxLength: Constraint[String] = maxLength(businessNameMaxLength, "error.agent.business-name.max-length")
  val businessTradeNameSpec = """^[A-Za-z0-9 ,.&'\\/-]*$"""
  val nameValidChars: Constraint[String] = validateCharAgainst(businessTradeNameSpec, "error.agent.business-name.invalid-character")

  val startDate: String = "startDate"
  val accountingMethodBusiness: String = "accountingMethodBusiness"
  val errorContext: String = "business"

  def hasDuplicateTradeNames(excludedNames: Seq[String]): Constraint[String] = constraint[String] { trade =>
    if (excludedNames.contains(trade)) Invalid("error.agent.business-trade-name.duplicate")
    else Valid
  }

  def nameIsNotExcluded(excludedNames: Seq[String]): Constraint[String] = constraint[String] { name =>
    if (excludedNames.contains(name)) Invalid("error.agent.business-trade-name.duplicate")
    else Valid
  }
  def maxStartDate: LocalDate = LocalDate.now().plusDays(6)
  def minStartDate: LocalDate = LocalDate.of(1900, 1, 1)

  def businessStartDate(f: LocalDate => String): Mapping[DateModel] = dateModelMapping(
    isAgent = true,
    errorContext = errorContext,
    minDate = Some(minStartDate),
    maxDate = Some(maxStartDate),
    Some(f)
  )

  val businessAccountingMethod: Mapping[AccountingMethod] = AccountingMethodMapping(
    errInvalid = Invalid("agent.error.accounting-method-property.invalid"),
    errEmpty = Some(Invalid("agent.error.accounting-method-property.invalid"))
  )

  def firstIncomeSourceForm(f: LocalDate => String): Form[(String, String, DateModel, AccountingMethod)] = Form(
    tuple(
      businessTradeName -> trimmedText.verifying(tradeNameEmpty andThen nameCharsValid andThen nameTooLong andThen nameTooShort),
      businessName -> trimmedText.verifying(nameNotEmpty andThen nameMaxLength andThen nameValidChars),
      startDate -> businessStartDate(f),
      accountingMethodBusiness -> businessAccountingMethod
    )
  )

  def createFirstIncomeSourceData(maybeTradeName: Option[String], maybeBusinessName: Option[String], maybeStartDate: Option[DateModel], maybeAccountingMethod: Option[AccountingMethod]): Map[String, String] = {

    val tradeNameMap: Map[String, String] = maybeTradeName.map(name => Map(businessTradeName -> name)).getOrElse(Map.empty)
    val businessNameMap: Map[String, String] = maybeBusinessName.map(name => Map(businessName -> name)).getOrElse(Map.empty)

    val dateMap: Map[String, String] = maybeStartDate.fold(Map.empty[String, String]) { date =>
      Map(
        s"$startDate-dateDay" -> date.day,
        s"$startDate-dateMonth" -> date.month,
        s"$startDate-dateYear" -> date.year
      )
    }

    val accountingMethodMap: Map[String, String] = maybeAccountingMethod.fold(Map.empty[String, String]) {
      case Cash => Map(accountingMethodBusiness -> CASH)
      case Accruals => Map(accountingMethodBusiness -> ACCRUALS)
    }

    tradeNameMap ++ businessNameMap ++ dateMap ++ accountingMethodMap

  }
}