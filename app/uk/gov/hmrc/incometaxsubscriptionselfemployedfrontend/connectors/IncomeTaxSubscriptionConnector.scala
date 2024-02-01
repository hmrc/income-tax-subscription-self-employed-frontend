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

import play.api.libs.json.{JsObject, JsValue, Json, Reads, Writes}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.GetSelfEmploymentsResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSubscriptionDetailsResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.RetrieveReferenceHttpParser.RetrieveReferenceResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class IncomeTaxSubscriptionConnector @Inject()(appConfig: AppConfig,
                                               http: HttpClient)
                                              (implicit ec: ExecutionContext) {

  def subscriptionURL(reference: String, id: String): String = {
    appConfig.protectedMicroServiceUrl + s"/income-tax-subscription/subscription-data/$reference/id/$id"
  }

  def retrieveReferenceUrl: String = {
    appConfig.protectedMicroServiceUrl + "/income-tax-subscription/subscription-data"
  }

  def saveSubscriptionDetails[T](reference: String, id: String, data: T)
                                (implicit hc: HeaderCarrier, writes: Writes[T]): Future[PostSubscriptionDetailsResponse] = {
    http.POST[JsValue, PostSubscriptionDetailsResponse](subscriptionURL(reference, id), Json.toJson(data))
  }

  def getSubscriptionDetails[T](reference: String, id: String)(implicit hc: HeaderCarrier, reads: Reads[T]): Future[GetSelfEmploymentsResponse[T]] = {
    http.GET[GetSelfEmploymentsResponse[T]](subscriptionURL(reference, id))
  }

  def retrieveReference(utr: String)(implicit hc: HeaderCarrier): Future[RetrieveReferenceResponse] = {
    http.POST[JsObject, RetrieveReferenceResponse](retrieveReferenceUrl, Json.obj("utr" -> utr))
  }

}
