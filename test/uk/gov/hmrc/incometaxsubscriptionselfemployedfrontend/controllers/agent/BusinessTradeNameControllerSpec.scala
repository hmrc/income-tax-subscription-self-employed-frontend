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

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.mvc.{Action, AnyContent}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.UnexpectedStatusFailure
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSubscriptionDetailsSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.ControllerBaseSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.BusinessTradeNameForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.mocks.{MockMultipleSelfEmploymentsService, MockSessionDataService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.TestModels.{testBusinessNameModel, testValidBusinessTradeName, testValidBusinessTradeNameModel}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.BusinessTradeName

class BusinessTradeNameControllerSpec extends ControllerBaseSpec
  with MockMultipleSelfEmploymentsService with MockSessionDataService {

  val id: String = "testId"

  override val controllerName: String = "BusinessTradeNameController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map()

  private def withController(testCode: BusinessTradeNameController => Any): Unit = {
    val businessTradeNameView = mock[BusinessTradeName]

    when(businessTradeNameView(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
    (ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(HtmlFormat.empty)

    val controller = new BusinessTradeNameController(
      mockMessagesControllerComponents,
      businessTradeNameView,
      mockMultipleSelfEmploymentsService,
      mockAuthService
    )(
      mockSessionDataService,
      appConfig
    )

    testCode(controller)
  }

  def modelToFormData(businessTradeNameModel: String): Seq[(String, String)] = {
    BusinessTradeNameForm.tradeValidationForm(Nil).fill(businessTradeNameModel).data.toSeq
  }

  "Show" should {
    "return ok (200)" when {
      "the connector returns data" in withController { controller =>
        mockAuthSuccess()
        mockFetchAllNameTradeCombos(Right(Seq(
          (id, Some(testBusinessNameModel), Some(testValidBusinessTradeName))
        )))

        val result = controller.show(id, isEditMode = false)(fakeRequest)

        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
      }
      "the connector returns no data" in withController { controller =>
        mockAuthSuccess()
        mockFetchAllNameTradeCombos(Right(Seq(
          (id, Some(testBusinessNameModel), None)
        )))

        val result = controller.show(id, isEditMode = false)(fakeRequest)

        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
      }
    }
    "Throw an internal exception error" when {
      "the connector returns an error" in withController { controller =>
        mockAuthSuccess()
        mockFetchAllNameTradeCombos(Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))

        intercept[InternalServerException](await(controller.show(id, isEditMode = false)(fakeRequest)))
      }
    }
  }

  "Submit when not in edit mode" should {
    "return 303, SEE_OTHER" when {
      "the user submits valid data" in withController { controller =>
        mockAuthSuccess()
        mockFetchAllNameTradeCombos(Right(Seq(
          (id, Some(testBusinessNameModel), None)
        )))
        mockSaveBusinessTrade(id, testValidBusinessTradeNameModel)(Right(PostSubscriptionDetailsSuccessResponse))

        val result = controller.submit(id, isEditMode = false)(
          fakeRequest.withFormUrlEncodedBody(modelToFormData(testValidBusinessTradeNameModel): _*)
        )

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.AddressLookupRoutingController.checkAddressLookupJourney(id).url)
      }
    }
    "return 400, SEE_OTHER" when {
      "the user submits invalid data" in withController { controller =>
        mockAuthSuccess()
        mockFetchAllNameTradeCombos(Right(Seq(
          (id, Some(testBusinessNameModel), None)
        )))

        val result = controller.submit(id, isEditMode = false)(fakeRequest)

        status(result) mustBe BAD_REQUEST
        contentType(result) mustBe Some("text/html")
      }
      "the user submits a trade which causes a duplicate business name/trade combo" in withController { controller =>
        mockAuthSuccess()
        mockFetchAllNameTradeCombos(Right(Seq(
          ("idOne", Some("nameOne"), Some("tradeOne")),
          ("idTwo", Some("nameOne"), None)
        )))

        val result = controller.submit("idTwo", isEditMode = false)(
          fakeRequest.withFormUrlEncodedBody(modelToFormData("tradeOne"): _*)
        )

        status(result) mustBe BAD_REQUEST
        contentType(result) mustBe Some("text/html")
      }
    }
  }

  "Submit when in edit mode" should {
    "return 303, SEE_OTHER" when {
      "the user submits valid data" in withController { controller =>
        mockAuthSuccess()
        mockFetchAllNameTradeCombos(Right(Seq(
          (id, Some(testBusinessNameModel), Some("tradeOne"))
        )))
        mockSaveBusinessTrade(id, testValidBusinessTradeNameModel)(Right(PostSubscriptionDetailsSuccessResponse))

        val result = controller.submit(id, isEditMode = true)(
          fakeRequest.withFormUrlEncodedBody(modelToFormData(testValidBusinessTradeNameModel): _*)
        )
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.SelfEmployedCYAController.show(id, isEditMode = true).url)
      }

      "the user does not update their trade in edit mode" in withController { controller =>
        mockAuthSuccess()
        mockFetchAllNameTradeCombos(Right(Seq(
          (id, Some(testBusinessNameModel), Some(testValidBusinessTradeNameModel))
        )))
        mockSaveBusinessTrade(id, testValidBusinessTradeNameModel)(Right(PostSubscriptionDetailsSuccessResponse))

        val result = controller.submit(id, isEditMode = true)(
          fakeRequest.withFormUrlEncodedBody(modelToFormData(testValidBusinessTradeNameModel): _*)
        )

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.SelfEmployedCYAController.show(id, isEditMode = true).url)
      }
    }
    "return 400, SEE_OTHER" when {
      "the user submits invalid data" in withController { controller =>
        mockAuthSuccess()
        mockFetchAllNameTradeCombos(Right(Seq(
          (id, Some(testBusinessNameModel), Some(testValidBusinessTradeNameModel))
        )))

        val result = controller.submit(id, isEditMode = true)(fakeRequest)

        status(result) mustBe BAD_REQUEST
        contentType(result) mustBe Some("text/html")
      }
    }
  }

  "The back url" when {
    "in edit mode" should {
      s"redirect to ${routes.SelfEmployedCYAController.show(id).url}" in withController { controller =>
        controller.backUrl(id, isEditMode = true) mustBe routes.SelfEmployedCYAController.show(id, isEditMode = true).url
      }
    }
    "not in edit mode" should {
      s"redirect to ${routes.BusinessStartDateController.show(id).url}" in withController { controller =>
        controller.backUrl(id, isEditMode = false) mustBe routes.BusinessStartDateController.show(id).url
      }
    }
  }

  authorisationTests()

}
