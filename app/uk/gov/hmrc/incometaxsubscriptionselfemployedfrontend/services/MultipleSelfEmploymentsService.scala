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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services

import uk.gov.hmrc.crypto.{ApplicationCrypto, Decrypter, Encrypter}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.IncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.GetSelfEmploymentsFailure
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSubscriptionDetailsSuccess
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.agent.StreamlineBusiness
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.MultipleSelfEmploymentsService.SaveSelfEmploymentDataFailure

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MultipleSelfEmploymentsService @Inject()(applicationCrypto: ApplicationCrypto,
                                               incomeTaxSubscriptionConnector: IncomeTaxSubscriptionConnector)
                                              (implicit ec: ExecutionContext) {

  implicit val jsonCrypto: Encrypter with Decrypter = applicationCrypto.JsonCrypto

  def fetchSoleTraderBusinesses(reference: String)
                               (implicit hc: HeaderCarrier): Future[Either[GetSelfEmploymentsFailure, Option[SoleTraderBusinesses]]] = {
    incomeTaxSubscriptionConnector.getSubscriptionDetails[SoleTraderBusinesses](
      reference = reference,
      id = SelfEmploymentDataKeys.soleTraderBusinessesKey
    )(implicitly, SoleTraderBusinesses.encryptedFormat)
  }

  def saveSoleTraderBusinesses(reference: String, soleTraderBusinesses: SoleTraderBusinesses)
                              (implicit hc: HeaderCarrier): Future[Either[SaveSelfEmploymentDataFailure.type, PostSubscriptionDetailsSuccess]] = {
    incomeTaxSubscriptionConnector.saveSubscriptionDetails[SoleTraderBusinesses](
      reference = reference,
      id = SelfEmploymentDataKeys.soleTraderBusinessesKey,
      data = soleTraderBusinesses
    )(implicitly, SoleTraderBusinesses.encryptedFormat) flatMap {
      case Right(value) =>
        incomeTaxSubscriptionConnector.deleteSubscriptionDetails(
          reference = reference,
          key = SelfEmploymentDataKeys.incomeSourcesComplete
        ) map {
          case Right(_) => Right(value)
          case Left(_) => Left(SaveSelfEmploymentDataFailure)
        }
      case Left(_) =>
        Future.successful(Left(SaveSelfEmploymentDataFailure))
    }
  }

  private def findData[T](reference: String, id: String, modelToData: SoleTraderBusiness => Option[T])
                         (implicit hc: HeaderCarrier): Future[Either[GetSelfEmploymentsFailure, Option[T]]] = {
    fetchSoleTraderBusinesses(reference) map { result =>
      result map {
        case Some(soleTraderBusinesses) => soleTraderBusinesses.businesses
        case None => Seq.empty[SoleTraderBusiness]
      } map { businesses =>
        businesses.find(_.id == id).flatMap(modelToData)
      }
    }
  }

  def fetchStreamlineData(reference: String, id: String)(implicit hc: HeaderCarrier): Future[Option[StreamlineBusiness]] = {
    fetchSoleTraderBusinesses(reference) map {
      case Right(Some(soleTraderBusinesses)) =>
        soleTraderBusinesses.businesses.find(_.id == id) map { business =>
          StreamlineBusiness(
            trade = business.trade,
            name = business.name,
            startDate = business.startDate,
            startDateBeforeLimit = business.startDateBeforeLimit
          )
        }
      case Right(None) => None
      case Left(error) =>
        throw new InternalServerException(s"[MultipleSelfEmploymentsService][fetchStreamlineData] - Unable to fetch sole trader businesses - $error")
    }
  }

  def fetchBusiness(reference: String, id: String)
                   (implicit hc: HeaderCarrier): Future[Either[GetSelfEmploymentsFailure, Option[SoleTraderBusiness]]] = {
    fetchSoleTraderBusinesses(reference) map { result =>
      result map {
        case Some(SoleTraderBusinesses(businesses)) =>
          businesses.find(_.id == id)
        case None => None
      }
    }
  }

  private def saveData(reference: String,
                       id: String,
                       businessUpdate: SoleTraderBusiness => SoleTraderBusiness)
                      (implicit hc: HeaderCarrier): Future[Either[SaveSelfEmploymentDataFailure.type, PostSubscriptionDetailsSuccess]] = {

    def updateSoleTraderBusinesses(soleTraderBusinesses: SoleTraderBusinesses): SoleTraderBusinesses = {
      val updatedBusinessesList: Seq[SoleTraderBusiness] = if (soleTraderBusinesses.businesses.exists(_.id == id)) {
        soleTraderBusinesses.businesses map {
          case business if business.id == id => businessUpdate(business)
          case business => business
        }
      } else {
        soleTraderBusinesses.businesses :+ businessUpdate(SoleTraderBusiness(id = id))
      }

      soleTraderBusinesses.copy(businesses = updatedBusinessesList)
    }

    fetchSoleTraderBusinesses(reference) map { result =>
      result map {
        case Some(soleTraderBusinesses) => soleTraderBusinesses
        case None => SoleTraderBusinesses(businesses = Seq.empty[SoleTraderBusiness])
      } map updateSoleTraderBusinesses
    } flatMap {
      case Right(soleTraderBusinesses) =>
        saveSoleTraderBusinesses(reference, soleTraderBusinesses)
      case Left(_) => Future.successful(Left(SaveSelfEmploymentDataFailure))
    }

  }

  def fetchStartDate(reference: String, businessId: String)
                    (implicit hc: HeaderCarrier): Future[Either[GetSelfEmploymentsFailure, Option[DateModel]]] = {
    findData[DateModel](reference, businessId, _.startDate)
  }

  def saveStartDate(reference: String, businessId: String, startDate: DateModel)
                   (implicit hc: HeaderCarrier): Future[Either[SaveSelfEmploymentDataFailure.type, PostSubscriptionDetailsSuccess]] = {
    saveData(reference, businessId, _.copy(startDate = Some(startDate), confirmed = false))
  }

  def saveAddress(reference: String, businessId: String, address: Address)
                 (implicit hc: HeaderCarrier): Future[Either[SaveSelfEmploymentDataFailure.type, PostSubscriptionDetailsSuccess]] = {
    saveData(reference, businessId, _.copy(address = Some(address), confirmed = false))
  }

  def confirmBusiness(reference: String, businessId: String)
                     (implicit hc: HeaderCarrier): Future[Either[SaveSelfEmploymentDataFailure.type, PostSubscriptionDetailsSuccess]] = {
    saveData(reference, businessId, _.copy(confirmed = true))
  }

  def saveStreamlinedIncomeSource(reference: String,
                                  businessId: String,
                                  trade: String,
                                  name: String,
                                  startDateBeforeLimit: Boolean)
                                 (implicit hc: HeaderCarrier): Future[Either[SaveSelfEmploymentDataFailure.type, PostSubscriptionDetailsSuccess]] = {
    saveData(
      reference = reference,
      id = businessId,
      businessUpdate = _.copy(name = Some(name), trade = Some(trade), startDateBeforeLimit = Some(startDateBeforeLimit), confirmed = false)
    )
  }

  def fetchFirstAddress(reference: String)
                       (implicit hc: HeaderCarrier): Future[Either[GetSelfEmploymentsFailure, Option[Address]]] = {
    fetchSoleTraderBusinesses(reference) map { result =>
      result.map { maybeBusinesses =>
        maybeBusinesses.flatMap { soleTraderBusinesses =>
          soleTraderBusinesses.businesses.flatMap(_.address).headOption
        }
      }
    }
  }

}

object MultipleSelfEmploymentsService {

  case object SaveSelfEmploymentDataFailure

}
