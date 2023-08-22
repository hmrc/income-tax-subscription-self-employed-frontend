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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services

import org.graalvm.compiler.replacements.amd64.PluginFactory_AMD64StringIndexOfNode
import uk.gov.hmrc.crypto.{ApplicationCrypto, Crypted, PlainText}
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
class MultipleSelfEmploymentsService @Inject()(incomeTaxSubscriptionConnector: IncomeTaxSubscriptionConnector,
                                               applicationCrypto: ApplicationCrypto)
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

  def confirmBusiness(reference: String, businessId: String)
                     (implicit hc: HeaderCarrier): Future[Either[SaveSelfEmploymentDataFailure.type, PostSubscriptionDetailsSuccess]] = {
    saveData(reference, businessId, _.copy(confirmed = true))
  }

  def fetchAllBusinesses(reference: String)(implicit hc: HeaderCarrier): Future[Either[GetSelfEmploymentsFailure, Seq[SelfEmploymentData]]] = {

    def decryptBusinessList(businesses: Seq[SelfEmploymentData]) : Seq[SelfEmploymentData] = {
      businesses map {
        business => business.copy(
          businessName = business.businessName.map(name =>
            name.decrypt(applicationCrypto.QueryParameterCrypto)
          ),
          businessAddress = business.businessAddress.map(address =>
            address.decrypt(applicationCrypto.QueryParameterCrypto)
          )
        )
      }
    }

    incomeTaxSubscriptionConnector.getSubscriptionDetails[Seq[SelfEmploymentData]](reference, businessesKey) map {
      case Right(Some(data)) => Right(decryptBusinessList(data))
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

  private[services] def findData[T](reference: String, businessId: String, modelToData: SelfEmploymentData => Option[T])(implicit hc: HeaderCarrier) =
    fetchAllBusinesses(reference) map {
      case Right(businesses) => Right(businesses.find(_.id == businessId).flatMap(modelToData))
      case Left(failure) => Left(failure)
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

    def encryptBusinessList(businesses: Seq[SelfEmploymentData]) : Seq[SelfEmploymentData] = {
      businesses map {
        business => business.copy(
          businessName = business.businessName.map(name =>
            name.encrypt(applicationCrypto.QueryParameterCrypto)
          ),
          businessAddress = business.businessAddress.map(address =>
            address.encrypt(applicationCrypto.QueryParameterCrypto)
          )
        )
      }
    }

    fetchAllBusinesses(reference) flatMap {
      case Right(data) =>
        val updatedBusinessList: Seq[SelfEmploymentData] = updateBusinessList(data)
        val encryptedBusinessList = encryptBusinessList(updatedBusinessList)
        incomeTaxSubscriptionConnector.saveSubscriptionDetails(reference, businessesKey, encryptedBusinessList) map {
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
