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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.UnexpectedStatusFailure
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSubscriptionDetailsSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.ControllerBaseSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessStartDateForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.mocks.{MockMultipleSelfEmploymentsService, MockSessionDataService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.TestModels.testValidStartDateAfterLimit
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.{BusinessStartDate => BusinessStartDateView}

class BusinessStartDateControllerSpec extends ControllerBaseSpec
  with MockMultipleSelfEmploymentsService with MockSessionDataService {

  val id: String = "testId"

  private val businessStartDate = mock[BusinessStartDateView]
  when(businessStartDate(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)

  override val controllerName: String = "BusinessStartDateController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map(
    "show" -> TestBusinessStartDateController.show(id, isEditMode = false, isGlobalEdit = false),
    "submit" -> TestBusinessStartDateController.submit(id, isEditMode = false, isGlobalEdit = false)
  )

  object TestBusinessStartDateController extends BusinessStartDateController(
    mockMessagesControllerComponents,
    mockMultipleSelfEmploymentsService,
    mockAuthService,
    businessStartDate
  )(
    mockSessionDataService,
    mockLanguageUtils,
    appConfig
  )

  def modelToFormData(businessStartDateModel: DateModel): Seq[(String, String)] = {
    BusinessStartDateForm.businessStartDateForm(
      BusinessStartDateForm.maxStartDate,
      _.toString
    ).fill(businessStartDateModel).data.toSeq
  }

  "Show" should {

    "return ok (200)" when {
      "the connector returns data" in {
        mockAuthSuccess()
        mockFetchBusinessStartDate(id)(
          Right(Some(DateModel("01", "01", "2000")))
        )

        val result = TestBusinessStartDateController.show(id, isEditMode = false, isGlobalEdit = false)(fakeRequest)

        status(result) mustBe OK
        contentType(result) mustBe Some(HTML)
      }
      "the connector returns no data" in {
        mockAuthSuccess()
        mockFetchBusinessStartDate(id)(Right(None))

        val result = TestBusinessStartDateController.show(id, isEditMode = false, isGlobalEdit = false)(fakeRequest)

        status(result) mustBe OK
        contentType(result) mustBe Some(HTML)
      }
    }
    "Throw an internal exception error" when {
      "the connector returns an error fetching the business start date" in {
        mockAuthSuccess()
        mockFetchBusinessStartDate(id)(Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))
        intercept[InternalServerException](await(TestBusinessStartDateController.show(id, isEditMode = false, isGlobalEdit = false)(fakeRequest)))
      }
    }

  }

  "Submit" when {
    "not in edit mode and user submits valid data" when {
      "return 303, SEE_OTHER and redirect to address lookup page" in {
        mockAuthSuccess()
        mockSaveBusinessStartDate(id, testValidStartDateAfterLimit)(Right(PostSubscriptionDetailsSuccessResponse))

        val result = TestBusinessStartDateController.submit(id, isEditMode = false, isGlobalEdit = false)(
          fakeRequest.withFormUrlEncodedBody(modelToFormData(testValidStartDateAfterLimit): _*)
        )

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.AddressLookupRoutingController.checkAddressLookupJourney(id).url)
      }
    }
    "in edit mode and user submits valid data" should {
      "return 303, SEE_OTHER and redirect to Self-employment Check Your Answer page" in {
        mockAuthSuccess()
        mockSaveBusinessStartDate(id, testValidStartDateAfterLimit)(Right(PostSubscriptionDetailsSuccessResponse))

        val result = TestBusinessStartDateController.submit(id, isEditMode = true, isGlobalEdit = false)(
          fakeRequest.withFormUrlEncodedBody(modelToFormData(testValidStartDateAfterLimit): _*)
        )

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.SelfEmployedCYAController.show(id, isEditMode = true).url)

      }
    }

    "return 400, SEE_OTHER" when {
      "the user submits invalid data" in {
        mockAuthSuccess()
        mockSaveBusinessStartDate(id, testValidStartDateAfterLimit)(Right(PostSubscriptionDetailsSuccessResponse))

        val result = TestBusinessStartDateController.submit(id, isEditMode = false, isGlobalEdit = false)(fakeRequest)

        status(result) mustBe BAD_REQUEST
        contentType(result) mustBe Some(HTML)
      }
    }
  }

  "The back url" when {
    "redirect to full income source page" when {
      "in global edit mode" in {
        TestBusinessStartDateController.backUrl(id, isEditMode = true, isGlobalEdit = true) mustBe routes.FullIncomeSourceController.show(id, isEditMode = true, isGlobalEdit = true).url
      }
      "in edit mode" in {
        TestBusinessStartDateController.backUrl(id, isEditMode = true, isGlobalEdit = false) mustBe routes.FullIncomeSourceController.show(id, isEditMode = true).url
      }
      "not in edit mode" in {
        TestBusinessStartDateController.backUrl(id, isEditMode = false, isGlobalEdit = false) mustBe routes.FullIncomeSourceController.show(id).url
      }
    }
  }

  authorisationTests()

}
