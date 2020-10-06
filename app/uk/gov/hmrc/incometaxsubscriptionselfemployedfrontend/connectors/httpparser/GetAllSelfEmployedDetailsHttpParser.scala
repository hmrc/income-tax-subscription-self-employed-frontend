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

object GetAllSelfEmployedDetailsHttpParser {

  type GetAllSelfEmployedDetailsResponse[T] = Either[GetAllSelfEmployedDetailsFailure, Option[T]]

  implicit def getAllSelfEmployedDetailsHttpReads[T](implicit reads: Reads[T]): HttpReads[GetAllSelfEmployedDetailsResponse[T]] =
    new HttpReads[GetAllSelfEmployedDetailsResponse[T]] {
      override def read(method: String, url: String, response: HttpResponse): GetAllSelfEmployedDetailsResponse[T] = {
        response.status match {
          case OK => response.json.validate[T] match {
            case JsSuccess(value, _) => Right(Some(value))
            case _ => Left(InvalidJson)
          }
          case NO_CONTENT => Right(None)
          case status => Left(UnexpectedStatusFailure(status))
        }
      }
    }
  
  sealed trait GetAllSelfEmployedDetailsFailure
  case object InvalidJson extends GetAllSelfEmployedDetailsFailure
  case class UnexpectedStatusFailure(status: Int) extends GetAllSelfEmployedDetailsFailure
}
