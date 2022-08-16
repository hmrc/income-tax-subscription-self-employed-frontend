/*
 * Copyright 2022 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.mocks

import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.GetSelfEmploymentsFailure
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSubscriptionDetailsSuccess
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.MultipleSelfEmploymentsService
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.MultipleSelfEmploymentsService.SaveSelfEmploymentDataFailure

import scala.concurrent.Future

trait MockMultipleSelfEmploymentsService extends PlaySpec with MockitoSugar with BeforeAndAfterEach {

  val mockMultipleSelfEmploymentsService: MultipleSelfEmploymentsService = mock[MultipleSelfEmploymentsService]

  override def beforeEach(): Unit = {
    reset(mockMultipleSelfEmploymentsService)
    super.beforeEach()
  }

  def mockFetchBusinessStartDate(businessId: String)(response: Either[GetSelfEmploymentsFailure, Option[BusinessStartDate]]): Unit = {
    when(mockMultipleSelfEmploymentsService.fetchBusinessStartDate(any(), ArgumentMatchers.eq(businessId))(any()))
      .thenReturn(Future.successful(response))
  }

  def mockFetchBusinessName(businessId: String)(response: Either[GetSelfEmploymentsFailure, Option[BusinessNameModel]]): Unit = {
    when(mockMultipleSelfEmploymentsService.fetchBusinessName(any(), ArgumentMatchers.eq(businessId))(any()))
      .thenReturn(Future.successful(response))
  }

  def mockFetchBusinessTrade(businessId: String)(response: Either[GetSelfEmploymentsFailure, Option[BusinessTradeNameModel]]): Unit = {
    when(mockMultipleSelfEmploymentsService.fetchBusinessTrade(any(), ArgumentMatchers.eq(businessId))(any()))
      .thenReturn(Future.successful(response))
  }

  def mockFetchAllBusinesses(response: Either[GetSelfEmploymentsFailure, Seq[SelfEmploymentData]]): Unit = {
    when(mockMultipleSelfEmploymentsService.fetchAllBusinesses(any())(any()))
      .thenReturn(Future.successful(response))
  }

  def mockFetchBusiness(businessId: String)(response: Either[GetSelfEmploymentsFailure, Option[SelfEmploymentData]]): Unit = {
    when(mockMultipleSelfEmploymentsService.fetchBusiness(any(), ArgumentMatchers.eq(businessId))(any()))
      .thenReturn(Future.successful(response))
  }

  def mockSaveBusinessStartDate(businessId: String, businessStartDate: BusinessStartDate)
                               (response: Either[SaveSelfEmploymentDataFailure.type, PostSubscriptionDetailsSuccess]): Unit = {
    when(
      mockMultipleSelfEmploymentsService.saveBusinessStartDate(
        any(),
        ArgumentMatchers.eq(businessId),
        ArgumentMatchers.eq(businessStartDate)
      )(any())
    ).thenReturn(Future.successful(response))
  }

  def mockSaveBusinessName(businessId: String, businessName: BusinessNameModel)
                          (response: Either[SaveSelfEmploymentDataFailure.type, PostSubscriptionDetailsSuccess]): Unit = {
    when(
      mockMultipleSelfEmploymentsService.saveBusinessName(
        any(),
        ArgumentMatchers.eq(businessId),
        ArgumentMatchers.eq(businessName)
      )(any())
    ).thenReturn(Future.successful(response))
  }

  def mockSaveBusinessTrade(businessId: String, businessTrade: BusinessTradeNameModel)
                           (response: Either[SaveSelfEmploymentDataFailure.type, PostSubscriptionDetailsSuccess]): Unit = {
    when(
      mockMultipleSelfEmploymentsService.saveBusinessTrade(
        any(),
        ArgumentMatchers.eq(businessId),
        ArgumentMatchers.eq(businessTrade)
      )(any())
    ).thenReturn(Future.successful(response))
  }

  def mockSaveBusinessAddress(businessId: String, businessAddress: BusinessAddressModel)
                             (response: Either[SaveSelfEmploymentDataFailure.type, PostSubscriptionDetailsSuccess]): Unit = {
    when(
      mockMultipleSelfEmploymentsService.saveBusinessAddress(
        any(),
        ArgumentMatchers.eq(businessId),
        ArgumentMatchers.eq(businessAddress)
      )(any())
    ).thenReturn(Future.successful(response))
  }

  def mockConfirmBusiness(businessId: String)
                         (response: Either[SaveSelfEmploymentDataFailure.type, PostSubscriptionDetailsSuccess]): Unit = {
    when(
      mockMultipleSelfEmploymentsService.confirmBusiness(
        any(),
        ArgumentMatchers.eq(businessId)
      )(any())
    ).thenReturn(Future.successful(response))
  }

}
