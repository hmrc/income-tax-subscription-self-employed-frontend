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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser

import play.api.http.Status._
import play.api.libs.json.{JsSuccess, Reads}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}


object GetSelfEmploymentsHttpParser {

  type GetSelfEmploymentsResponse[T] = Either[GetSelfEmploymentsFailure, Option[T]]

  implicit def getSelfEmploymentsHttpReads[T](implicit reads: Reads[T]): HttpReads[GetSelfEmploymentsResponse[T]] =
    (_: String, _: String, response: HttpResponse) => {
      response.status match {
        case OK => response.json.validate[T] match {
          case JsSuccess(value, _) => Right(Some(value))
          case _ => Left(InvalidJson)
        }
        case NO_CONTENT => Right(None)
        case status => Left(UnexpectedStatusFailure(status))
      }
    }

  sealed trait GetSelfEmploymentsFailure

  case object InvalidJson extends GetSelfEmploymentsFailure

  case class UnexpectedStatusFailure(status: Int) extends GetSelfEmploymentsFailure

}