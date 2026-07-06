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
import play.api.data.Forms.single
import play.api.data.validation.Constraint
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.constraints.StringConstraints._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.ConstraintUtil.ConstraintUtil
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.MappingUtil.trimmedText

object BusinessTradeNameForm {

  val businessTradeName: String = "business-trade"
  val pageIdentifier: String = "full-income-source"

  private val businessNameAndTradeAllowedCharacters = """^[A-Za-z0-9 ,.&'\\/-]*$"""
  private val businessTradeNameMaxLength: Int = 35
  private val businessTradeNameMinLength: Int = 2

  val tradeNameEmpty: Constraint[String] = nonEmpty(s"individual.error.$pageIdentifier.$businessTradeName.empty")
  val tradeNameMaxLength: Constraint[String] = maxLength(businessTradeNameMaxLength, s"individual.error.$pageIdentifier.$businessTradeName.max-length")
  val tradeNameMinLength: Constraint[String] = minLettersLength(businessTradeNameMinLength, s"individual.error.$pageIdentifier.$businessTradeName.min-length")
  val tradeNameValidChars: Constraint[String] = validateCharAgainst(businessNameAndTradeAllowedCharacters, s"individual.error.$pageIdentifier.$businessTradeName.invalid")

  val businessTradeNameForm: Form[String] = Form(
    single(
      businessTradeName -> trimmedText.verifying(tradeNameEmpty andThen tradeNameMaxLength andThen tradeNameValidChars andThen tradeNameMinLength)
    )
  )

  def createBusinessTradeNameData(maybeTradeName: Option[String]): Map[String, String] =
    maybeTradeName.fold(Map.empty[String, String])(trade => Map(businessTradeName -> trade))

}