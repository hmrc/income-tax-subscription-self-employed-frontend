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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.mvc.{Action, AnyContent}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.{InvalidJson, UnexpectedStatusFailure}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSubscriptionDetailsSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.mocks.MockIncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.ControllerBaseSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.BusinessAccountingMethodForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.AccountingMethod
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.mocks.MockMultipleSelfEmploymentsService
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.TestModels.testAccountingMethodModel
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.BusinessAccountingMethod

class BusinessAccountingMethodControllerSpec extends ControllerBaseSpec
  with MockIncomeTaxSubscriptionConnector
  with MockMultipleSelfEmploymentsService
  with FeatureSwitching {

  override val controllerName: String = "BusinessAccountingMethodController"
  private val testId = "testId"
  private val id: String = testId
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map(
    "show" -> TestBusinessAccountingMethodController.show(id = id, isEditMode = false),
    "submit" -> TestBusinessAccountingMethodController.submit(id = id, isEditMode = false)
  )

  private object TestBusinessAccountingMethodController extends BusinessAccountingMethodController(
    mock[BusinessAccountingMethod],
    mockMessagesControllerComponents,
    mockMultipleSelfEmploymentsService,
    mockAuthService
  )(
    mockIncomeTaxSubscriptionConnector
  )

  def modelToFormData(accountingMethodModel: AccountingMethod): Seq[(String, String)] = {
    BusinessAccountingMethodForm.businessAccountingMethodForm.fill(accountingMethodModel).data.toSeq
  }

  "Show" should {

    "return ok (200)" when {
      "the connector returns data" in withController { controller =>
        mockAuthSuccess()
        mockFetchAccountingMethod(Right(Some(testAccountingMethodModel)))

        val result = controller.show(id = id, isEditMode = false)(fakeRequest)

        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
      }
      "the connector returns no data" in withController { controller =>
        mockAuthSuccess()
        mockFetchAccountingMethod(Right(None))

        val result = controller.show(id = id, isEditMode = false)(fakeRequest)

        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
      }
    }
    "Throw an internal exception" when {
      "there is an unexpected status failure" in withController { controller =>
        mockAuthSuccess()
        mockFetchAccountingMethod(Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))

        val response = intercept[InternalServerException](await(controller.show(id = id, isEditMode = false)(fakeRequest)))

        response.message mustBe "[BusinessAccountingMethodController][show] - Unexpected status: 500"
      }

      "there is an invalid Json" in withController { controller =>
        mockAuthSuccess()
        mockFetchAccountingMethod(Left(InvalidJson))

        val response = intercept[InternalServerException](await(controller.show(id = id, isEditMode = false)(fakeRequest)))

        response.message mustBe "[BusinessAccountingMethodController][show] - Invalid Json"
      }
    }
  }

  "Submit" should {
    "return 303, SEE_OTHER not in edit mode" when {
      "the user submits valid data" in withController { controller =>
        mockAuthSuccess()
        mockSaveAccountingMethod(testAccountingMethodModel)(Right(PostSubscriptionDetailsSuccessResponse))

        val result = controller.submit(id = id, isEditMode = false)(
          fakeRequest.withFormUrlEncodedBody(modelToFormData(testAccountingMethodModel): _*)
        )

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.SelfEmployedCYAController.show(id).url)
      }
    }
    "return 303, SEE_OTHER in edit mode" when {
      "the user submits valid data" in withController { controller =>
        mockAuthSuccess()
        mockSaveAccountingMethod(testAccountingMethodModel)(Right(PostSubscriptionDetailsSuccessResponse))

        val result = controller.submit(id = id, isEditMode = true)(
          fakeRequest.withFormUrlEncodedBody(modelToFormData(testAccountingMethodModel): _*)
        )

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.SelfEmployedCYAController.show(id, isEditMode = true).url)
      }
    }
    "return 400, SEE_OTHER" when {
      "the user submits no option" in withController { controller =>
        mockAuthSuccess()

        val result = controller.submit(id = id, isEditMode = false)(fakeRequest)

        status(result) mustBe BAD_REQUEST
        contentType(result) mustBe Some("text/html")
      }
    }
  }

  "The back url" when {
    "not in edit mode" should {
      "return None" in withController { controller =>
        mockAuthSuccess()
        controller.backUrl(id = id, isEditMode = false) mustBe None
      }
    }

    "in edit mode" should {
      "return a url for the self employed CYA page" in withController { controller =>
        mockAuthSuccess()
        controller.backUrl(id = id, isEditMode = true) mustBe
          Some(routes.SelfEmployedCYAController.show(testId, isEditMode = true).url)
      }
    }
  }

  authorisationTests()

  private def withController(testCode: BusinessAccountingMethodController => Any): Unit = {
    val businessAccountingMethodView = mock[BusinessAccountingMethod]

    when(businessAccountingMethodView(any(), any(), any(), any(), any())(any(), any()))
      .thenReturn(HtmlFormat.empty)

    val controller = new BusinessAccountingMethodController(
      businessAccountingMethodView,
      mockMessagesControllerComponents,
      mockMultipleSelfEmploymentsService,
      mockAuthService
    )(
      mockIncomeTaxSubscriptionConnector
    )

    testCode(controller)
  }

}
