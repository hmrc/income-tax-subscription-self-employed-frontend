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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitch.EnableTaskListRedesign
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.UnexpectedStatusFailure
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.ControllerBaseSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessNameForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.mocks.{MockMultipleSelfEmploymentsService, MockSessionDataService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ITSASessionKeys
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.TestModels._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.BusinessName

class BusinessNameControllerSpec extends ControllerBaseSpec
  with MockMultipleSelfEmploymentsService with FeatureSwitching with MockSessionDataService {

  val id: String = "testId"

  override val controllerName: String = "BusinessNameController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map()

  private val businessName = mock[BusinessName]
  when(businessName(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)

  object TestBusinessNameController extends BusinessNameController(
    mockMessagesControllerComponents,
    businessName,
    mockMultipleSelfEmploymentsService,
    mockAuthService
  )(
    mockSessionDataService,
    appConfig
  )

  override def beforeEach(): Unit = {
    disable(featureSwitch = EnableTaskListRedesign)
    super.beforeEach()
  }

  def modelToFormData(businessNameModel: String): Seq[(String, String)] = {
    BusinessNameForm.businessNameValidationForm(Nil).fill(businessNameModel).data.toSeq
  }

  "show" should {
    "return ok (200)" when {
      "the connector returns data for the current business" in {
        mockAuthSuccess()
        mockFetchAllNameTradeCombos(Right(Seq(
          (id, Some(testBusinessNameModel), None)
        )))

        val result = TestBusinessNameController.show(id, isEditMode = false)(fakeRequest)

        status(result) mustBe OK
        contentType(result) mustBe Some(HTML)
      }
      "the connector returns data for the current business but with no business name" in {
        mockAuthSuccess()
        mockFetchAllNameTradeCombos(Right(Seq(
          (id, None, None)
        )))

        val result = TestBusinessNameController.show(id, isEditMode = false)(fakeRequest)

        status(result) mustBe OK
        contentType(result) mustBe Some(HTML)
      }
    }
    "Throw an internal exception error" when {
      "the connector returns an error" in {
        mockAuthSuccess()
        mockFetchAllNameTradeCombos(
          Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR))
        )
        intercept[InternalServerException](await(TestBusinessNameController.show(id, isEditMode = false)(fakeRequest)))
      }

    }
  }

  "submit" when {
    "not in edit mode" should {
      s"return $SEE_OTHER and redirect to Business Start Date page" in {
        mockAuthSuccess()
        mockFetchAllNameTradeCombos(Right(Seq(
          (id, None, None)
        )))
        mockSaveBusinessName(id, mockBusinessNameModel)(Right(PostSubscriptionDetailsSuccessResponse))

        val result = TestBusinessNameController.submit(id, isEditMode = false)(
          fakeRequest.withFormUrlEncodedBody(modelToFormData(mockBusinessNameModel): _*)
        )

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.BusinessStartDateController.show(id).url)
      }

      s"return $BAD_REQUEST" when {
        "the user submits invalid data" in {
          mockAuthSuccess()
          mockFetchAllNameTradeCombos(Right(Seq(
            (id, None, None)
          )))
          val result = TestBusinessNameController.submit(id, isEditMode = false)(fakeRequest)
          status(result) mustBe BAD_REQUEST
          contentType(result) mustBe Some(HTML)
        }
        "the user enters a business name which would cause a duplicate business name / trade combination" in {
          mockAuthSuccess()
          mockFetchAllNameTradeCombos(Right(Seq(
            ("idOne", Some("nameOne"), Some("tradeOne")),
            ("idTwo", None, Some("tradeOne"))
          )))

          val result = TestBusinessNameController.submit("idTwo", isEditMode = false)(
            fakeRequest.withFormUrlEncodedBody(modelToFormData("nameOne"): _*)
          )

          status(result) mustBe BAD_REQUEST
          contentType(result) mustBe Some(HTML)
        }
      }
      "throw an exception" when {
        "an error is returned when retrieving all businesses" in {
          mockAuthSuccess()
          mockFetchAllNameTradeCombos(
            Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR))
          )

          intercept[InternalServerException](await(TestBusinessNameController.submit(id, isEditMode = false)(fakeRequest)))
        }
      }
    }
    "in edit mode" should {
      s"return $SEE_OTHER" when {
        "the users answer is updated correctly" in {
          mockAuthSuccess()
          mockFetchAllNameTradeCombos(Right(Seq(
            (id, Some("nameOne"), None)
          )))
          mockSaveBusinessName(id, mockBusinessNameModel)(Right(PostSubscriptionDetailsSuccessResponse))

          val result = TestBusinessNameController.submit(id, isEditMode = true)(
            fakeRequest.withFormUrlEncodedBody(modelToFormData(mockBusinessNameModel): _*)
          )

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.SelfEmployedCYAController.show(id, isEditMode = true).url)
        }

        "the user does not update their answer" in {
          mockAuthSuccess()
          mockFetchAllNameTradeCombos(Right(Seq(
            (id, Some(mockBusinessNameModel), None)
          )))
          mockSaveBusinessName(id, mockBusinessNameModel)(Right(PostSubscriptionDetailsSuccessResponse))

          val result = TestBusinessNameController.submit(id, isEditMode = true)(
            fakeRequest.withFormUrlEncodedBody(modelToFormData(mockBusinessNameModel): _*)
          )

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.SelfEmployedCYAController.show(id, isEditMode = true).url)
        }
      }
    }
  }

  "The back url" when {
    "in edit mode" should {
      s"redirect to Self-Employment check your answer page" in {
        TestBusinessNameController.backUrl(id, isEditMode = true)(fakeRequest) mustBe routes.SelfEmployedCYAController.show(id, isEditMode = true).url
      }
    }
    "not in edit mode with feature switch is enabled" should {
      "redirect to business name confirmation page if user name exists" in {
        enable(featureSwitch = EnableTaskListRedesign)
        TestBusinessNameController.backUrl(id, isEditMode = false)(fakeRequest.withSession(ITSASessionKeys.FullNameSessionKey -> "Selena Kyle")) contains appConfig.incomeTaxSubscriptionFrontendBaseUrl + "/details/confirm-business-name"
      }
      "redirect to new income source page if user name doesn't exists feature is enabled" in {
        enable(featureSwitch = EnableTaskListRedesign)
        TestBusinessNameController.backUrl(id, isEditMode = false)(fakeRequest) mustBe appConfig.yourIncomeSourcesUrl
      }
    }
    "not in edit mode with feature switch disabled" in {
      TestBusinessNameController.backUrl(id, isEditMode = false)(fakeRequest) mustBe appConfig.whatIncomeSourceToSignUpUrl
    }
  }
  authorisationTests()

}
