/*
 * Copyright 2021 HM Revenue & Customs
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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.mvc.{Action, AnyContent}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.businessAccountingMethodKey
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitch.SaveAndRetrieve
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.{InvalidJson, UnexpectedStatusFailure}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSubscriptionDetailsSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.mocks.MockIncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessAccountingMethodForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.AccountingMethodModel
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.TestModels._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.BusinessAccountingMethod

class BusinessAccountingMethodControllerSpec extends ControllerBaseSpec
  with MockIncomeTaxSubscriptionConnector
  with FeatureSwitching {

  override val controllerName: String = "BusinessAccountingMethodController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map(
    "show" -> TestBusinessAccountingMethodController.show(id = Some("testId"), isEditMode = false),
    "submit" -> TestBusinessAccountingMethodController.submit(id = Some("testId"), isEditMode = false)
  )

  private object TestBusinessAccountingMethodController extends BusinessAccountingMethodController(
    mock[BusinessAccountingMethod],
    mockMessagesControllerComponents,
    mockIncomeTaxSubscriptionConnector,
    mockAuthService
  )

  def modelToFormData(accountingMethodModel: AccountingMethodModel): Seq[(String, String)] = {
    BusinessAccountingMethodForm.businessAccountingMethodForm.fill(accountingMethodModel).data.toSeq
  }

  "Show" should {

    "return ok (200)" when {
      "the connector returns data" in withController { controller =>
        mockAuthSuccess()
        mockGetSelfEmployments(businessAccountingMethodKey)(
          Right(Some(testAccountingMethodModel))
        )
        val result = controller.show(id = Some("testId"), isEditMode = false)(fakeRequest)
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
      }
      "the connector returns no data" in withController { controller =>
        mockAuthSuccess()
        mockGetSelfEmployments(businessAccountingMethodKey)(Right(None))
        val result = controller.show(id = Some("testId"), isEditMode = false)(fakeRequest)
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
      }
    }
    "Throw an internal exception" when {
      "there is an unexpected status failure" in withController { controller =>
        mockAuthSuccess()
        mockGetSelfEmployments(businessAccountingMethodKey)(Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))
        val response = intercept[InternalServerException](await(controller.show(id = Some("testId"), isEditMode = false)(fakeRequest)))
        response.message mustBe ("[BusinessAccountingMethodController][show] - Unexpected status: 500")
      }

      "there is an invalid Json" in withController { controller =>
        mockAuthSuccess()
        mockGetSelfEmployments(businessAccountingMethodKey)(Left(InvalidJson))
        val response = intercept[InternalServerException](await(controller.show(id = Some("testId"), isEditMode = false)(fakeRequest)))
        response.message mustBe ("[BusinessAccountingMethodController][show] - Invalid Json")
      }
    }

  }

  "Submit" should {

    "return 303, SEE_OTHER)" when {
      "the user submits valid data" when {
        "not in edit mode" when {
          "save and retrieve is enabled (an ID is provided)" should {
            "redirect to the self employed CYA page" in withController { controller =>
              enable(SaveAndRetrieve)
              mockAuthSuccess()
              mockSaveSelfEmployments(businessAccountingMethodKey, testAccountingMethodModel)(Right(PostSubscriptionDetailsSuccessResponse))
              val result = controller.submit(id = Some("testId"), isEditMode = false)(
                fakeRequest.withFormUrlEncodedBody(modelToFormData(testAccountingMethodModel): _*)
              )
              status(result) mustBe SEE_OTHER
              redirectLocation(result) mustBe
                Some(uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.SelfEmployedCYAController.show("testId").url)
            }
          }

          "save and retrieve is disabled" should {
            "redirect to sign up front end rerouting page" in withController { controller =>
              disable(SaveAndRetrieve)
              mockAuthSuccess()
              mockSaveSelfEmployments(businessAccountingMethodKey, testAccountingMethodModel)(Right(PostSubscriptionDetailsSuccessResponse))
              val result = controller.submit(id = None, isEditMode = false)(
                fakeRequest.withFormUrlEncodedBody(modelToFormData(testAccountingMethodModel): _*)
              )
              status(result) mustBe SEE_OTHER
              redirectLocation(result) mustBe
                Some("http://localhost:9561/report-quarterly/income-and-expenses/sign-up/business/routing")
            }
          }
        }

        "in edit mode" when {
          "save and retrieve is enabled (an ID is provided)" should {
            "redirect to the self employed CYA page" in withController { controller =>
              enable(SaveAndRetrieve)
              mockAuthSuccess()
              mockSaveSelfEmployments(businessAccountingMethodKey, testAccountingMethodModel)(Right(PostSubscriptionDetailsSuccessResponse))
              val result = controller.submit(id = Some("testId"), isEditMode = true)(
                fakeRequest.withFormUrlEncodedBody(modelToFormData(testAccountingMethodModel): _*)
              )
              status(result) mustBe SEE_OTHER
              redirectLocation(result) mustBe
                Some(uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.SelfEmployedCYAController.show("testId").url)
            }
          }

          "save and retrieve is disabled" should {
            "redirect to sign up front end check your answer page" in withController { controller =>
              disable(SaveAndRetrieve)
              mockAuthSuccess()
              mockSaveSelfEmployments(businessAccountingMethodKey, testAccountingMethodModel)(Right(PostSubscriptionDetailsSuccessResponse))
              val result = controller.submit(id = None, isEditMode = true)(
                fakeRequest.withFormUrlEncodedBody(modelToFormData(testAccountingMethodModel): _*)
              )
              status(result) mustBe SEE_OTHER
              redirectLocation(result) mustBe
                Some("http://localhost:9561/report-quarterly/income-and-expenses/sign-up/check-your-answers")
            }
          }
        }
      }
    }
    "return 400, SEE_OTHER)" when {
      "the user submits invalid data" in withController { controller =>
        mockAuthSuccess()
        mockSaveSelfEmployments(businessAccountingMethodKey, "invalid")(Right(PostSubscriptionDetailsSuccessResponse))
        val result = controller.submit(id = Some("testId"), isEditMode = false)(fakeRequest)
        status(result) mustBe BAD_REQUEST
        contentType(result) mustBe Some("text/html")
      }
    }
  }

  "The back url" when {
    "not in edit mode" when {
      "save and retrieve is disabled" should {
        "return a url for the business list CYA page" in withController { controller =>
          disable(SaveAndRetrieve)
          mockAuthSuccess()
          controller.backUrl(id = None, isEditMode = false) mustBe Some(routes.BusinessListCYAController.show.url)
        }
      }

      "save and retrieve is enabled" should {
        "return None when Save and Retrieve is enabled" in withController { controller =>
          enable(SaveAndRetrieve)
          mockAuthSuccess()
          controller.backUrl(id = None, isEditMode = false) mustBe None
        }
      }
    }

    "in edit mode" when {
      "save and retrieve is disabled" should {
        "return a url for the sign up front end final CYA page" in withController { controller =>
          disable(SaveAndRetrieve)
          mockAuthSuccess()
          controller.backUrl(id = None, isEditMode = true) mustBe Some(appConfig.subscriptionFrontendFinalCYAController)
        }
      }

      "save and retrieve is enabled" should {
        "return a url for the self employed CYA page" in withController { controller =>
          enable(SaveAndRetrieve)
          mockAuthSuccess()
          controller.backUrl(id = Some("testId"), isEditMode = true) mustBe
            Some(uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.SelfEmployedCYAController.show("testId").url)
        }
      }
    }
  }
  authorisationTests()

  private def withController(testCode: BusinessAccountingMethodController => Any): Unit = {
    val businessAccountingMethodView = mock[BusinessAccountingMethod]

    when(businessAccountingMethodView(any(), any(), any(), any())(any(), any(), any()))
      .thenReturn(HtmlFormat.empty)

    val controller = new BusinessAccountingMethodController(
      businessAccountingMethodView,
      mockMessagesControllerComponents,
      mockIncomeTaxSubscriptionConnector,
      mockAuthService
    )

    testCode(controller)
  }
}
