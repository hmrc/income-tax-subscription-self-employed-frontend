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
import play.api.libs.json.JsSuccess
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.GetAllSelfEmploymentModel
object GetAllSelfEmploymentsHttpParser {

  type GetAllSelfEmploymentResponse = Either[GetAllSelfEmploymentFailure, GetAllSelfEmploymentSuccess]

  implicit object GetAllSelfEmploymentHttpReads extends HttpReads[GetAllSelfEmploymentResponse] {
    override def read(method: String, url: String, response: HttpResponse): GetAllSelfEmploymentResponse =
      response.status match {
        case OK =>  response.json.validate[GetAllSelfEmploymentModel] match {
          case JsSuccess(model,_) => Right(GetAllSelfEmploymentDataModel(model))
          case _ => Left(InvalidJson)
        }
        case NO_CONTENT => Right(GetAllSelfEmploymentConnectionSuccess)
        case status => Left(GetAllSelfEmploymentConnectionFailure(status))
      }
  }

  sealed trait GetAllSelfEmploymentSuccess
  case object GetAllSelfEmploymentConnectionSuccess extends GetAllSelfEmploymentSuccess
  case class GetAllSelfEmploymentDataModel(model:  GetAllSelfEmploymentModel) extends GetAllSelfEmploymentSuccess


  sealed trait GetAllSelfEmploymentFailure
  case object InvalidJson extends GetAllSelfEmploymentFailure
  case class GetAllSelfEmploymentConnectionFailure(status: Int) extends GetAllSelfEmploymentFailure
}
