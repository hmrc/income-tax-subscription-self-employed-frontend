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

package controllers.agent

import helpers.ComponentSpecBase
import helpers.servicemocks.AuthStub.stubAuthSuccess
import play.api.http.Status._

class InitialiseControllerISpec extends ComponentSpecBase {

  "GET /report-quarterly/income-and-expenses/sign-up/self-employments/client/details" when {
    "redirect to the full sole trader income source page" in {
      Given("I setup the Wiremock stubs")
      stubAuthSuccess()

      When("GET /details is called")
      val res = getClientInitialise

      Then("should redirect to the full income source page page")
      res must have(
        httpStatus(SEE_OTHER),
        redirectURI("/client/details/sole-trader-business")
      )
    }
  }
}
