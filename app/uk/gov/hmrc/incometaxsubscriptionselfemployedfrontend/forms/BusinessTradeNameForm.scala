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

import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.validation.StringConstraints._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.validation.utils.ConstraintUtil._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.validation.utils.MappingUtil._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{BusinessNameModel, BusinessTradeNameModel, SelfEmploymentData}

object BusinessTradeNameForm {

  val businessTradeName: String = "businessTradeName"

  val businessTradeNameMaxLength = 160

  val tradeNameEmpty: Constraint[String] = nonEmpty("error.business_trade_name.empty")
  val nameTooLong: Constraint[String] = maxLength(businessTradeNameMaxLength, "error.business_trade_name.maxLength")
  val tradeNameInvalidCharacters: Constraint[String] = validateChar("error.business_trade_name.invalid")

  def duplicateNameTrade(businessName: String, businesses: Seq[SelfEmploymentData]): Constraint[String] = constraint[String] { trade =>

    val hasDuplicateNameTrade: Boolean = businesses.exists(business =>
      business.businessName.exists(_.businessName == businessName) &&
        business.businessTradeName.exists(_.businessTradeName == trade)
    )

    if (hasDuplicateNameTrade) {
      Invalid("error.business_trade_name.duplicate")
    } else {
      Valid
    }

  }

  def businessTradeNameValidationForm(businessName: String, businesses: Seq[SelfEmploymentData]): Form[BusinessTradeNameModel] = Form(
    mapping(
      businessTradeName -> oText.toText.verifying(
        tradeNameEmpty andThen nameTooLong andThen tradeNameInvalidCharacters andThen duplicateNameTrade(businessName, businesses)
      )
    )(BusinessTradeNameModel.apply)(BusinessTradeNameModel.unapply)
  )
}
