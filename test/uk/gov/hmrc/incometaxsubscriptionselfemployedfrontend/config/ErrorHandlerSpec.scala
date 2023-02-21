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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{RequestHeader, Result}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.auth.core.InvalidBearerToken
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.templates.ErrorTemplate
import uk.gov.hmrc.play.bootstrap.config.AuthRedirects

class ErrorHandlerSpec extends AnyFunSuite with MockitoSugar {
  private val anAgentPath = "/x/y/client/z"
  private val anIndividualPath = "/x/y/z"
  private val individualLoginAddress = "http://a.com/b/c"
  private val agentLoginAddress = "http://a.com/b/c/client"

  test("Will redirect agent to agent sign in if session not valid") {
    withErrorHandler(expected = agentLoginAddress) {
      _.resolveError(getMockHeader(anAgentPath), new InvalidBearerToken)
    }
  }

  test("Will redirect individual to individual sign in if session not valid") {
    withErrorHandler(expected = individualLoginAddress) {
      _.resolveError(getMockHeader(anIndividualPath), new InvalidBearerToken)
    }
  }

  test("Will redirect agent to agent sign in if session not exists") {
    withErrorHandler(expected = agentLoginAddress) {
      _.onClientError(
        getMockHeader(anAgentPath),
        play.mvc.Http.Status.FORBIDDEN,
        "Bad agent, you logged out in another tab")
    }
  }

  test("Will redirect individual to individual sign in if session not exists") {
    withErrorHandler(expected = individualLoginAddress) {
      _.onClientError(getMockHeader(anIndividualPath),
        play.mvc.Http.Status.FORBIDDEN,
        "Bad Individual, you logged out in another tab")
    }
  }


  private def withErrorHandler(expected: String)(testCode: ErrorHandler => Any): Unit = {
    val view = mock[ErrorTemplate]
    when(view(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)
    val config: AppConfig = mock[AppConfig]
    when(config.incomeTaxSubscriptionFrontendBaseUrl).thenReturn("http://a.com/b/c")

    trait AuthRedirectsMock {
      this: AuthRedirects =>
      override def toGGLogin(continueUrl: String): Result = {
        assert(continueUrl === expected)
        null
      }
    }

    val errorHandler = new ErrorHandler(
      errorTemplate = view,
      appConfig = config,
      messagesApi = null,
      config = null,
      env = null
    ) with AuthRedirectsMock

    testCode(errorHandler)
  }

  private def getMockHeader(path: String) = {
    val header = mock[RequestHeader]
    when(header.path).thenReturn(path)
    header
  }

}
