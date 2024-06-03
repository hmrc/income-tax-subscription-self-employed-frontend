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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.individual

import org.mockito.Mockito.when
import play.api.mvc.{Action, AnyContent}
import play.api.test.Helpers._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.ControllerBaseSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.mocks.MockMultipleSelfEmploymentsService
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.UUIDGenerator

class InitialiseControllerSpec extends ControllerBaseSpec
  with MockMultipleSelfEmploymentsService {

  override val controllerName: String = "InitialiseController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map()

  val mockUuid: UUIDGenerator = mock[UUIDGenerator]

  when(mockUuid.generateId).thenReturn("testId")

  object TestInitialiseController extends InitialiseController(
    mockMessagesControllerComponents,
    mockAuthService,
    mockUuid
  )(
    appConfig
  )

  "initialise" when {
    s"return $SEE_OTHER and redirect to Business Name Confirmation page" in {
      mockAuthSuccess()

      val result = TestInitialiseController.initialise(fakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.individual.routes.BusinessNameConfirmationController.show("testId").url)
    }
  }

  authorisationTests()

}
