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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.DeleteSubscriptionDetailsHttpParser.DeleteSubscriptionDetailsResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.GetSelfEmploymentsResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSubscriptionDetailsResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.RetrieveReferenceHttpParser.RetrieveReferenceResponse

import java.net.URL
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue

@Singleton
class IncomeTaxSubscriptionConnector @Inject()(appConfig: AppConfig,
                                               http: HttpClientV2)
                                              (implicit ec: ExecutionContext) {

  def subscriptionURL(reference: String, id: String): URL = {
      url"${appConfig.protectedMicroServiceUrl}/income-tax-subscription/subscription-data/$reference/id/$id"
  }

  def retrieveReferenceUrl: URL = {
    url"${appConfig.protectedMicroServiceUrl}/income-tax-subscription/subscription-data"
  }

  def saveSubscriptionDetails[T](reference: String, id: String, data: T)
                                (implicit hc: HeaderCarrier, writes: Writes[T]): Future[PostSubscriptionDetailsResponse] = {
    http.post(url"${subscriptionURL(reference, id)}").withBody(Json.toJson(data)).execute[PostSubscriptionDetailsResponse]
  }

  def getSubscriptionDetails[T](reference: String, id: String)(implicit hc: HeaderCarrier, reads: Reads[T]): Future[GetSelfEmploymentsResponse[T]] = {
    http.get(url"${subscriptionURL(reference, id)}").execute[GetSelfEmploymentsResponse[T]]
  }

  def retrieveReference(utr: String)(implicit hc: HeaderCarrier): Future[RetrieveReferenceResponse] = {
    http.post(url"${retrieveReferenceUrl}").withBody(Json.toJson(Json.obj("utr" -> utr))).execute[RetrieveReferenceResponse]
  }

  def deleteSubscriptionDetails(reference: String, key: String)
                               (implicit hc: HeaderCarrier): Future[DeleteSubscriptionDetailsResponse] = {
    http.delete(url"${subscriptionURL(reference, key)}").execute[DeleteSubscriptionDetailsResponse]
  }

}
