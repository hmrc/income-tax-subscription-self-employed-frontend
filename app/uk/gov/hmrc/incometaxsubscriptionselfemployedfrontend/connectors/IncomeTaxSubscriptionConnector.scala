/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsValue, Json, Reads, Writes}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.GetSelfEmploymentsResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSelfEmploymentsResponse
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncomeTaxSubscriptionConnector @Inject()(appConfig: AppConfig,
                                               http: HttpClient)
                                              (implicit ec: ExecutionContext) {

  def selfEmployedURL(id: String): String = {
    appConfig.selfEmployedUrl + s"/$id"
  }

  def saveSelfEmployments[T](id: String, data: T)(implicit hc: HeaderCarrier, writes: Writes[T]): Future[PostSelfEmploymentsResponse] = {
    http.POST[JsValue, PostSelfEmploymentsResponse](selfEmployedURL(id), Json.toJson(data))
  }

  def getSelfEmployments[T](id: String)(implicit hc: HeaderCarrier, reads: Reads[T]): Future[GetSelfEmploymentsResponse[T]] = {
    http.GET[GetSelfEmploymentsResponse[T]](selfEmployedURL(id))
  }
}
