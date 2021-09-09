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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities

import java.time.LocalDate

import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._

object TestModels {

  val testValidStartDate: DateModel = DateModel.dateConvert(LocalDate.now.minusYears(3))
  val testBusinessStartDateModel: BusinessStartDate = BusinessStartDate(testValidStartDate)
  val testBusinessNameModel: BusinessNameModel = BusinessNameModel("Business")
  val testValidBusinessTradeName: String = "Plumbing"
  val testInvalidBusinessTradeName: String = "!()+{}?^~"
  val testValidBusinessTradeNameModel: BusinessTradeNameModel = BusinessTradeNameModel(testValidBusinessTradeName)
  val testInvalidBusinessTradeNameModel: BusinessTradeNameModel = BusinessTradeNameModel(testInvalidBusinessTradeName)
  val testAccountingMethodModel: AccountingMethodModel = AccountingMethodModel(Cash)

  val mockBusinessNameModel: BusinessNameModel = BusinessNameModel("ITSA me, Mario")
  val mockAddAnotherBusinessModelWithYes: AddAnotherBusinessModel = AddAnotherBusinessModel(Yes)
  val mockAddAnotherBusinessModelWithNo: AddAnotherBusinessModel = AddAnotherBusinessModel(No)

  val testValidBusinessAddressModel: BusinessAddressModel = BusinessAddressModel(auditRef = "1",
    Address(lines = Seq("line1", "line2", "line3"), postcode = "TF3 4NT"))

}
