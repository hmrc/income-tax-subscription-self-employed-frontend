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

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.json.Json

class AccountingMethodSpec extends PlaySpec with GuiceOneServerPerSuite {

  "Accounting method" should {

    "read correctly from json" when {

      "given a valid string of 'Cash'" in {
        val json = Json.toJson("Cash")

        json.as[AccountingMethod] mustBe Cash
      }

      "given a valid string of 'Accruals'" in {
        val json = Json.toJson("Accruals")

        json.as[AccountingMethod] mustBe Accruals
      }
    }

    "throw a json error" when {

      "given an invalid string" in {
        val json = Json.toJson("Invalid string")

        the[Exception] thrownBy json.as[AccountingMethod] must have message "Invalid accounting method type from json."
      }
    }
  }
}
