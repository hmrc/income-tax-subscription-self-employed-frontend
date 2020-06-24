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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.BusinessStartDateForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{BusinessStartDate, DateModel}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.TestModels.testBusinessStartDateModel

import scala.concurrent.Future

class BusinessStartDateControllerSpec extends ControllerBaseSpec
  with MockIncomeTaxSubscriptionConnector {

  override val controllerName: String = "BusinessStartDateController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map(
    "show" -> TestBusinessStartBusinessStartDateController$.show(),
    "submit" -> TestBusinessStartBusinessStartDateController$.submit()
  )

  object TestBusinessStartBusinessStartDateController$ extends BusinessStartDateController(
    mockMessagesControllerComponents,
    mockIncomeTaxSubscriptionConnector,
    mockAuthService, mockLanguageUtils
  )

  def modelToFormData(businessStartDateModel: BusinessStartDate): Seq[(String, String)] = {
    BusinessStartDateForm.businessStartDateForm("error").fill(businessStartDateModel).data.toSeq
  }

  "Show" should {

    "return ok (200)" when {
      "the connector returns data" in {
        mockAuthSuccess()
        mockGetSelfEmployments(BusinessStartDateController.businessStartDateKey)(
          Right(Some(BusinessStartDate(DateModel("01", "01", "2000"))))
        )
        val result = TestBusinessStartBusinessStartDateController$.show()(FakeRequest())
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
      }
      "the connector returns no data" in {
        mockAuthSuccess()
        mockGetSelfEmployments(BusinessStartDateController.businessStartDateKey)(Right(None))
        val result = TestBusinessStartBusinessStartDateController$.show()(FakeRequest())
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
      }
    }
    "Throw an internal exception error" when {
      "the connector returns an error" in {
        mockAuthSuccess()
        mockGetSelfEmployments(BusinessStartDateController.businessStartDateKey)(Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))
        intercept[InternalServerException](await(TestBusinessStartBusinessStartDateController$.show()(FakeRequest())))
      }
    }

  }

  "Submit" should {

    "return 303, SEE_OTHER)" when {
      "the user submits valid data" in {
        mockAuthSuccess()
        mockSaveSelfEmployments(BusinessStartDateController.businessStartDateKey, testBusinessStartDateModel)(Right(PostSelfEmploymentsSuccessResponse))
        val result = TestBusinessStartBusinessStartDateController$.submit()(
          FakeRequest().withFormUrlEncodedBody(modelToFormData(testBusinessStartDateModel): _*)
        )
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.BusinessNameController.show().url)
      }
    }
    "return 400, SEE_OTHER)" when {
      "the user submits invalid data" in {
        mockAuthSuccess()
        mockSaveSelfEmployments(BusinessStartDateController.businessStartDateKey, testBusinessStartDateModel)(Right(PostSelfEmploymentsSuccessResponse))
        val result = TestBusinessStartBusinessStartDateController$.submit()(FakeRequest())
        status(result) mustBe BAD_REQUEST
        contentType(result) mustBe Some("text/html")
      }
    }
  }
  authorisationTests()


}
