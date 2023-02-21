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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.utils

import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.PlaySpec
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.mvc.{AnyContent, Request, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout, status}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.IncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.RetrieveReferenceHttpParser.{InvalidJsonFailure, UnexpectedStatusFailure}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.mocks.MockIncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ITSASessionKeys

import scala.concurrent.{ExecutionContext, Future}

class ReferenceRetrievalSpec extends PlaySpec with Matchers with MockIncomeTaxSubscriptionConnector with Results {

  object TestReferenceRetrieval extends ReferenceRetrieval {
    override implicit val ec: ExecutionContext = executionContext
    override val incomeTaxSubscriptionConnector: IncomeTaxSubscriptionConnector = mockIncomeTaxSubscriptionConnector
  }

  implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  val utr: String = "1234567890"
  val reference: String = "test-reference"

  "withReference" should {
    "return an exception" when {
      "the user's utr is not in session" in {
        implicit val request: Request[AnyContent] = FakeRequest().withSession()

        intercept[InternalServerException](await(TestReferenceRetrieval.withReference { reference =>
          Future.successful(Ok(reference))
        })).message mustBe "[ReferenceRetrieval][withReference] - Unable to retrieve users utr"
      }
      "reference is not already in session and the retrieval returns an InvalidJson error" in {
        mockRetrieveReference(utr)(Left(InvalidJsonFailure))

        implicit val request: Request[AnyContent] = FakeRequest().withSession(ITSASessionKeys.UTR -> utr)

        intercept[InternalServerException](await(TestReferenceRetrieval.withReference { reference =>
          Future.successful(Ok(reference))
        })).message mustBe "[ReferenceRetrieval][withReference] - Unable to parse json returned"
      }
      "reference is not already in session and the retrieval returns an UnexpectedStatus error" in {
        mockRetrieveReference(utr)(Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))

        implicit val request: Request[AnyContent] = FakeRequest().withSession(ITSASessionKeys.UTR -> utr)

        intercept[InternalServerException](await(TestReferenceRetrieval.withReference { reference =>
          Future.successful(Ok(reference))
        })).message mustBe s"[ReferenceRetrieval][withReference] - Unexpected status returned: $INTERNAL_SERVER_ERROR"
      }
    }
    "pass the reference through to the provided function" when {
      "the reference is already in session" in {
        implicit val request: Request[AnyContent] = FakeRequest().withSession(ITSASessionKeys.UTR -> utr, ITSASessionKeys.REFERENCE -> reference)

        val result = TestReferenceRetrieval.withReference { reference =>
          Future.successful(Ok(reference))
        }

        status(result) mustBe OK
      }
      "the reference is not in session and we call out to retrieve the reference successfully and add the reference to the session" in {
        mockRetrieveReference(utr)(Right(reference))

        implicit val request: Request[AnyContent] = FakeRequest().withSession(ITSASessionKeys.UTR -> utr)

        val result = TestReferenceRetrieval.withReference { reference =>
          Future.successful(Ok(reference))
        }

        status(result) mustBe OK
      }
    }
  }
}
