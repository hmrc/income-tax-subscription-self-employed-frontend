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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.mvc.{Action, AnyContent}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.UnexpectedStatusFailure
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSubscriptionDetailsSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.mocks.MockIncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessStartDateForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.mocks.MockMultipleSelfEmploymentsService
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.TestModels.testBusinessStartDateModel
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.{BusinessStartDate => BusinessStartDateView}

class BusinessStartDateControllerSpec extends ControllerBaseSpec
  with MockMultipleSelfEmploymentsService with FeatureSwitching with MockIncomeTaxSubscriptionConnector {

  val id: String = "testId"

  private val businessStartDate = mock[BusinessStartDateView]
  when(businessStartDate(any(), any(), any(), any())(any(), any())).thenReturn(HtmlFormat.empty)

  override val controllerName: String = "BusinessStartDateController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map(
    "show" -> TestBusinessStartDateController.show(id, isEditMode = false),
    "submit" -> TestBusinessStartDateController.submit(id, isEditMode = false)
  )


  object TestBusinessStartDateController extends BusinessStartDateController(
    mockMessagesControllerComponents,
    mockMultipleSelfEmploymentsService,
    mockIncomeTaxSubscriptionConnector,
    mockAuthService,
    mockLanguageUtils,
    businessStartDate
  )

  def modelToFormData(businessStartDateModel: BusinessStartDate): Seq[(String, String)] = {
    BusinessStartDateForm.businessStartDateForm(BusinessStartDateForm.minStartDate, BusinessStartDateForm.maxStartDate, d => d.toString).fill(businessStartDateModel).data.toSeq
  }

  def selfEmploymentData(id: String): SelfEmploymentData = SelfEmploymentData(
    id = id,
    businessStartDate = Some(BusinessStartDate(DateModel("8", "8", "2016"))),
    businessName = Some(BusinessNameModel("testBusinessName")),
    businessTradeName = Some(BusinessTradeNameModel("testTrade")),
    businessAddress = Some(BusinessAddressModel(Address(Seq("line1"), Some("TF3 4NT"))))
  )

  "Show" should {

    "return ok (200)" when {
      "the connector returns data" in {
        mockAuthSuccess()
        mockFetchBusinessStartDate(id)(
          Right(Some(BusinessStartDate(DateModel("01", "01", "2000"))))
        )
        val result = TestBusinessStartDateController.show(id, isEditMode = false)(fakeRequest)
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
      }
      "the connector returns no data" in {
        mockAuthSuccess()
        mockFetchBusinessStartDate(id)(Right(None))
        val result = TestBusinessStartDateController.show(id, isEditMode = false)(fakeRequest)
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
      }
    }
    "Throw an internal exception error" when {
      "the connector returns an error fetching the business start date" in {
        mockAuthSuccess()
        mockFetchBusinessStartDate(id)(Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))
        intercept[InternalServerException](await(TestBusinessStartDateController.show(id, isEditMode = false)(fakeRequest)))
      }
    }

  }

  "Submit" should {
    "when it is not in edit mode" when {
      "return 303, SEE_OTHER and redirect to Business Trade Name page" when {
        "the user submits valid data" in {
          mockAuthSuccess()
          mockSaveBusinessStartDate(id, testBusinessStartDateModel)(Right(PostSubscriptionDetailsSuccessResponse))
          val result = TestBusinessStartDateController.submit(id, isEditMode = false)(
            fakeRequest.withFormUrlEncodedBody(modelToFormData(testBusinessStartDateModel): _*)
          )
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.BusinessTradeNameController.show(id).url)
        }
      }
    }
    "when it is in edit mode" when {
      "return 303, SEE_OTHER and redirect to Self-employment Check Your Answer page" when {
        "the user submits valid data" in {
          mockAuthSuccess()
          mockSaveBusinessStartDate(id, testBusinessStartDateModel)(Right(PostSubscriptionDetailsSuccessResponse))
          val result = TestBusinessStartDateController.submit(id, isEditMode = true)(
            fakeRequest.withFormUrlEncodedBody(modelToFormData(testBusinessStartDateModel): _*)
          )
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.SelfEmployedCYAController.show(id, isEditMode = true).url)
        }
      }
    }
    "return 303, SEE_OTHER" when {
      "the user submits valid data" in {
        mockAuthSuccess()
        mockSaveBusinessStartDate(id, testBusinessStartDateModel)(Right(PostSubscriptionDetailsSuccessResponse))
        val result = TestBusinessStartDateController.submit(id, isEditMode = false)(
          fakeRequest.withFormUrlEncodedBody(modelToFormData(testBusinessStartDateModel): _*)
        )
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.BusinessTradeNameController.show(id).url)
      }
    }
    "return 400, SEE_OTHER" when {
      "the user submits invalid data" in {
        mockAuthSuccess()
        mockFetchAllBusinesses(Right(Seq.empty[SelfEmploymentData]))
        mockSaveBusinessStartDate(id, testBusinessStartDateModel)(Right(PostSubscriptionDetailsSuccessResponse))
        val result = TestBusinessStartDateController.submit(id, isEditMode = false)(fakeRequest)
        status(result) mustBe BAD_REQUEST
        contentType(result) mustBe Some("text/html")
      }
    }
  }

  "The back url" when {
    "in edit mode" should {
      s"redirect to Self-Employment check your answer page" in {
        TestBusinessStartDateController.backUrl(id, isEditMode = true) mustBe routes.SelfEmployedCYAController.show(id, isEditMode = true).url
      }
    }
    "not in edit mode" should {
      "redirect to business name page" in {
        TestBusinessStartDateController.backUrl(id, isEditMode = false) mustBe routes.BusinessNameController.show(id).url
      }
    }
  }

  authorisationTests()

}
