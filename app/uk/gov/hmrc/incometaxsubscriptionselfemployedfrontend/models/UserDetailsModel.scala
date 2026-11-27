/*
 * Copyright 2026 HM Revenue & Customs
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
import play.api.libs.json.*
import uk.gov.hmrc.crypto.Sensitive.SensitiveString

case class UserDetailsModel(firstName: String, lastName: String) {

}

object UserDetailsModel {
  def apply(firstName: String, lastName: String): UserDetailsModel =
    new UserDetailsModel(
      firstName = firstName.capitaliseAll,
      lastName = lastName.capitaliseAll
    )

  implicit class StringCapitalise(text: String) {
    private val pattern = raw"\b\p{L}".r

    def capitaliseAll: String =
      pattern.replaceAllIn(text, _.matched.toUpperCase)
  }

  def encryptedFormat(implicit sensitiveFormat: Format[SensitiveString]): OFormat[UserDetailsModel] = {
    val reads: Reads[UserDetailsModel] = (
      (__ \ "firstName").read[SensitiveString] and
        (__ \ "lastName").read[SensitiveString]
      )(
      (firstName, lastName) =>
        UserDetailsModel.apply(firstName.decryptedValue, lastName.decryptedValue)
    )

    val writes: OWrites[UserDetailsModel] = (
      (__ \ "firstName").write[SensitiveString] and
        (__ \ "lastName").write[SensitiveString]
      )(
      userDetailsModel =>
        (
          SensitiveString(userDetailsModel.firstName),
          SensitiveString(userDetailsModel.lastName)
        )
    )

    OFormat(reads, writes)
  }

}
