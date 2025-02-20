/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._
import uk.gov.hmrc.crypto.Sensitive.SensitiveString
import uk.gov.hmrc.crypto.json.JsonEncryption
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}

case class Address(lines: Seq[String], postcode: Option[String]) {
  override def toString: String = s"${lines.mkString("<br>")}${postcode.map(t => s"<br>$t").getOrElse("")}"
}

object Address {

  def encryptedFormat(implicit crypto: Encrypter with Decrypter): OFormat[Address] = {
    implicit val sensitiveFormat: Format[SensitiveString] = JsonEncryption.sensitiveEncrypterDecrypter(SensitiveString.apply)

    val reads: Reads[Address] = (
      (__ \ "lines").read[Seq[SensitiveString]] and
        (__ \ "postcode").readNullable[SensitiveString]
      )(
      (lines, postcode) =>
        Address.apply(lines.map(_.decryptedValue), postcode.map(_.decryptedValue))
    )

    val writes: OWrites[Address] = (
      (__ \ "lines").write[Seq[SensitiveString]] and
        (__ \ "postcode").writeNullable[SensitiveString]
      )(
      address =>
        (
          address.lines.map(SensitiveString.apply),
          address.postcode.map(SensitiveString.apply)
        )
    )

    OFormat(reads, writes)

  }

  val format: OFormat[Address] = Json.format[Address]

}