/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.libs.json.{JsError, JsPath, JsSuccess, Json}

class BusinessNameModelSpec extends PlaySpec with GuiceOneServerPerSuite {

  "BusinessNameModel" should {

    val name = BusinessNameModel("Business")
    val json = Json.obj("businessName" -> "Business")

    "read from Json correctly" in {
      Json.fromJson[BusinessNameModel](json) mustBe JsSuccess(name)
    }

    "fail to read from Json" in {
      Json.fromJson[BusinessNameModel](Json.obj()) mustBe JsError(JsPath \ "businessName", "error.path.missing")
    }

    "write from Json correctly" in {
      Json.toJson(name) mustBe json
    }
  }

}
