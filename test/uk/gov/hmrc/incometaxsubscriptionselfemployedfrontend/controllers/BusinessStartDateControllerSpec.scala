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
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitch.SaveAndRetrieve
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.UnexpectedStatusFailure
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSelfEmploymentsSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessStartDateForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.mocks.MockMultipleSelfEmploymentsService
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.TestModels.testBusinessStartDateModel
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.DateOfCommencement

class BusinessStartDateControllerSpec extends ControllerBaseSpec
  with MockMultipleSelfEmploymentsService with FeatureSwitching {

  override def beforeEach(): Unit = {
    disable(SaveAndRetrieve)
    super.beforeEach()
  }

  val id: String = "testId"

  val businessStartDate = mock[DateOfCommencement]
  when(businessStartDate(any(), any(), any(), any(), any())(any(), any(), any()))
    .thenReturn(HtmlFormat.empty)

  override val controllerName: String = "BusinessStartDateController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map(
  "show" -> TestBusinessStartDateController.show(id, isEditMode = false),
  "submit" -> TestBusinessStartDateController.submit(id, isEditMode = false)
  )


  object TestBusinessStartDateController extends BusinessStartDateController(
    mockMessagesControllerComponents,
    mockMultipleSelfEmploymentsService,
    mockAuthService,
    mockLanguageUtils,
    businessStartDate
  )

  def modelToFormData(businessStartDateModel: BusinessStartDate): Seq[(String, String)] = {
    BusinessStartDateForm.businessStartDateForm("minStartDateError", "maxStartDateError").fill(businessStartDateModel).data.toSeq
  }

  def selfEmploymentData(id: String): SelfEmploymentData = SelfEmploymentData(
    id = id,
    businessStartDate = Some(BusinessStartDate(DateModel("8", "8", "2016"))),
    businessName = Some(BusinessNameModel("testBusinessName")),
    businessTradeName = Some(BusinessTradeNameModel("testTrade")),
    businessAddress = Some(BusinessAddressModel("12345", Address(Seq("line1"), "TF3 4NT")))
  )

  "Show" should {

    "return ok (200)" when {
      "the connector returns data" in {
        mockAuthSuccess()
        mockFetchBusinessStartDate(id)(
          Right(Some(BusinessStartDate(DateModel("01", "01", "2000"))))
        )
        val result = TestBusinessStartDateController.show(id, isEditMode = false)(FakeRequest())
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
      }
      "the connector returns no data" in {
        mockAuthSuccess()
        mockFetchBusinessStartDate(id)(Right(None))
        val result = TestBusinessStartDateController.show(id, isEditMode = false)(FakeRequest())
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
      }
    }
    "Throw an internal exception error" when {
      "the connector returns an error fetching the business start date" in {
        mockAuthSuccess()
        mockFetchBusinessStartDate(id)(Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))
        intercept[InternalServerException](await(TestBusinessStartDateController.show(id, isEditMode = false)(FakeRequest())))
      }
    }

  }

  "Submit" should {
    "when it is not in edit mode" should {
      "return 303, SEE_OTHER)" when {
        "the user submits valid data" in {
          mockAuthSuccess()
          mockSaveBusinessStartDate(id, testBusinessStartDateModel)(Right(PostSelfEmploymentsSuccessResponse))
          val result = TestBusinessStartDateController.submit(id, isEditMode = false)(
            FakeRequest().withFormUrlEncodedBody(modelToFormData(testBusinessStartDateModel): _*)
          )
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.BusinessNameController.show(id).url)
        }
      }
    }
    "when it is in edit mode" should {
      "return 303, SEE_OTHER)" when {
        "the user submits valid data" in {
          isEnabled(SaveAndRetrieve)
          mockAuthSuccess()
          mockSaveBusinessStartDate(id, testBusinessStartDateModel)(Right(PostSelfEmploymentsSuccessResponse))
          val result = TestBusinessStartDateController.submit(id, isEditMode = true)(
            FakeRequest().withFormUrlEncodedBody(modelToFormData(testBusinessStartDateModel): _*)
          )
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.BusinessListCYAController.show().url)
        }
      }
    }
    "when it is in SaveAndRetrieve mode" should {
      "return 303, SEE_OTHER)" when {
        "the user submits valid data" in {
          enable(SaveAndRetrieve)
          mockAuthSuccess()
          mockSaveBusinessStartDate(id, testBusinessStartDateModel)(Right(PostSelfEmploymentsSuccessResponse))
          val result = TestBusinessStartDateController.submit(id, isEditMode = false)(
            FakeRequest().withFormUrlEncodedBody(modelToFormData(testBusinessStartDateModel): _*)
          )
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.BusinessTradeNameController.show(id).url)
        }
      }
    }
    "return 400, SEE_OTHER)" when {
      "the user submits invalid data" in {
        mockAuthSuccess()
        mockFetchAllBusinesses(Right(Seq.empty[SelfEmploymentData]))
        mockSaveBusinessStartDate(id, testBusinessStartDateModel)(Right(PostSelfEmploymentsSuccessResponse))
        val result = TestBusinessStartDateController.submit(id, isEditMode = false)(FakeRequest())
        status(result) mustBe BAD_REQUEST
        contentType(result) mustBe Some("text/html")
      }
    }
  }

  "The back url" should {
    "go to the check your answers page" when {
      "in edit mode" in {
        TestBusinessStartDateController.backUrl(id, isEditMode = true) mustBe routes.BusinessListCYAController.show().url
      }
    }
    "go to the business name page" when{
        "in SaveAndRetrieve mode" in {
          enable(SaveAndRetrieve)
          TestBusinessStartDateController.backUrl(id, isEditMode = false) mustBe routes.BusinessNameController.show(id).url
        }
    }
    "go to the income source page" when {
      "not in edit mode" in {
        TestBusinessStartDateController.backUrl(id, isEditMode = false) must include("/report-quarterly/income-and-expenses/sign-up/details/income-receive")
      }
    }
  }

  authorisationTests()

}
