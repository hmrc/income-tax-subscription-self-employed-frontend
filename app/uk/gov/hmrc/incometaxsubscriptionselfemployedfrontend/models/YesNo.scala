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

import play.api.i18n.Messages
import play.api.libs.json.Reads._
import play.api.libs.json._


sealed trait YesNo {
  def toMessageString(implicit messages: Messages): String
}

case object Yes extends YesNo {
  private[models] val YES = "Yes"

  override def toString: String = YES

  override def toMessageString(implicit messages: Messages): String = Messages("base.yes")
}

case object No extends YesNo {
  private[models] val NO = "No"

  override def toString: String = NO

  override def toMessageString(implicit messages: Messages): String = Messages("base.no")
}

object YesNo {

  import No.NO
  import Yes.YES

  private val reads: Reads[YesNo] = (json: JsValue) => json.validate[String] flatMap {
    case YES => JsSuccess(Yes)
    case NO => JsSuccess(No)
    case _ => JsError("[YesNo][reads] - Unable to parse value to YesNo type")
  }

  private val writes: Writes[YesNo] = {
    case Yes => JsString(YES)
    case No => JsString(NO)
  }

  implicit val format: Format[YesNo] = Format[YesNo](reads, writes)
}
