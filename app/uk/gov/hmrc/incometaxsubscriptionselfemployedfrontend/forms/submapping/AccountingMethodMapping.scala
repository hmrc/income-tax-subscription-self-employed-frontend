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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.submapping

import play.api.data.Forms.of
import play.api.data.format.Formatter
import play.api.data.validation.Invalid
import play.api.data.{FormError, Mapping}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{AccountingMethod, Accruals, Cash}


object AccountingMethodMapping {

  import Cash.CASH
  import Accruals.ACCRUALS

  def apply(errInvalid: Invalid, errEmpty: Option[Invalid]): Mapping[AccountingMethod] = of(new Formatter[AccountingMethod] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], AccountingMethod] = {
      data.get(key) match {
        case Some(`CASH`) => Right(Cash)
        case Some(`ACCRUALS`) => Right(Accruals)
        case Some(other) if other.nonEmpty => Left(errInvalid.errors.map(e => FormError(key, e.message, e.args)))
        case _ =>
          val err = errEmpty.getOrElse(errInvalid)
          Left(err.errors.map(e => FormError(key, e.message, e.args)))
      }
    }

    override def unbind(key: String, value: AccountingMethod): Map[String, String] = {
      val stringValue = value match {
        case Cash => CASH
        case Accruals => ACCRUALS
      }

      Map(key -> stringValue)
    }
  })

}
