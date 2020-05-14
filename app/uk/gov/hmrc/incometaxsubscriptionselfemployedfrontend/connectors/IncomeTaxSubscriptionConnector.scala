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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsObject, Json, OWrites, Reads}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetAllSelfEmploymentsHttpParser.GetAllSelfEmploymentResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.GetSelfEmploymentsResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSelfEmploymentsResponse

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncomeTaxSubscriptionConnector @Inject()(appConfig: AppConfig,
                                               http: HttpClient)
                                              (implicit ec: ExecutionContext) {

  def selfEmployedURL(id: String): String = {
    appConfig.selfEmployedUrl + IncomeTaxSubscriptionConnector.selfEmploymentsUri(id)
  }

  def getAllSelfEmployments()(implicit hc:HeaderCarrier): Future[GetAllSelfEmploymentResponse] = {
    http.GET[GetAllSelfEmploymentResponse](appConfig.allSelfEmployedUrl)
  }

  def saveSelfEmployments[T](id: String, data: T)(implicit hc:HeaderCarrier, writes: OWrites[T]): Future[PostSelfEmploymentsResponse] = {
    http.POST[JsObject,PostSelfEmploymentsResponse](selfEmployedURL(id),Json.toJsObject(data))
  }

  def getSelfEmployments[T](id: String)(implicit hc: HeaderCarrier, reads: Reads[T]): Future[GetSelfEmploymentsResponse[T]] = {
    http.GET[GetSelfEmploymentsResponse[T]](selfEmployedURL(id))
  }

}

object IncomeTaxSubscriptionConnector{
  def selfEmploymentsUri(id: String): String = s"/$id"
}




