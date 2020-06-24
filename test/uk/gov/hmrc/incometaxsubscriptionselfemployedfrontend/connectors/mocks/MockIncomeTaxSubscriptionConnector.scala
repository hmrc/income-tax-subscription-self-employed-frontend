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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.IncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.{GetSelfEmploymentsFailure, GetSelfEmploymentsResponse}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.{PostSelfEmploymentsFailure, PostSelfEmploymentsResponse, PostSelfEmploymentsSuccess}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.BusinessNameModel
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.UnitTestTrait

import scala.concurrent.Future

trait MockIncomeTaxSubscriptionConnector extends UnitTestTrait with MockitoSugar with BeforeAndAfterEach {

  val mockIncomeTaxSubscriptionConnector: IncomeTaxSubscriptionConnector = mock[IncomeTaxSubscriptionConnector]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockIncomeTaxSubscriptionConnector)
  }

  def mockGetSelfEmployments[T](id: String)
                               (response: GetSelfEmploymentsResponse[T]): OngoingStubbing[Future[GetSelfEmploymentsResponse[T]]] = {
    when(mockIncomeTaxSubscriptionConnector.getSelfEmployments[T](
      ArgumentMatchers.eq(id)
    )(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(response))
  }

  def mockSaveSelfEmployments[T](id: String, value: T)
                                (response: PostSelfEmploymentsResponse): OngoingStubbing[Future[PostSelfEmploymentsResponse]] = {
    when(mockIncomeTaxSubscriptionConnector.saveSelfEmployments[T](
      ArgumentMatchers.eq(id), ArgumentMatchers.eq(value)
    )(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(Future.successful(response))
  }
}