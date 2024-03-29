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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers

import org.scalatest.matchers.should.Matchers._
import play.api.mvc.{Action, AnyContent, AnyContentAsEmpty}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AffinityGroup

class SignOutControllerSpec extends ControllerBaseSpec {

  object TestSignOutController extends SignOutController(
    mockMessagesControllerComponents, appConfig, mockAuthService)

  override val controllerName: String = "SignOutController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map.empty
  val testOrigin = "/hello-world"

  "Authorised users" when {
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = fakeRequest
    "with an agent affinity group" should {
      "be redirected to the gg signOut with an url end /ITSU-A" in {
        mockRetrievalSuccess(Some(AffinityGroup.Agent))

        val result = TestSignOutController.signOut(request)
        status(result) shouldBe SEE_OTHER
        val agentRedirectUrl = appConfig.feedbackFrontendRedirectUrlAgent
        agentRedirectUrl should endWith("ITSU-A")
        redirectLocation(result).get should be(appConfig.ggSignOutUrl(agentRedirectUrl))
      }
    }
    "with an individual affinity group" should {
      "be redirected to the gg signOut with an url end with /ITSU " in {
        mockRetrievalSuccess(Some(AffinityGroup.Individual))

        val result = TestSignOutController.signOut(request)
        status(result) shouldBe SEE_OTHER
        val individualRedirectUrl = appConfig.feedbackFrontendRedirectUrl
        individualRedirectUrl should endWith("ITSU")
        redirectLocation(result).get should be(appConfig.ggSignOutUrl(individualRedirectUrl))
      }

    }
  }

  authorisationTests()

}