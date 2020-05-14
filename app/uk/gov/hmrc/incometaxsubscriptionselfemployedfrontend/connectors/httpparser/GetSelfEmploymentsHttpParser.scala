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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser

import play.api.http.Status._
import play.api.libs.json.{JsSuccess, Reads}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetAllSelfEmploymentsHttpParser.InvalidJson


object GetSelfEmploymentsHttpParser {

  type GetSelfEmploymentsResponse[T] = Either[GetSelfEmploymentsFailure, GetSelfEmploymentsSuccess[T]]

  implicit def getSelfEmploymentsHttpReads[T](implicit reads: Reads[T]): HttpReads[GetSelfEmploymentsResponse[T]] =
    new HttpReads[GetSelfEmploymentsResponse[T]] {
      override def read(method: String, url: String, response: HttpResponse): GetSelfEmploymentsResponse[T] = {
        response.status match {
          case OK => response.json.validate[T] match {
            case JsSuccess(value, _) => Right(GetSelfEmploymentsData(value))
            case _ => Left(InvalidJson)
          }
          case NO_CONTENT => Right(GetSelfEmploymentsEmpty)
          case status => Left(UnexpectedStatusFailure(status))
        }
      }
    }
  sealed trait GetSelfEmploymentsSuccess[+T]
  case class GetSelfEmploymentsData[+T](model: T) extends GetSelfEmploymentsSuccess[T]
  case object GetSelfEmploymentsEmpty extends GetSelfEmploymentsSuccess[Nothing]

  sealed trait GetSelfEmploymentsFailure
  case object InvalidJson extends GetSelfEmploymentsFailure
  case class UnexpectedStatusFailure(status: Int) extends GetSelfEmploymentsFailure
}