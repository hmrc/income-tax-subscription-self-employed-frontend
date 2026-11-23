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

import crypto.ComposeCryptoService
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.json.*
import uk.gov.hmrc.crypto.SymmetricCryptoFactory.aesGcmCryptoFromConfig
import uk.gov.hmrc.crypto.{ApplicationCrypto, Decrypter, Encrypter}

class AddressModelSpec extends PlaySpec with GuiceOneServerPerSuite {

  implicit val crypto: Encrypter with Decrypter = app.injector.instanceOf[ComposeCryptoService].composedJsonCrypto

  val fullAddress: Address = Address(lines = Seq("1 Long Road", "Lonely Town"), postcode = Some("ZZ1 1ZZ"), country = Country.UK)
  val minAddress: Address = Address(Seq.empty[String], None, Country.UK)
  val fullAddressWithUrpn: Address = fullAddress.copy(uprn = Some("1234"))

  "Address reading JSON encrypted with legacy AES using composed crypto" should {
    val legacyCrypto = app.injector.instanceOf[ApplicationCrypto].JsonCrypto

    "read successfully" when {
      "all information except uprn is present in json" in {
        val fullJson = Json.toJsObject(fullAddress)(Address.encryptedFormat(legacyCrypto))
        Json.fromJson[Address](fullJson)(Address.encryptedFormat) mustBe JsSuccess(fullAddress)
      }
      "all information including uprn is present in json" in {
        val fullJsonWithUrpn = Json.toJsObject(fullAddressWithUrpn)(Address.encryptedFormat(legacyCrypto))
        Json.fromJson[Address](fullJsonWithUrpn)(Address.encryptedFormat) mustBe JsSuccess(fullAddressWithUrpn)
      }
      "lines is empty and postcode is missing" in {
        val minJson = Json.toJsObject(minAddress)(Address.encryptedFormat(legacyCrypto))
        Json.fromJson[Address](minJson)(Address.encryptedFormat) mustBe JsSuccess(minAddress)
      }
    }
    "fail to read" when {
      "lines is missing" in {
        val fullJson = Json.toJsObject(fullAddress)(Address.encryptedFormat(legacyCrypto))
        Json.fromJson[Address](fullJson - "lines")(Address.encryptedFormat) mustBe JsError(__ \ "lines", "error.path.missing")
      }
    }
  }

  "Address reading JSON encrypted with AES-GCM using composed crypto" should {
    val aesGcmCrypto = aesGcmCryptoFromConfig("json.encryption.gcm", app.configuration.underlying)

    "read successfully" when {
      "all information except uprn is present in json" in {
        val fullJson = Json.toJsObject(fullAddress)(Address.encryptedFormat(aesGcmCrypto))
        Json.fromJson[Address](fullJson)(Address.encryptedFormat) mustBe JsSuccess(fullAddress)
      }
      "all information including uprn is present in json" in {
        val fullJsonWithUrpn = Json.toJsObject(fullAddressWithUrpn)(Address.encryptedFormat(aesGcmCrypto))
        Json.fromJson[Address](fullJsonWithUrpn)(Address.encryptedFormat) mustBe JsSuccess(fullAddressWithUrpn)
      }
      "lines is empty and postcode is missing" in {
        val minJson = Json.toJsObject(minAddress)(Address.encryptedFormat(aesGcmCrypto))
        Json.fromJson[Address](minJson)(Address.encryptedFormat) mustBe JsSuccess(minAddress)
      }
    }
    "fail to read" when {
      "lines is missing" in {
        val fullJson = Json.toJsObject(fullAddress)(Address.encryptedFormat(aesGcmCrypto))
        Json.fromJson[Address](fullJson - "lines")(Address.encryptedFormat) mustBe JsError(__ \ "lines", "error.path.missing")
      }
    }
  }

}
