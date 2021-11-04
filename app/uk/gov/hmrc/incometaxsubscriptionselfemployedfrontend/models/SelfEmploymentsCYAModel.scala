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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models

import play.api.libs.json._

case class SelfEmploymentsCYAModel(id: String,
                                   confirmed: Boolean = false,
                                   businessStartDate: Option[BusinessStartDate] = None,
                                   businessName: Option[BusinessNameModel] = None,
                                   businessTradeName: Option[BusinessTradeNameModel] = None,
                                   businessAddress: Option[BusinessAddressModel] = None,
                                   businessAddressRedirect: Option[String] = None,
                                   accountingMethod: Option[AccountingMethodModel] = None) {

  val businessStartDateComplete: Boolean = businessStartDate.isDefined

  val businessNameComplete: Boolean = businessName.isDefined

  val businessTradeNameComplete: Boolean = businessTradeName.isDefined

  val businessAddressComplete: Boolean = businessAddress.isDefined

  val accountingMethodComplete: Boolean = accountingMethod.isDefined

  val isComplete: Boolean = {
    businessStartDateComplete &&
      businessNameComplete &&
      businessTradeNameComplete &&
      businessAddressComplete &&
      accountingMethodComplete
  }

}

object SelfEmploymentsCYAModel {

  implicit val format: Format[SelfEmploymentsCYAModel] = Json.format[SelfEmploymentsCYAModel]

  def apply(id: String, selfEmployment: Option[SelfEmploymentData], accountingMethod: Option[AccountingMethodModel]): SelfEmploymentsCYAModel = {
    SelfEmploymentsCYAModel(
      id = id,
      businessStartDate = selfEmployment.flatMap(_.businessStartDate),
      businessName = selfEmployment.flatMap(_.businessName),
      businessTradeName = selfEmployment.flatMap(_.businessTradeName),
      businessAddress = selfEmployment.flatMap(_.businessAddress),
      businessAddressRedirect = selfEmployment.flatMap(_.addressRedirect),
      accountingMethod = accountingMethod
    )
  }

}