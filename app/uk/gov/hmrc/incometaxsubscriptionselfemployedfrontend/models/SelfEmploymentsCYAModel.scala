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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models

case class SelfEmploymentsCYAModel(id: String,
                                   confirmed: Boolean = false,
                                   startDateBeforeLimit: Option[Boolean] = None,
                                   businessStartDate: Option[DateModel] = None,
                                   businessName: Option[String] = None,
                                   businessTradeName: Option[String] = None,
                                   businessAddress: Option[Address] = None) {

  private val businessStartDateComplete: Boolean = businessStartDate.isDefined
  private val businessNameComplete: Boolean = businessName.isDefined
  private val businessTradeNameComplete: Boolean = businessTradeName.isDefined
  private val businessAddressComplete: Boolean = businessAddress.isDefined

  val isComplete: Boolean = {
    startDateBeforeLimit match {
      case Some(true) =>
        businessNameComplete &&
          businessTradeNameComplete &&
          businessAddressComplete
      case _ =>
        businessStartDateComplete &&
          businessNameComplete &&
          businessTradeNameComplete &&
          businessAddressComplete
    }
  }

}

object SelfEmploymentsCYAModel {
  def apply(id: String, soleTraderBusiness: Option[SoleTraderBusiness]): SelfEmploymentsCYAModel = {
    SelfEmploymentsCYAModel(
      id = id,
      confirmed = soleTraderBusiness.exists(_.confirmed),
      startDateBeforeLimit = soleTraderBusiness.flatMap(_.startDateBeforeLimit),
      businessStartDate = soleTraderBusiness.flatMap(_.startDate),
      businessName = soleTraderBusiness.flatMap(_.name),
      businessTradeName = soleTraderBusiness.flatMap(_.trade),
      businessAddress = soleTraderBusiness.flatMap(_.address)
    )
  }
}