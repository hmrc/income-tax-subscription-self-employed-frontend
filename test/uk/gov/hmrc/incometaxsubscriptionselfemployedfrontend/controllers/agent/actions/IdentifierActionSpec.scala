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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.actions

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{BodyParsers, Result}
import play.api.mvc.Results.Ok
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.{AuthConnector, InvalidBearerToken}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.utils.ReferenceRetrievalInj
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.ClientDetails
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.requests.agent.IdentifierRequest
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.ClientDetailsRetrieval
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.UnitTestTrait

import scala.concurrent.Future

class IdentifierActionSpec extends UnitTestTrait with MockitoSugar with BeforeAndAfterEach {

  val mockAuthConnector: AuthConnector = mock[AuthConnector]
  val mockReferenceRetrieval: ReferenceRetrievalInj = mock[ReferenceRetrievalInj]
  val mockClientDetailsRetrieval: ClientDetailsRetrieval = mock[ClientDetailsRetrieval]

  val testReference: String = "test-reference"
  val testClientDetails: ClientDetails = ClientDetails("FirstName LastName", "AA111111A")

  lazy val bodyParser: BodyParsers.Default = app.injector.instanceOf[BodyParsers.Default]

  def identifierAction: IdentifierAction = new IdentifierAction(
    mockAuthConnector, bodyParser
  )(
    mockReferenceRetrieval, mockClientDetailsRetrieval, appConfig
  )

  val testBlock: IdentifierRequest[_] => Future[Result] = _ => Future.successful(Ok)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuthConnector, mockReferenceRetrieval, mockClientDetailsRetrieval)
  }

  "invokeBlock" when {
    "the user is authorised" when {
      "a reference is present in session" should {
        "fetch the client details and call the block with an IdentifierRequest" in {
          when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
            .thenReturn(Future.successful(()))
          when(mockReferenceRetrieval.getReference(any()))
            .thenReturn(Future.successful(Some(testReference)))
          when(mockClientDetailsRetrieval.getClientDetails(any(), any()))
            .thenReturn(Future.successful(testClientDetails))

          val result = identifierAction.invokeBlock(FakeRequest(), testBlock)

          status(result) mustBe OK
        }
      }

      "no reference is present in session" should {
        "redirect to the client your income sources page" in {
          when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
            .thenReturn(Future.successful(()))
          when(mockReferenceRetrieval.getReference(any()))
            .thenReturn(Future.successful(None))

          val result = identifierAction.invokeBlock(FakeRequest(), testBlock)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(appConfig.clientYourIncomeSourcesUrl)
        }
      }
    }

    "the user is unauthorised" should {
      "redirect to the login page" in {
        when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
          .thenReturn(Future.failed(InvalidBearerToken()))

        val request = FakeRequest("GET", "/some-path")
        val result = identifierAction.invokeBlock(request, testBlock)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe redirectLocation(Future.successful(appConfig.redirectToLogin(request.path)))
      }
    }
  }
}