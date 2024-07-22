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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent

import org.mockito.Mockito.when
import play.api.mvc.{Action, AnyContent}
import play.api.test.Helpers._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitch.EnableAgentStreamline
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.ControllerBaseSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{SoleTraderBusiness, SoleTraderBusinesses}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.mocks.{MockMultipleSelfEmploymentsService, MockSessionDataService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.UUIDGenerator

class InitialiseControllerSpec extends ControllerBaseSpec
  with MockSessionDataService
  with MockMultipleSelfEmploymentsService
  with FeatureSwitching {

  override val controllerName: String = "InitialiseController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map()

  val mockUuid: UUIDGenerator = mock[UUIDGenerator]

  when(mockUuid.generateId).thenReturn("testId")

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(EnableAgentStreamline)
  }

  object TestInitialiseController extends InitialiseController(
    mockMessagesControllerComponents,
    mockMultipleSelfEmploymentsService,
    mockAuthService,
    mockUuid
  )(appConfig, mockSessionDataService)

  "initialise" when {
    "the agent streamline feature switch is disabled" should {
      s"return $SEE_OTHER and redirect to Business Name Confirmation page" in {
        mockAuthSuccess()

        val result = TestInitialiseController.initialise(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.BusinessNameConfirmationController.show("testId").url)
      }
    }
    "the agent streamline feature switch is enabled" should {
      s"return $SEE_OTHER and redirect to the first sole trader income source page" when {
        "there are no businesses in the sole trader businesses" in {
          enable(EnableAgentStreamline)

          mockAuthSuccess()
          mockFetchSoleTraderBusinesses(Right(Some(SoleTraderBusinesses(Seq.empty))))

          val result = TestInitialiseController.initialise(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.FirstIncomeSourceController.show("testId").url)
        }
        "there is no sole trader businesses stored" in {
          enable(EnableAgentStreamline)

          mockAuthSuccess()
          mockFetchSoleTraderBusinesses(Right(None))

          val result = TestInitialiseController.initialise(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.FirstIncomeSourceController.show("testId").url)
        }
      }
      s"return $SEE_OTHER and redirect to the next sole trader income source page" when {
        "there are businesses already" in {
          enable(EnableAgentStreamline)

          mockAuthSuccess()
          mockFetchSoleTraderBusinesses(Right(Some(SoleTraderBusinesses(Seq(SoleTraderBusiness(id = "previousId"))))))

          val result = TestInitialiseController.initialise(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.NextIncomeSourceController.show("testId").url)
        }
      }
    }
  }

  authorisationTests()

}
