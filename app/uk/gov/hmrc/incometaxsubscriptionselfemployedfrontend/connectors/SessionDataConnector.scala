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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors

import play.api.libs.json._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSessionDataHttpParser.GetSessionDataResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.SaveSessionDataHttpParser.SaveSessionDataResponse

import java.net.URL
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue

@Singleton
class SessionDataConnector @Inject()(appConfig: AppConfig,
                                     http: HttpClientV2)
                                    (implicit ec: ExecutionContext) {

  private def sessionDataUrl(id: String): URL = {
    url"${appConfig.protectedMicroServiceUrl}/income-tax-subscription/session-data/id/$id"
  }

  def getSessionData[T](id: String)(implicit hc: HeaderCarrier, reads: Reads[T]): Future[GetSessionDataResponse[T]] = {
    http.get(url"${sessionDataUrl(id)}").execute[GetSessionDataResponse[T]]
  }

  def saveSessionData[T](id: String, data: T)(implicit hc: HeaderCarrier, writes: Writes[T]): Future[SaveSessionDataResponse] = {
    http.post(url"${sessionDataUrl(id)}").withBody(Json.toJson(data)).execute[SaveSessionDataResponse]
  }
}
