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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models


import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent._

case class GetAllSelfEmploymentModel(businessStartDate: BusinessStartDate,
                                     businessName: BusinessNameModel,
                                     businessTradeName: BusinessTradeNameModel,
                                     businessAddress: BusinessAddressModel
                                    )


object GetAllSelfEmploymentModel {
  implicit val reads: Reads[GetAllSelfEmploymentModel] = (
    (__ \ DateOfCommencementController.businessStartDateKey).read[BusinessStartDate] and
      (__ \ BusinessNameController.businessName).read[BusinessNameModel] and
      (__ \ BusinessTradeNameController.businessTradeNameKey).read[BusinessTradeNameModel] and
      (__ \ AddressLookupRoutingController.businessAddress).read[BusinessAddressModel]
    ) (GetAllSelfEmploymentModel.apply _)

  implicit val writes: OWrites[GetAllSelfEmploymentModel] = (
    (__ \ DateOfCommencementController.businessStartDateKey).write[BusinessStartDate] and
      (__ \ BusinessNameController.businessName).write[BusinessNameModel] and
      (__ \ BusinessTradeNameController.businessTradeNameKey).write[BusinessTradeNameModel] and
      (__ \ AddressLookupRoutingController.businessAddress).write[BusinessAddressModel]
    ) (unlift(GetAllSelfEmploymentModel.unapply))
}