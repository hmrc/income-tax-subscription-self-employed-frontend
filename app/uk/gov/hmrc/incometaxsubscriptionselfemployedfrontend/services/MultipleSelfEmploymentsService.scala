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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.businessesKey
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.IncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.GetSelfEmploymentsFailure
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSubscriptionDetailsSuccess
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.MultipleSelfEmploymentsService.SaveSelfEmploymentDataFailure

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MultipleSelfEmploymentsService @Inject()(incomeTaxSubscriptionConnector: IncomeTaxSubscriptionConnector)
                                              (implicit ec: ExecutionContext) {

  def fetchBusinessStartDate(reference: String, businessId: String)
                            (implicit hc: HeaderCarrier): Future[Either[GetSelfEmploymentsFailure, Option[BusinessStartDate]]] = {
    findData[BusinessStartDate](reference, businessId, _.businessStartDate)
  }

  def saveBusinessStartDate(reference: String, businessId: String, businessStartDate: BusinessStartDate)
                           (implicit hc: HeaderCarrier): Future[Either[SaveSelfEmploymentDataFailure.type, PostSubscriptionDetailsSuccess]] = {
    saveData(reference, businessId, _.copy(businessStartDate = Some(businessStartDate), confirmed = false))
  }

  def fetchBusinessName(reference: String, businessId: String)
                       (implicit hc: HeaderCarrier): Future[Either[GetSelfEmploymentsFailure, Option[BusinessNameModel]]] = {
    findData[BusinessNameModel](reference, businessId, _.businessName)
  }

  def saveBusinessName(reference: String, businessId: String, businessName: BusinessNameModel)
                      (implicit hc: HeaderCarrier): Future[Either[SaveSelfEmploymentDataFailure.type, PostSubscriptionDetailsSuccess]] = {
    saveData(reference, businessId, _.copy(businessName = Some(businessName), confirmed = false))
  }

  def fetchBusinessTrade(reference: String, businessId: String)
                        (implicit hc: HeaderCarrier): Future[Either[GetSelfEmploymentsFailure, Option[BusinessTradeNameModel]]] = {
    findData[BusinessTradeNameModel](reference, businessId, _.businessTradeName)
  }

  def saveBusinessTrade(reference: String, businessId: String, businessTrade: BusinessTradeNameModel)
                       (implicit hc: HeaderCarrier): Future[Either[SaveSelfEmploymentDataFailure.type, PostSubscriptionDetailsSuccess]] = {
    saveData(reference, businessId, _.copy(businessTradeName = Some(businessTrade), confirmed = false))
  }

  def fetchBusinessAddress(reference: String, businessId: String)
                          (implicit hc: HeaderCarrier): Future[Either[GetSelfEmploymentsFailure, Option[BusinessAddressModel]]] = {
    findData[BusinessAddressModel](reference, businessId, _.businessAddress)
  }

  def saveBusinessAddress(reference: String, businessId: String, businessAddress: BusinessAddressModel)
                         (implicit hc: HeaderCarrier): Future[Either[SaveSelfEmploymentDataFailure.type, PostSubscriptionDetailsSuccess]] = {
    saveData(reference, businessId, _.copy(businessAddress = Some(businessAddress), confirmed = false))
  }

  def fetchAddressRedirect(reference: String, businessId: String)
                          (implicit hc: HeaderCarrier): Future[Either[GetSelfEmploymentsFailure, Option[String]]] = {
    findData[String](reference, businessId, _.addressRedirect)
  }

  def saveAddressRedirect(reference: String, businessId: String, addressRedirect: String)
                         (implicit hc: HeaderCarrier): Future[Either[SaveSelfEmploymentDataFailure.type, PostSubscriptionDetailsSuccess]] = {
    saveData(reference, businessId, _.copy(addressRedirect = Some(addressRedirect), confirmed = false))
  }

  def confirmBusiness(reference: String, businessId: String)
                     (implicit hc: HeaderCarrier): Future[Either[SaveSelfEmploymentDataFailure.type, PostSubscriptionDetailsSuccess]] = {
    saveData(reference, businessId, _.copy(confirmed = true))
  }

  def fetchAllBusinesses(reference: String)(implicit hc: HeaderCarrier): Future[Either[GetSelfEmploymentsFailure, Seq[SelfEmploymentData]]] = {
    incomeTaxSubscriptionConnector.getSubscriptionDetails[Seq[SelfEmploymentData]](reference, businessesKey) map {
      case Right(Some(data)) => Right(data)
      case Right(None) => Right(Seq.empty[SelfEmploymentData])
      case Left(error) => Left(error)
    }
  }

  def fetchBusiness(reference: String, id: String)(implicit hc: HeaderCarrier): Future[Either[GetSelfEmploymentsFailure, Option[SelfEmploymentData]]] = {
    fetchAllBusinesses(reference).map {
      case Left(value) => Left(value)
      case Right(value) => Right(value.find(_.id == id))
    }
  }

  private[services] def findData[T](reference: String, businessId: String, modelToData: SelfEmploymentData => Option[T])(implicit hc: HeaderCarrier) = {
    incomeTaxSubscriptionConnector.getSubscriptionDetails[Seq[SelfEmploymentData]](reference, businessesKey) map {
      case Right(Some(businesses)) => Right(businesses.find(_.id == businessId).flatMap(modelToData))
      case Right(None) => Right(None)
      case Left(failure) => Left(failure)
    }
  }

  private[services] def saveData(reference: String, businessId: String, businessUpdate: SelfEmploymentData => SelfEmploymentData)
                                (implicit hc: HeaderCarrier) = {

    def updateBusinessList(businesses: Seq[SelfEmploymentData]): Seq[SelfEmploymentData] = {
      if (businesses.exists(_.id == businessId)) {
        businesses map {
          case business if business.id == businessId => businessUpdate(business)
          case business => business
        }
      } else {
        businesses :+ businessUpdate(SelfEmploymentData(businessId))
      }
    }

    incomeTaxSubscriptionConnector.getSubscriptionDetails[Seq[SelfEmploymentData]](reference, businessesKey) flatMap {
      case Right(data) =>
        val updatedBusinessList: Seq[SelfEmploymentData] = updateBusinessList(data.toSeq.flatten)
        incomeTaxSubscriptionConnector.saveSubscriptionDetails(reference, businessesKey, updatedBusinessList) map {
          case Right(result) => Right(result)
          case Left(_) => Left(SaveSelfEmploymentDataFailure)
        }
      case Left(_) => Future.successful(Left(SaveSelfEmploymentDataFailure))
    }

  }

}

object MultipleSelfEmploymentsService {

  case object SaveSelfEmploymentDataFailure

}
