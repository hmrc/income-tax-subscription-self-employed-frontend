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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers

import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.UnexpectedStatusFailure
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSelfEmploymentsSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.mocks.MockIncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.BusinessTradeNameForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.BusinessTradeNameModel
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.TestModels._

class BusinessTradeNameControllerSpec extends ControllerBaseSpec
  with MockIncomeTaxSubscriptionConnector {

  override val controllerName: String = "BusinessTradeNameController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map(
    "show" -> TestBusinessTradeNameController$.show(isEditMode = false),
    "submit" -> TestBusinessTradeNameController$.submit(isEditMode = false)
  )

  object TestBusinessTradeNameController$ extends BusinessTradeNameController(
    mockMessagesControllerComponents,
    mockIncomeTaxSubscriptionConnector,
    mockAuthService
  )

  def modelToFormData(businessTradeNameModel: BusinessTradeNameModel): Seq[(String, String)] = {
    BusinessTradeNameForm.businessTradeNameValidationForm.fill(businessTradeNameModel).data.toSeq
  }

  "Show" should {

    "return ok (200)" when {
      "the connector returns data" in {
        mockAuthSuccess()
        mockGetSelfEmployments(BusinessTradeNameController.businessTradeNameKey)(
          Right(Some(testValidBusinessTradeNameModel))
        )
        val result = TestBusinessTradeNameController$.show(isEditMode = false)(FakeRequest())
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
      }
      "the connector returns no data" in {
        mockAuthSuccess()
        mockGetSelfEmployments(BusinessTradeNameController.businessTradeNameKey)(Right(None))
        val result = TestBusinessTradeNameController$.show(isEditMode = false)(FakeRequest())
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
      }
    }
    "Throw an internal exception error" when {
      "the connector returns an error" in {
        mockAuthSuccess()
        mockGetSelfEmployments(BusinessTradeNameController.businessTradeNameKey)(Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))
        intercept[InternalServerException](await(TestBusinessTradeNameController$.show(isEditMode = false)(FakeRequest())))
      }
    }

  }

  "Submit - it is not in edit mode" should {

    "return 303, SEE_OTHER)" when {
      "the user submits valid data" in {
        mockAuthSuccess()
        mockSaveSelfEmployments(BusinessTradeNameController.businessTradeNameKey, testValidBusinessTradeNameModel)(Right(PostSelfEmploymentsSuccessResponse))
        val result = TestBusinessTradeNameController$.submit(isEditMode = false)(
          FakeRequest().withFormUrlEncodedBody(modelToFormData(testValidBusinessTradeNameModel): _*)
        )
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.BusinessListCYAController.show().url)
      }
    }
    "return 400, SEE_OTHER)" when {
      "the user submits invalid data" in {
        mockAuthSuccess()
        mockSaveSelfEmployments(BusinessTradeNameController.businessTradeNameKey, testInvalidBusinessTradeNameModel)(Right(PostSelfEmploymentsSuccessResponse))
        val result = TestBusinessTradeNameController$.submit(isEditMode = false)(FakeRequest())
        status(result) mustBe BAD_REQUEST
        contentType(result) mustBe Some("text/html")
      }
    }
  }

  "Submit - it is in edit mode" should {

    s"return a redirect status (SEE_OTHER - 303) to '${uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.BusinessListCYAController.show().url}" when {
      "the user submits valid data" in {
        mockAuthSuccess()
        mockSaveSelfEmployments(BusinessTradeNameController.businessTradeNameKey, testValidBusinessTradeNameModel)(Right(PostSelfEmploymentsSuccessResponse))
        val result = TestBusinessTradeNameController$.submit(isEditMode = true)(
          FakeRequest().withFormUrlEncodedBody(modelToFormData(testValidBusinessTradeNameModel): _*)
        )
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.BusinessListCYAController.show().url)
      }
    }
  }

  "The back url" when {
    "in edit mode" should {
      s"redirect to ${routes.BusinessListCYAController.show().url}" in {
        TestBusinessTradeNameController$.backUrl(isEditMode = true) mustBe routes.BusinessListCYAController.show().url
      }
    }
    "not in edit mode" should {
      s"redirect to ${routes.BusinessNameController.show().url}" in {
        TestBusinessTradeNameController$.backUrl(isEditMode = false) mustBe routes.BusinessNameController.show().url
      }
    }
  }
  authorisationTests()

}
