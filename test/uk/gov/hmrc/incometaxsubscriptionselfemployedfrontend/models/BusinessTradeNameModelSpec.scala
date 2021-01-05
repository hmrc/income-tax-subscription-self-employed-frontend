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

import org.scalatest.Matchers._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.json.{JsError, JsPath, JsSuccess, Json}

class BusinessTradeNameModelSpec extends PlaySpec with GuiceOneServerPerSuite {

  "BusinessTradeNameModel" should {

    val name = BusinessTradeNameModel("BusinessTrade")
    val json = Json.obj("businessTradeName"-> "BusinessTrade")

    "read from Json correctly" in {
      Json.fromJson[BusinessTradeNameModel](json) mustBe JsSuccess(name)
    }

    "fail to read from Json" in {
      Json.fromJson[BusinessTradeNameModel](Json.obj()) mustBe JsError(JsPath \ "businessTradeName", "error.path.missing")
    }

    "write from Json correctly" in {
      Json.toJson(name) mustBe json
    }
  }

}
