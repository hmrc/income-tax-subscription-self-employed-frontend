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

package controllers.individual

import helpers.ComponentSpecBase
import helpers.servicemocks.AuthStub.stubAuthSuccess
import play.api.http.Status._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching

class InitialiseControllerISpec extends ComponentSpecBase with FeatureSwitching {

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  "GET /report-quarterly/income-and-expenses/sign-up/self-employments/details" should {
    "redirect to self-employment business name page" in {
      Given("I setup the Wiremock stubs")
      stubAuthSuccess()
      When("GET /details is called")
      val res = getInitialise

      Then("should redirect to the BusinessNamePage")
      res must have(
        httpStatus(SEE_OTHER)
      )
    }
  }
}
