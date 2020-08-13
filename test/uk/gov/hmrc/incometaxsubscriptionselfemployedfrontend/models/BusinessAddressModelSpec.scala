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

import org.scalatest.Matchers._
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.json.{JsError, JsPath, JsSuccess, Json}

class BusinessAddressModelSpec extends PlaySpec with GuiceOneServerPerSuite {

  "BusinessAddressModel" should {

    val name = BusinessAddressModel(id = "1", redirectUrl = "http://localhost/redirectUrl",
      Address(lines = Seq("line1", "line2", "line3"), postcode = "TF3 4NT"))
    val json = Json.obj("id"-> "1", "redirectUrl" -> "http://localhost/redirectUrl",
      "address" -> Json.obj("lines" -> Seq("line1", "line2", "line3"), "postcode" -> "TF3 4NT"))

    "read from Json correctly" in {
      Json.fromJson[BusinessAddressModel](json) mustBe JsSuccess(name)
    }

    "write from Json correctly" in {
      Json.toJson(name) mustBe json
    }
  }

}
