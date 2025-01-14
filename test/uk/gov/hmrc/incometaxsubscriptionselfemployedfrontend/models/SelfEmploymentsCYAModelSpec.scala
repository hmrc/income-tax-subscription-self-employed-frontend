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
    businessAddress = Some(address),
    accountingMethod = Some(Cash),
    totalSelfEmployments = 1,
    isFirstBusiness = true
  )

  "SelfEmploymentsCYAModel.isComplete" when {
    "start date before limit is defined and true" should {
      "return true" when {
        "name, trade, address, accounting method are defined" in {
          fullModel.copy(startDateBeforeLimit = Some(true), businessStartDate = None).isComplete mustBe true
        }
      }
      "return false" when {
        "name is not defined" in {
          fullModel.copy(startDateBeforeLimit = Some(true), businessStartDate = None, businessName = None).isComplete mustBe false
        }
        "trade is not defined" in {
          fullModel.copy(startDateBeforeLimit = Some(true), businessStartDate = None, businessTradeName = None).isComplete mustBe false
        }
        "address is not defined" in {
          fullModel.copy(startDateBeforeLimit = Some(true), businessStartDate = None, businessAddress = None).isComplete mustBe false
        }
        "accounting method is not defined" in {
          fullModel.copy(startDateBeforeLimit = Some(true), businessStartDate = None, accountingMethod = None).isComplete mustBe false
        }
      }
    }

    "start date before limit is defined and false" should {
      "return true" when {
        "start date, name, trade, address, accounting method are defined" in {
          fullModel.isComplete mustBe true
        }
      }
      "return false" when {
        "start date is not defined" in {
          fullModel.copy(businessStartDate = None).isComplete mustBe false
        }
        "name is not defined" in {
          fullModel.copy(businessName = None).isComplete mustBe false
        }
        "trade is not defined" in {
          fullModel.copy(businessTradeName = None).isComplete mustBe false
        }
        "address is not defined" in {
          fullModel.copy(businessAddress = None).isComplete mustBe false
        }
        "accounting method is not defined" in {
          fullModel.copy(accountingMethod = None).isComplete mustBe false
        }
      }
    }

    "start date before limit is not defined" should {
      "return true" when {
        "start date, name, trade, address, accounting method are defined" in {
          fullModel.copy(startDateBeforeLimit = None).isComplete mustBe true
        }
      }
      "return false" when {
        "start date is not defined" in {
          fullModel.copy(startDateBeforeLimit = None, businessStartDate = None).isComplete mustBe false
        }
        "name is not defined" in {
          fullModel.copy(startDateBeforeLimit = None, businessName = None).isComplete mustBe false
        }
        "trade is not defined" in {
          fullModel.copy(startDateBeforeLimit = None, businessTradeName = None).isComplete mustBe false
        }
        "address is not defined" in {
          fullModel.copy(startDateBeforeLimit = None, businessAddress = None).isComplete mustBe false
        }
        "accounting method is not defined" in {
          fullModel.copy(startDateBeforeLimit = None, accountingMethod = None).isComplete mustBe false
        }
      }
    }
  }

}
