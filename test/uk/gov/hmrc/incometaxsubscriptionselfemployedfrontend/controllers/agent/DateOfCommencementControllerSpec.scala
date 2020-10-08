/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.UnexpectedStatusFailure
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSelfEmploymentsSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.mocks.MockIncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.ControllerBaseSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.DateOfCommencementForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.TestModels._

class DateOfCommencementControllerSpec extends ControllerBaseSpec
with MockIncomeTaxSubscriptionConnector {
  val id: String = "testId"

  override val controllerName: String = "DateOfCommencementController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map(
    "show" -> TestDateOfCommencementController.show(id,isEditMode = false),
    "submit" -> TestDateOfCommencementController.submit(id,isEditMode = false)
  )

  object TestDateOfCommencementController extends DateOfCommencementController(
    mockMessagesControllerComponents,
    mockIncomeTaxSubscriptionConnector,
    mockAuthService, mockLanguageUtils
  )

  def modelToFormData(businessStartDateModel: BusinessStartDate): Seq[(String, String)] = {
    DateOfCommencementForm.dateOfCommencementForm("error").fill(businessStartDateModel).data.toSeq
  }

  "Show" should {
    "return ok (200)" when {
      "the connector returns data" in {

        mockAuthSuccess()
        mockGetSelfEmployments("BusinessStartDate")(
          Right(Some(BusinessStartDate(DateModel("01", "01", "2000"))))
        )
        val result = TestDateOfCommencementController.show(id,isEditMode = false)(FakeRequest())
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
      }
      "the connector returns no data" in {
        mockAuthSuccess()
        mockGetSelfEmployments("BusinessStartDate")(Right(None))
        val result = TestDateOfCommencementController.show(id,isEditMode = false)(FakeRequest())
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
      }
    }
    "Throw an internal exception error" when {
      "the connector returns an error" in {
        mockAuthSuccess()
        mockGetSelfEmployments("BusinessStartDate")(Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))
        intercept[InternalServerException](await(TestDateOfCommencementController.show(id,isEditMode = false)(FakeRequest())))
      }
    }

  }

  "Submit" should {
    "when it is not in edit mode" should {
      "return 303, SEE_OTHER)" when {
        "the user submits valid data" in {
          mockAuthSuccess()
          mockSaveSelfEmployments("BusinessStartDate", testBusinessStartDateModel)(Right(PostSelfEmploymentsSuccessResponse))
          val result = TestDateOfCommencementController.submit(id,isEditMode = false)(
            FakeRequest().withFormUrlEncodedBody(modelToFormData(testBusinessStartDateModel): _*)
          )
          status(result) mustBe SEE_OTHER
          Some(uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.routes.DateOfCommencementController.show(id).url)
        }
      }
      "return 400, SEE_OTHER)" when {
        "the user submits invalid data" in {
          mockAuthSuccess()
          mockSaveSelfEmployments("BusinessStartDate", testBusinessStartDateModel)(Right(PostSelfEmploymentsSuccessResponse))
          val result = TestDateOfCommencementController.submit(id,isEditMode = false)(FakeRequest())
          status(result) mustBe BAD_REQUEST
          contentType(result) mustBe Some("text/html")
        }
      }
    }
    "when it is in edit mode" should {
      "return 303, SEE_OTHER)" when {
        "the user submits valid data" in {
          mockAuthSuccess()
          mockSaveSelfEmployments("BusinessStartDate", testBusinessStartDateModel)(Right(PostSelfEmploymentsSuccessResponse))
          val result = TestDateOfCommencementController.submit(id,isEditMode = true)(
            FakeRequest().withFormUrlEncodedBody(modelToFormData(testBusinessStartDateModel): _*)
          )
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe
            Some(uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.routes.BusinessListCYAController.show(id).url)
        }
      }
      "return 400, SEE_OTHER)" when {
        "the user submits invalid data" in {
          mockAuthSuccess()
          mockSaveSelfEmployments("BusinessStartDate", testBusinessStartDateModel)(Right(PostSelfEmploymentsSuccessResponse))
          val result = TestDateOfCommencementController.submit(id,isEditMode = true)(FakeRequest())
          status(result) mustBe BAD_REQUEST
          contentType(result) mustBe Some("text/html")
        }
      }
    }
  }
}
