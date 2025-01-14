/*
 * Copyright 2023 HM Revenue & Customs
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

  def mockFetchBusiness(id: String)(response: Either[GetSelfEmploymentsFailure, Option[SoleTraderBusiness]]): Unit = {
    when(mockMultipleSelfEmploymentsService.fetchBusiness(
      ArgumentMatchers.any(),
      ArgumentMatchers.eq(id)
    )(ArgumentMatchers.any())) thenReturn Future.successful(response)
  }

  def mockFetchSoleTraderBusinesses(response: Either[GetSelfEmploymentsFailure, Option[SoleTraderBusinesses]]): Unit = {
    when(mockMultipleSelfEmploymentsService.fetchSoleTraderBusinesses(any())(any()))
      .thenReturn(Future.successful(response))
  }

  def mockFetchBusinessStartDate(businessId: String)(response: Either[GetSelfEmploymentsFailure, Option[DateModel]]): Unit = {
    when(mockMultipleSelfEmploymentsService.fetchStartDate(any(), ArgumentMatchers.eq(businessId))(any()))
      .thenReturn(Future.successful(response))
  }

  def mockSaveBusinessStartDate(businessId: String, businessStartDate: DateModel)
                               (response: Either[SaveSelfEmploymentDataFailure.type, PostSubscriptionDetailsSuccess]): Unit = {
    when(
      mockMultipleSelfEmploymentsService.saveStartDate(
        any(),
        ArgumentMatchers.eq(businessId),
        ArgumentMatchers.eq(businessStartDate)
      )(any())
    ).thenReturn(Future.successful(response))
  }

  def mockSaveBusinessName(businessId: String, businessName: String)
                          (response: Either[SaveSelfEmploymentDataFailure.type, PostSubscriptionDetailsSuccess]): Unit = {
    when(
      mockMultipleSelfEmploymentsService.saveName(
        any(),
        ArgumentMatchers.eq(businessId),
        ArgumentMatchers.eq(businessName)
      )(any())
    ).thenReturn(Future.successful(response))
  }

  def mockSaveBusinessTrade(businessId: String, businessTrade: String)
                           (response: Either[SaveSelfEmploymentDataFailure.type, PostSubscriptionDetailsSuccess]): Unit = {
    when(
      mockMultipleSelfEmploymentsService.saveTrade(
        any(),
        ArgumentMatchers.eq(businessId),
        ArgumentMatchers.eq(businessTrade)
      )(any())
    ).thenReturn(Future.successful(response))
  }

  def mockSaveBusinessAddress(businessId: String, businessAddress: Address)
                             (response: Either[SaveSelfEmploymentDataFailure.type, PostSubscriptionDetailsSuccess]): Unit = {
    when(
      mockMultipleSelfEmploymentsService.saveAddress(
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

  def mockFetchFirstAddress(response: Either[GetSelfEmploymentsFailure, Option[Address]]): Unit = {
    when(mockMultipleSelfEmploymentsService.fetchFirstAddress(any())(any()))
      .thenReturn(Future.successful(response))
  }

  def mockFetchFirstBusinessName(response: Either[GetSelfEmploymentsFailure, Option[String]]): Unit = {
    when(mockMultipleSelfEmploymentsService.fetchFirstBusinessName(any())(any()))
      .thenReturn(Future.successful(response))
  }

  def mockFetchAccountingMethod(response: Either[GetSelfEmploymentsFailure, Option[AccountingMethod]]): Unit = {
    when(mockMultipleSelfEmploymentsService.fetchAccountingMethod(any())(any()))
      .thenReturn(Future.successful(response))
  }

  def mockSaveAccountingMethod(accountingMethod: AccountingMethod)
                              (response: Either[SaveSelfEmploymentDataFailure.type, PostSubscriptionDetailsSuccess]): Unit = {
    when(mockMultipleSelfEmploymentsService.saveAccountingMethod(any(), any(), ArgumentMatchers.eq(accountingMethod))(any()))
      .thenReturn(Future.successful(response))
  }

  def mockFetchAllNameTradeCombos(response: Either[GetSelfEmploymentsFailure, Seq[(String, Option[String], Option[String])]]): Unit = {
    when(mockMultipleSelfEmploymentsService.fetchAllNameTradeCombos(any())(any()))
      .thenReturn(Future.successful(response))
  }

}
