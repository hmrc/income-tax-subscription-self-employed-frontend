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

import crypto.ComposeCryptoService
import play.api.libs.json.{OFormat, Reads}
import play.api.mvc.Request
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.SessionDataConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSessionDataHttpParser.GetSessionDataResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.SaveSessionDataHttpParser.SaveSessionDataSuccess
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.requests.agent.IdentifierRequest
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{DuplicateDetails, UserDetailsModel}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.{ITSASessionKeys, SensitiveStringProvider}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionDataService @Inject()(composeCryptoService: ComposeCryptoService, sessionDataConnector: SessionDataConnector)
                                  (implicit ec: ExecutionContext) {

  implicit val jsonCrypto: Encrypter with Decrypter = composeCryptoService.composedJsonCrypto

  def fetchReference(implicit hc: HeaderCarrier): Future[GetSessionDataResponse[String]] = {
    sessionDataConnector.getSessionData[String](ITSASessionKeys.REFERENCE)
  }

  def fetchNino(implicit hc: HeaderCarrier): Future[GetSessionDataResponse[String]] = {
    sessionDataConnector.getSessionData[String](ITSASessionKeys.NINO)
  }

  // TODO: Temporary function to accommodate existing users' journeys. Can be removed once complete
  def fetchUserDetailsCookie(implicit request: Request[_]): Option[UserDetailsModel] = {
    (request.session.get("FirstName"), request.session.get("LastName")) match {
      case (Some(f), Some(l)) => Some(UserDetailsModel(firstName = f, lastName = l))
      case _ => None
    }
  }

  def fetchUserDetails(implicit hc: HeaderCarrier,
                       userDetailsEncryption: Reads[UserDetailsModel],
                       request: Request[_]): Future[GetSessionDataResponse[UserDetailsModel]] = {
    sessionDataConnector.getSessionData[UserDetailsModel](ITSASessionKeys.UserDetailsKey) flatMap {
      case Right(None) => Future.successful(Right(fetchUserDetailsCookie))
      case sessionDataResponse => Future.successful(sessionDataResponse)
    }
  }

  def getDuplicateDetails(id: String)(implicit hc: HeaderCarrier): Future[Option[DuplicateDetails]] = {
    sessionDataConnector.getSessionData[DuplicateDetails](ITSASessionKeys.DUPLICATE_DETAILS)(implicitly, DuplicateDetails.encryptedFormat) map {
      case Right(maybeDuplicateDetails) =>
        maybeDuplicateDetails.filter(_.id == id)
      case Left(error) =>
        throw new InternalServerException(s"[SessionDataService][getDuplicateDetails] - Unable to get duplicate details from session - $error")
    }
  }

  def saveDuplicateDetails(duplicateBusinessDetails: DuplicateDetails)(implicit hc: HeaderCarrier): Future[SaveSessionDataSuccess] = {
    sessionDataConnector.saveSessionData(ITSASessionKeys.DUPLICATE_DETAILS, duplicateBusinessDetails)(implicitly, DuplicateDetails.encryptedFormat) map {
      case Right(success) =>
        success
      case Left(error) =>
        throw new InternalServerException(s"[SessionDataService][saveDuplicateDetails] - Unable to save duplicate details to session - $error")
    }
  }

}