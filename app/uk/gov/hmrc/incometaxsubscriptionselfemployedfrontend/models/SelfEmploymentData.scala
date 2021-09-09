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

case class SelfEmploymentData(id: String,
                              businessStartDate: Option[BusinessStartDate] = None,
                              businessName: Option[BusinessNameModel] = None,
                              businessTradeName: Option[BusinessTradeNameModel] = None,
                              businessAddress: Option[BusinessAddressModel] = None,
                              addressRedirect: Option[String] = None) {

  val isComplete: Boolean = businessStartDate.isDefined &&
    businessName.isDefined &&
    businessTradeName.isDefined &&
    businessAddress.isDefined

}

object SelfEmploymentData {

  implicit val format: Format[SelfEmploymentData] = Json.format[SelfEmploymentData]

}

