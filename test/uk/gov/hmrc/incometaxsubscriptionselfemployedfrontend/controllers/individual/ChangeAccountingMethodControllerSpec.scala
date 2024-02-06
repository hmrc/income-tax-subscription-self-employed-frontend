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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.mvc.{Action, AnyContent}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.mocks.MockIncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.ControllerBaseSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.ChangeAccountingMethod

class ChangeAccountingMethodControllerSpec extends ControllerBaseSpec
  with MockIncomeTaxSubscriptionConnector
  with FeatureSwitching {

  private val id: String = "testId"

  override val controllerName: String = "ChangeAccountingMethodController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map(
    "show" -> TestChangeAccountingMethodController.show(id = id),
    "submit" -> TestChangeAccountingMethodController.submit(id = id)
  )

  private object TestChangeAccountingMethodController extends ChangeAccountingMethodController(
    mock[ChangeAccountingMethod],
    mockMessagesControllerComponents,
    mockAuthService
  )

  "show" should {
    "return ok (200)" in withController { controller =>
      mockAuthSuccess()

      val result = controller.show(id = id)(fakeRequest)

      status(result) mustBe OK
      contentType(result) mustBe Some("text/html")
    }
  }

  "submit" should {
    "return 303, SEE_OTHER to the business accounting method page" in withController { controller =>
      mockAuthSuccess()

      val result = controller.submit(id = id)(fakeRequest)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe
        Some(routes.BusinessAccountingMethodController.show(id, isEditMode = true).url)
    }
  }

  "The back url" should {
    "return a link to the self employment check your answer page" in {
      TestChangeAccountingMethodController.backUrl(id) mustBe routes.SelfEmployedCYAController.show(id, isEditMode = true).url
    }
  }

  authorisationTests()

  private def withController(testCode: ChangeAccountingMethodController => Any): Unit = {
    val changeAccountingMethodView = mock[ChangeAccountingMethod]

    when(changeAccountingMethodView(any(), any())(any(), any()))
      .thenReturn(HtmlFormat.empty)

    val controller = new ChangeAccountingMethodController(
      changeAccountingMethodView,
      mockMessagesControllerComponents,
      mockAuthService
    )

    testCode(controller)
  }

}
