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

package controllers.agent

import helpers.servicemocks.AuthStub
import helpers.{ComponentSpecBase, CustomMatchers}
import play.api.http.Status.{OK, SEE_OTHER}

class SessionTimeoutControllerISpec extends ComponentSpecBase with CustomMatchers {

  "GET /report-quarterly/income-and-expenses/sign-up/self-employments/keep-alive" when {
    "an agent chooses to not time out" should {
      "return an OK and keep the session" in {

        val res = getClientKeepAlive
        Then("Should return a OK")
        res must have(
          httpStatus(OK)
        )
      }
    }
  }

  "GET /report-quarterly/income-and-expenses/sign-up/timeout" when {
    "an agent times out" should {
      "redirect and sign out the user" in {
        AuthStub.stubAuthSuccess()
        val res = getClientTimeout
        res must have(
          httpStatus(SEE_OTHER),
          redirectURI("http://localhost:9553/bas-gateway/sign-in?continue_url=http%3A%2F%2Flocalhost%3A9561%2Freport-quarterly%2Fincome-and-expenses%2Fsign-up%2Fclient&origin=income-tax-subscription-self-employed-frontend")
        )
      }
    }
  }
}
