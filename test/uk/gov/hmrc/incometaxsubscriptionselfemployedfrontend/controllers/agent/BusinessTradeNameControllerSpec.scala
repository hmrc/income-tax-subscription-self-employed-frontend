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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.BusinessTradeNameForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.BusinessTradeNameModel
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.TestModels.{testInvalidBusinessTradeNameModel, testValidBusinessTradeNameModel}

class BusinessTradeNameControllerSpec extends ControllerBaseSpec
  with MockIncomeTaxSubscriptionConnector {

  override val controllerName: String = "BusinessTradeNameController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map(
    "show" -> TestBusinessTradeNameController.show(isEditMode = false),
    "submit" -> TestBusinessTradeNameController.submit(isEditMode = false)
  )

  object TestBusinessTradeNameController extends BusinessTradeNameController(
    mockMessagesControllerComponents,
    mockIncomeTaxSubscriptionConnector,
    mockAuthService
  )

  def modelToFormData(businessTradeNameModel: BusinessTradeNameModel): Seq[(String, String)] = {
    BusinessTradeNameForm.businessTradeNameValidationForm(Nil).fill(businessTradeNameModel).data.toSeq
  }

  "Show" should {

    "return ok (200)" when {
      "the connector returns data" in {
        mockAuthSuccess()
        mockGetSelfEmployments("BusinessTradeName")(
          Right(Some(testValidBusinessTradeNameModel))
        )
        val result = TestBusinessTradeNameController.show(isEditMode = false)(FakeRequest())
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
      }
      "the connector returns no data" in {
        mockAuthSuccess()
        mockGetSelfEmployments("BusinessTradeName")(Right(None))
        val result = TestBusinessTradeNameController.show(isEditMode = false)(FakeRequest())
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
      }
    }
    "Throw an internal exception error" when {
      "the connector returns an error" in {
        mockAuthSuccess()
        mockGetSelfEmployments("BusinessTradeName")(Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))
        intercept[InternalServerException](await(TestBusinessTradeNameController.show(isEditMode = false)(FakeRequest())))
      }
    }

  }

  "Submit when not in edit mode" should {

    "return 303, SEE_OTHER)" when {
      "the user submits valid data" in {
        mockAuthSuccess()
        mockSaveSelfEmployments("BusinessTradeName", testValidBusinessTradeNameModel)(Right(PostSelfEmploymentsSuccessResponse))
        val result = TestBusinessTradeNameController.submit(isEditMode = false)(
          FakeRequest().withFormUrlEncodedBody(modelToFormData(testValidBusinessTradeNameModel): _*)
        )
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe
          Some(
            uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.routes.AddressLookupRoutingController.initialiseAddressLookupJourney().url
          )
      }
    }
    "return 400, SEE_OTHER)" when {
      "the user submits invalid data" in {
        mockAuthSuccess()
        mockSaveSelfEmployments("BusinessTradeName", testInvalidBusinessTradeNameModel)(Right(PostSelfEmploymentsSuccessResponse))
        val result = TestBusinessTradeNameController.submit(isEditMode = false)(FakeRequest())
        status(result) mustBe BAD_REQUEST
        contentType(result) mustBe Some("text/html")
      }
    }
  }

  "Submit when in edit mode" should {

    "return 303, SEE_OTHER)" when {
      "the user submits valid data" in {
        mockAuthSuccess()
        mockSaveSelfEmployments("BusinessTradeName", testValidBusinessTradeNameModel)(Right(PostSelfEmploymentsSuccessResponse))
        val result = TestBusinessTradeNameController.submit(isEditMode = true)(
          FakeRequest().withFormUrlEncodedBody(modelToFormData(testValidBusinessTradeNameModel): _*)
        )
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe
          Some(uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.routes.BusinessTradeNameController.show().url)

      }
    }
    "return 400, SEE_OTHER)" when {
      "the user submits invalid data" in {
        mockAuthSuccess()
        mockSaveSelfEmployments("BusinessTradeName", testInvalidBusinessTradeNameModel)(Right(PostSelfEmploymentsSuccessResponse))
        val result = TestBusinessTradeNameController.submit(isEditMode = true)(FakeRequest())
        status(result) mustBe BAD_REQUEST
        contentType(result) mustBe Some("text/html")
      }
    }
  }
  authorisationTests()

}