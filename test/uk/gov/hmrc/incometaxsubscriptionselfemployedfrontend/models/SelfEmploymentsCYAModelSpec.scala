/*
 * Copyright 2025 HM Revenue & Customs
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

import org.scalatestplus.play.PlaySpec

class SelfEmploymentsCYAModelSpec extends PlaySpec {

  val dateModel: DateModel = DateModel("1", "2", "1980")

  val address: Address = Address(
    lines = Seq("1 long road"),
    postcode = Some("ZZ1 1ZZ")
  )

  val fullModel: SelfEmploymentsCYAModel = SelfEmploymentsCYAModel(
    id = "test-id",
    confirmed = true,
    startDateBeforeLimit = Some(false),
    businessStartDate = Some(dateModel),
    businessName = Some("test name"),
    businessTradeName = Some("test trade"),
    businessAddress = Some(address)
  )

  "SelfEmploymentsCYAModel.isComplete" must {
    "return true" when {
      "start date before limit is defined and true" in {
        fullModel.copy(startDateBeforeLimit = Some(true), businessStartDate = None).isComplete mustBe true
      }
      "start date before limit is defined and false and start date defined" in {
        fullModel.copy(startDateBeforeLimit = Some(false)).isComplete mustBe true
      }
      "start date before limit is not defined and start date defined" in {
        fullModel.copy(startDateBeforeLimit = None).isComplete mustBe true
      }
    }

    "return false" when {
      "start date before limit is defined and true and name is not defined" in {
        fullModel.copy(startDateBeforeLimit = Some(true), businessStartDate = None, businessName = None).isComplete mustBe false
      }
      "start date before limit is defined and true and trade is not defined" in {
        fullModel.copy(startDateBeforeLimit = Some(true), businessStartDate = None, businessTradeName = None).isComplete mustBe false
      }
      "start date before limit is defined and true and address is not defined" in {
        fullModel.copy(startDateBeforeLimit = Some(true), businessStartDate = None, businessAddress = None).isComplete mustBe false
      }
      "start date before limit is defined and false and start date is not defined" in {
        fullModel.copy(startDateBeforeLimit = Some(false), businessStartDate = None).isComplete mustBe false
      }
      "start date before limit is not defined and start date is not defined" in {
        fullModel.copy(startDateBeforeLimit = None, businessStartDate = None).isComplete mustBe false
      }
    }
  }
}
