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

import play.api.mvc.Request
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.requests.agent.IdentifierRequest
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{ClientDetails, UserDetailsModel}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.SensitiveStringProvider

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ClientDetailsRetrieval @Inject()(sessionDataService: SessionDataService,
                                       sensitiveStringProvider: SensitiveStringProvider)
                                      (implicit ec: ExecutionContext) {

  def getClientDetails(implicit hc: HeaderCarrier, request: Request[_]): Future[ClientDetails] = {
    for {
      nino <- sessionDataService.fetchNino
      userDetails <- sessionDataService.fetchUserDetails(
        implicitly,
        UserDetailsModel.encryptedFormat(sensitiveStringProvider.sensitiveFormat),
        implicitly
      )
    } yield (nino, userDetails) match {
      case (Right(Some(nino)), Right(Some(userDetails))) =>
        ClientDetails(
          name = s"${userDetails.firstName} ${userDetails.lastName}",
          nino = nino
        )
      case (Right(None), _) => throw new NoNinoInSessionException
      case (_, Right(None)) => throw new NoUserDetailsInSessionException
      case (Left(error), _) => throw new FetchNinoFromSessionException(error.toString)
      case (_, Left(error)) => throw new FetchUserDetailsFromSessionException(error.toString)
    }
  }

  private class NoNinoInSessionException extends InternalServerException(
    s"[ClientDetailsRetrieval][getClientDetails] - no nino value present in session"
  )

  private class NoUserDetailsInSessionException extends InternalServerException(
    s"[ClientDetailsRetrieval][getClientDetails] - no user details value present in session"
  )

  private class FetchNinoFromSessionException(error: String) extends InternalServerException(
    s"[ClientDetailsRetrieval][getClientDetails] - failure when fetching nino from session: $error"
  )

  private class FetchUserDetailsFromSessionException(error: String) extends InternalServerException(
    s"[ClientDetailsRetrieval][getClientDetails] - failure when fetching user details from session: $error"
  )

}
