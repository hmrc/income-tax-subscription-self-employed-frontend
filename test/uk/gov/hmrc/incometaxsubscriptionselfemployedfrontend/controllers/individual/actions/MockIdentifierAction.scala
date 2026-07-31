/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.individual.actions

import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.individual.IdentifierRequest
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.utils.ReferenceRetrievalInj
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{BodyParsers, Request, Result}
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MockIdentifierAction extends MockitoSugar with BeforeAndAfterEach {
  suite: Suite =>

  val reference: String = "test-reference"

  private val mockReferenceRetrieval = mock[ReferenceRetrievalInj]

  val fakeIdentifierAction: IdentifierAction =
    new IdentifierAction(
      mock[AuthConnector],
      mock[BodyParsers.Default]
    )(
      mockReferenceRetrieval,
      mock[AppConfig]
    ) {
      override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {
        block(
          IdentifierRequest(
            request = request,
            reference = reference
          )
        )
      }
    }

  def fakeIdentifierActionWithReference(reference: String): IdentifierAction =
    new IdentifierAction(
      mock[AuthConnector],
      mock[BodyParsers.Default]
    )(
      mockReferenceRetrieval,
      mock[AppConfig]
    ) {
      override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {
        block(
          IdentifierRequest(
            request = request,
            reference = reference
          )
        )
      }
    }
}