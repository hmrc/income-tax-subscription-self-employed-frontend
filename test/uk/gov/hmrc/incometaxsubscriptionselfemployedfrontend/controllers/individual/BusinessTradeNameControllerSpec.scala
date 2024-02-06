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

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.mvc.{Action, AnyContent}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitch.EnableTaskListRedesign
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.UnexpectedStatusFailure
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSubscriptionDetailsSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.mocks.MockIncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.ControllerBaseSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessTradeNameForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.mocks.MockMultipleSelfEmploymentsService
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.TestModels._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.BusinessTradeName

class BusinessTradeNameControllerSpec extends ControllerBaseSpec
  with MockMultipleSelfEmploymentsService with FeatureSwitching with MockIncomeTaxSubscriptionConnector {

  val id: String = "testId"

  override val controllerName: String = "BusinessTradeNameController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map()

  override def beforeEach(): Unit = {
    disable(EnableTaskListRedesign)
    super.beforeEach()
  }

  private def withController(testCode: BusinessTradeNameController => Any): Unit = {
    val businessTradeNameView = mock[BusinessTradeName]

    when(businessTradeNameView(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
    (ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(HtmlFormat.empty)

    val controller = new BusinessTradeNameController(
      mockMessagesControllerComponents,
      businessTradeNameView,
      mockMultipleSelfEmploymentsService,
      mockIncomeTaxSubscriptionConnector,
      mockAuthService
    )

    testCode(controller)
  }

  def modelToFormData(businessTradeNameModel: String): Seq[(String, String)] = {
    BusinessTradeNameForm.businessTradeNameValidationForm(Nil).fill(businessTradeNameModel).data.toSeq
  }

  "Show" should {

    "return ok (200)" when {
      "a trade name combo is returned" in withController { controller =>
        mockAuthSuccess()
        mockFetchAllNameTradeCombos(Right(Seq(
          (id, Some(mockBusinessNameModel), Some(testValidBusinessTradeNameModel))
        )))

        val result = controller.show(id, isEditMode = false)(fakeRequest)

        status(result) mustBe OK
        contentType(result) mustBe Some(HTML)
      }

      "a name is returned without a trade" in withController { controller =>
        mockAuthSuccess()
        mockFetchAllNameTradeCombos(Right(Seq(
          (id, Some(mockBusinessNameModel), None)
        )))

        val result = controller.show(id, isEditMode = false)(fakeRequest)

        status(result) mustBe OK
        contentType(result) mustBe Some(HTML)
      }

      "no name trade combos are returned" in withController { controller =>
        mockAuthSuccess()
        mockFetchAllNameTradeCombos(Right(Seq()))

        val result = controller.show(id, isEditMode = false)(fakeRequest)

        status(result) mustBe OK
        contentType(result) mustBe Some(HTML)
      }
    }

    "throw an internal exception error" when {
      "the connector returns an error" in withController { controller =>
        mockAuthSuccess()
        mockFetchAllNameTradeCombos(Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))

        intercept[InternalServerException](await(controller.show(id, isEditMode = false)(fakeRequest)))
      }
    }

  }

  "Submit" when {
    "not in edit mode" should {
      "return 303, SEE_OTHER and redirect to check address lookup route" when {
        "the user inputs a valid trade" in withController { controller =>
          mockAuthSuccess()
          mockFetchAllNameTradeCombos(Right(Seq(
            (id, Some(mockBusinessNameModel), None)
          )))
          mockSaveBusinessTrade(id, testValidBusinessTradeNameModel)(Right(PostSubscriptionDetailsSuccessResponse))

          val result = controller.submit(id, isEditMode = false)(
            fakeRequest.withFormUrlEncodedBody(modelToFormData(testValidBusinessTradeNameModel): _*)
          )

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.AddressLookupRoutingController.checkAddressLookupJourney(id).url)
        }
        "return 404, BAD_REQUEST" when {
          "the user enters an invalid trade" in withController { controller =>
            mockAuthSuccess()
            mockFetchAllNameTradeCombos(Right(Seq(
              (id, Some(mockBusinessNameModel), None)
            )))

            val result = controller.submit(id, isEditMode = false)(fakeRequest)

            status(result) mustBe BAD_REQUEST
            contentType(result) mustBe Some(HTML)
          }
          "the user enters a trade which would cause a name trade combo conflict with another business" in withController { controller =>
            mockAuthSuccess()
            mockFetchAllNameTradeCombos(Right(Seq(
              ("idOne", Some("nameOne"), Some("tradeOne")),
              ("idTwo", Some("nameOne"), None)
            )))

            val result = controller.submit("idTwo", isEditMode = false)(
              fakeRequest.withFormUrlEncodedBody(modelToFormData("tradeOne"): _*)
            )

            status(result) mustBe BAD_REQUEST
            contentType(result) mustBe Some(HTML)
          }
        }
      }
    }
    "in edit mode" should {
      "return 303, SEE_OTHER to the sole trader check your answers" when {
        "the user updates their trade" in withController { controller =>
          mockAuthSuccess()
          mockFetchAllNameTradeCombos(Right(Seq(
            (id, Some(mockBusinessNameModel), Some(testValidBusinessTradeNameModel))
          )))
          mockSaveBusinessTrade(id, testValidBusinessTradeNameModel)(
            Right(PostSubscriptionDetailsSuccessResponse)
          )

          val result = controller.submit(id, isEditMode = true)(
            fakeRequest.withFormUrlEncodedBody(modelToFormData(testValidBusinessTradeNameModel): _*)
          )

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.SelfEmployedCYAController.show(id, isEditMode = true).url)
        }
        "the user does not update their trade" in withController { controller =>
          mockAuthSuccess()
          mockFetchAllNameTradeCombos(Right(Seq(
            (id, Some(mockBusinessNameModel), Some("tradeOne"))
          )))
          mockSaveBusinessTrade(id, testValidBusinessTradeNameModel)(
            Right(PostSubscriptionDetailsSuccessResponse)
          )

          val result = controller.submit(id, isEditMode = true)(
            fakeRequest.withFormUrlEncodedBody(modelToFormData(testValidBusinessTradeNameModel): _*)
          )

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.SelfEmployedCYAController.show(id, isEditMode = true).url)
        }
      }
    }
  }

  "The back url" when {
    "in edit mode" should {
      s"redirect to ${
        routes.SelfEmployedCYAController.show(id, isEditMode = true).url
      }" in withController {
        controller => {
          controller.backUrl(id, isEditMode = true) mustBe routes.SelfEmployedCYAController.show(id, isEditMode = true).url
        }
      }
      "not in edit mode" should {
        s"redirect to ${
          routes.BusinessStartDateController.show(id).url
        }" in withController {
          controller =>
            controller.backUrl(id, isEditMode = false) mustBe routes.BusinessStartDateController.show(id).url
        }
      }
    }
  }

  authorisationTests()

}
