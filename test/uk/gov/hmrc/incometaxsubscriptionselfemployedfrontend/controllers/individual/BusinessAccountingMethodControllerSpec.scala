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
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.UnexpectedStatusFailure
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSubscriptionDetailsSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.ControllerBaseSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessAccountingMethodForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.mocks.{MockMultipleSelfEmploymentsService, MockSessionDataService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.TestModels._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.BusinessAccountingMethod

class BusinessAccountingMethodControllerSpec extends ControllerBaseSpec
  with MockSessionDataService with MockMultipleSelfEmploymentsService
  with FeatureSwitching {

  override val controllerName: String = "BusinessAccountingMethodController"
  private val testId = "testId"
  private val id: String = testId
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map(
    "show" -> TestBusinessAccountingMethodController.show(id = id, isEditMode = false, isGlobalEdit = false),
    "submit" -> TestBusinessAccountingMethodController.submit(id = id, isEditMode = false, isGlobalEdit = false)
  )

  private object TestBusinessAccountingMethodController extends BusinessAccountingMethodController(
    mock[BusinessAccountingMethod],
    mockMessagesControllerComponents,
    mockMultipleSelfEmploymentsService,
    mockAuthService
  )(
    mockSessionDataService,
    appConfig
  )

  def modelToFormData(accountingMethodModel: AccountingMethod): Seq[(String, String)] = {
    BusinessAccountingMethodForm.businessAccountingMethodForm.fill(accountingMethodModel).data.toSeq
  }

  val soleTraderBusinesses: SoleTraderBusinesses = SoleTraderBusinesses(
    businesses = Seq(SoleTraderBusiness(id)),
    accountingMethod = Some(Cash)
  )

  "Show" should {
    "return ok (200)" when {
      "the connector returns data" in withController { controller =>
        mockAuthSuccess()
        mockFetchAccountingMethod(Right(Some(testAccountingMethodModel)))
        mockFetchSoleTraderBusinesses(Right(Some(soleTraderBusinesses)))

        val result = controller.show(id = id, isEditMode = false, isGlobalEdit = false)(fakeRequest)
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
      }
      "the connector returns no data" in withController { controller =>
        mockAuthSuccess()
        mockFetchAccountingMethod(Right(None))
        mockFetchSoleTraderBusinesses(Right(Some(soleTraderBusinesses)))

        val result = controller.show(id = id, isEditMode = false, isGlobalEdit = false)(fakeRequest)
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
      }
    }
    "Throw an internal exception" when {
      "there is an unexpected status failure from the accounting method retrieval" in withController { controller =>
        mockAuthSuccess()
        mockFetchAccountingMethod(Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))

        val response = intercept[InternalServerException](await(controller.show(id = id, isEditMode = false, isGlobalEdit = false)(fakeRequest)))
        response.message mustBe "[BusinessAccountingMethodController][withAccountingMethod] - Failed to retrieve accounting method"
      }

      "there is an unexpected status failure from the get all businesses call" in withController { controller =>
        mockAuthSuccess()
        mockFetchAccountingMethod(Right(None))
        mockFetchSoleTraderBusinesses(Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))

        val response = intercept[InternalServerException](await(controller.show(id = id, isEditMode = false, isGlobalEdit = false)(fakeRequest)))
        response.message mustBe "[BusinessAccountingMethodController][withSelfEmploymentsCount] - Failed to retrieve all self employments"
      }
    }

  }

  "Submit" should {
    "return 303, SEE_OTHER)" when {
      "the user submits valid data" when {
        "not in edit mode" when {
          "an ID is provided" should {
            "redirect to the self employed CYA page" in withController { controller =>
              mockAuthSuccess()
              mockSaveAccountingMethod(testAccountingMethodModel)(Right(PostSubscriptionDetailsSuccessResponse))

              val result = controller.submit(id = id, isEditMode = false, isGlobalEdit = false)(
                fakeRequest.withFormUrlEncodedBody(modelToFormData(testAccountingMethodModel): _*)
              )

              status(result) mustBe SEE_OTHER
              redirectLocation(result) mustBe Some(routes.SelfEmployedCYAController.show(testId).url)
            }
          }

        }

        "in edit mode" when {
          "an ID is provided" should {
            "redirect to the self employed CYA page" in withController { controller =>
              mockAuthSuccess()
              mockSaveAccountingMethod(testAccountingMethodModel)(Right(PostSubscriptionDetailsSuccessResponse))

              val result = controller.submit(id = id, isEditMode = true, isGlobalEdit = false)(
                fakeRequest.withFormUrlEncodedBody(modelToFormData(testAccountingMethodModel): _*)
              )

              status(result) mustBe SEE_OTHER
              redirectLocation(result) mustBe Some(routes.SelfEmployedCYAController.show(testId, isEditMode = true).url)
            }
          }

        }

        "in global edit mode" when {
          "an ID is provided" should {
            "redirect to the self employed CYA page" in withController { controller =>
              mockAuthSuccess()
              mockSaveAccountingMethod(testAccountingMethodModel)(Right(PostSubscriptionDetailsSuccessResponse))

              val result = controller.submit(id = id, isEditMode = true, isGlobalEdit = true)(
                fakeRequest.withFormUrlEncodedBody(modelToFormData(testAccountingMethodModel): _*)
              )

              status(result) mustBe SEE_OTHER
              redirectLocation(result) mustBe Some(routes.SelfEmployedCYAController.show(testId, isEditMode = true, isGlobalEdit = true).url)
            }
          }

        }
      }
    }
    "return 400, SEE_OTHER)" when {
      "the user submits invalid data" in withController { controller =>
        mockAuthSuccess()
        mockFetchSoleTraderBusinesses(Right(Some(soleTraderBusinesses)))

        val result = controller.submit(id = id, isEditMode = false, isGlobalEdit = false)(fakeRequest)

        status(result) mustBe BAD_REQUEST
        contentType(result) mustBe Some("text/html")
      }
    }
  }

  "The back url" when {
    "not in edit mode" should {
      "return None" in withController { controller =>
        mockAuthSuccess()
        controller.backUrl(id = testId, isEditMode = false, isGlobalEdit = false, selfEmploymentCount = 0) mustBe None
      }
    }

    "in edit mode" when {
      "the number of self-employment businesses is more than 1" should {
        "return a url for the change accounting method page" in withController { controller =>
          mockAuthSuccess()
          controller.backUrl(id = testId, isEditMode = true, isGlobalEdit = false, selfEmploymentCount = 2) mustBe
            Some(routes.ChangeAccountingMethodController.show(testId, isGlobalEdit = false).url)
        }
      }
      "the number of self-employment businesses is 1" should {
        "return a url for the self-employed CYA page" in withController { controller =>
          mockAuthSuccess()
          controller.backUrl(id = testId, isEditMode = true, isGlobalEdit = false, selfEmploymentCount = 1) mustBe
            Some(routes.SelfEmployedCYAController.show(testId, isEditMode = true, isGlobalEdit = false).url)
        }
      }
    }

    "in global edit mode" when {
      "the number of self-employment businesses is more than 1" should {
        "return a url for the change accounting method page" in withController { controller =>
          mockAuthSuccess()
          controller.backUrl(id = testId, isEditMode = false, isGlobalEdit = true, selfEmploymentCount = 2) mustBe
            Some(routes.ChangeAccountingMethodController.show(testId, isGlobalEdit = true).url)
        }
      }
      "the number of self-employment businesses is 1" should {
        "return a url for the self-employed CYA page" in withController { controller =>
          mockAuthSuccess()
          controller.backUrl(id = testId, isEditMode = false, isGlobalEdit = true, selfEmploymentCount = 1) mustBe
            Some(routes.SelfEmployedCYAController.show(testId, isEditMode = true, isGlobalEdit = true).url)
        }
      }
    }
  }


  authorisationTests()

  private def withController(testCode: BusinessAccountingMethodController => Any): Unit = {
    val businessAccountingMethodView = mock[BusinessAccountingMethod]

    when(businessAccountingMethodView(any(), any(), any(), any())(any(), any()))
      .thenReturn(HtmlFormat.empty)

    val controller = new BusinessAccountingMethodController(
      businessAccountingMethodView,
      mockMessagesControllerComponents,
      mockMultipleSelfEmploymentsService,
      mockAuthService
    )(
      mockSessionDataService,
      appConfig
    )

    testCode(controller)
  }
}
