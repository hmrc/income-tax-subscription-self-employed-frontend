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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.individual

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, when}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.utils.ReferenceRetrievalInj
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.mvc.Results.Redirect
import play.api.mvc.{BodyParsers, Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.auth.MockAuth
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.individual.actions.IdentifierAction

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class IdentifierActionSpec extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterEach with MockAuth {

  val mockReferenceRetrieval: ReferenceRetrievalInj = mock[ReferenceRetrievalInj]

  val mockAppConfig: AppConfig = mock[AppConfig]

  val identifierAction: IdentifierAction = new IdentifierAction(
    authConnector = mockAuth,
    parser = app.injector.instanceOf[BodyParsers.Default]
  )(
    mockReferenceRetrieval,
    mockAppConfig
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(
      mockReferenceRetrieval,
      mockAppConfig
    )
  }

  val reference = "test-reference"

  "IdentifierAction" when {
    "the user is authorised and has a reference" must {
      "create an IdentifierRequest containing the reference" in {

        mockAuthSuccess()

        when(mockReferenceRetrieval.getReference(
          ArgumentMatchers.any[HeaderCarrier]
        )).thenReturn(Future.successful(Some(reference)))

        val result: Future[Result] = identifierAction { request =>
          request.reference mustBe reference
          Results.Ok
        }(FakeRequest())

        status(result) mustBe OK
      }
    }

    "the user is authorised but has no reference" must {
      "redirect to the your income sources page" in {

        mockAuthSuccess()

        val yourIncomeSourcesUrl = "/test-your-income-sources"

        when(mockReferenceRetrieval.getReference(
            ArgumentMatchers.any[HeaderCarrier]
          )
        ).thenReturn(Future.successful(None))

        when(mockAppConfig.yourIncomeSourcesUrl).thenReturn(yourIncomeSourcesUrl)

        val result: Future[Result] = identifierAction { _ =>
            Results.Ok
          }(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(yourIncomeSourcesUrl)
      }
    }

    "authorisation throws an AuthorisationException" must {
      "redirect the user to login" in {

        mockAuthUnauthorised()

        val requestPath = "/test-url"
        val loginUrl = "/test-login"

        when(mockAppConfig.redirectToLogin(requestPath)).thenReturn(Redirect(loginUrl))

        val result: Future[Result] =
          identifierAction { _ =>
            Results.Ok
          }(
            FakeRequest(
              method = "GET",
              path = requestPath
            )
          )

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(loginUrl)
      }
    }
  }
}