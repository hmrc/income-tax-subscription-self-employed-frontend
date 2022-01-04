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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.utils

import play.api.mvc.{AnyContent, Request, Result}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.IncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.RetrieveReferenceHttpParser.{InvalidJsonFailure, UnexpectedStatusFailure}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ITSASessionKeys

import scala.concurrent.{ExecutionContext, Future}

trait ReferenceRetrieval {

  val incomeTaxSubscriptionConnector: IncomeTaxSubscriptionConnector
  implicit val ec: ExecutionContext

  def withReference(f: String => Future[Result])
                   (implicit request: Request[AnyContent],
                    hc: HeaderCarrier): Future[Result] = {
    request.session.get(ITSASessionKeys.REFERENCE) match {
      case Some(value) => f(value)
      case None =>
        val utr: String = request.session.get(ITSASessionKeys.UTR).getOrElse(
          throw new InternalServerException("[ReferenceRetrieval][withReference] - Unable to retrieve users utr")
        )
        incomeTaxSubscriptionConnector.retrieveReference(utr) flatMap {
          case Left(InvalidJsonFailure) =>
            throw new InternalServerException("[ReferenceRetrieval][withReference] - Unable to parse json returned")
          case Left(UnexpectedStatusFailure(status)) =>
            throw new InternalServerException(s"[ReferenceRetrieval][withReference] - Unexpected status returned: $status")
          case Right(value) =>
            f(value) map (_.addingToSession(ITSASessionKeys.REFERENCE -> value))
        }
    }
  }

}
