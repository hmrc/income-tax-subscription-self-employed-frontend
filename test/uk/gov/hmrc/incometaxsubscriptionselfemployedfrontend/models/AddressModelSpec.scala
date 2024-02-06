/*
 * Copyright 2023 HM Revenue & Customs
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
import play.api.libs.json._
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.crypto.json.JsonEncryption
import uk.gov.hmrc.crypto.{ApplicationCrypto, Decrypter, Encrypter, PlainText}

class AddressModelSpec extends PlaySpec with GuiceOneServerPerSuite {

  implicit val crypto: Encrypter with Decrypter = app.injector.instanceOf[ApplicationCrypto].JsonCrypto

  def jsonSensitiveEncrypter(text: String): JsValue = JsonEncryption
    .sensitiveEncrypterDecrypter(SensitiveString.apply)
    .writes(SensitiveString(text))

  "encrypting the same thing twice should result in the same thing" in {
    crypto.encrypt(PlainText("text")).value mustBe crypto.encrypt(PlainText("text")).value
  }

  val fullAddress: Address = Address(lines = Seq("1 Long Road", "Lonely Town"), postcode = Some("ZZ1 1ZZ"))
  val fullJson: JsObject = Json.obj(
    "lines" -> Json.arr(
      jsonSensitiveEncrypter("1 Long Road"),
      jsonSensitiveEncrypter("Lonely Town")
    ),
    "postcode" -> jsonSensitiveEncrypter("ZZ1 1ZZ")
  )

  val minAddress: Address = Address(Seq.empty[String], None)
  val minJson: JsObject = Json.obj(
    "lines" -> Json.arr()
  )

  "Address" when {
    "reading from encrypted json" should {
      "read successfully" when {
        "all information is present in json" in {
          Json.fromJson[Address](fullJson)(Address.encryptedFormat) mustBe JsSuccess(fullAddress)
        }
        "lines is empty and postcode is missing" in {
          Json.fromJson[Address](minJson)(Address.encryptedFormat) mustBe JsSuccess(minAddress)
        }
      }
      "fail to read" when {
        "lines is missing" in {
          Json.fromJson[Address](fullJson - "lines")(Address.encryptedFormat) mustBe JsError(__ \ "lines", "error.path.missing")
        }
      }
    }
    "writing to encrypted json" should {
      "write successfully" when {
        "all information is present in the model" in {
          Json.toJson(fullAddress)(Address.encryptedFormat) mustBe fullJson
        }
        "lines is empty and postcode is missing" in {
          Json.toJson(minAddress)(Address.encryptedFormat) mustBe minJson
        }
      }
    }
  }

}
