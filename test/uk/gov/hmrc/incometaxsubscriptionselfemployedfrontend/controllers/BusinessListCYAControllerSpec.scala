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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetAllSelfEmploymentsHttpParser._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.mocks.MockIncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ImplicitDateFormatterImpl
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.TestModels._

class BusinessListCYAControllerSpec extends ControllerBaseSpec with MockIncomeTaxSubscriptionConnector {

  implicit val mockImplicitDateFormatter: ImplicitDateFormatterImpl = new ImplicitDateFormatterImpl(mockLanguageUtils)
  override val controllerName: String = "BusinessListCYAController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map(
    "show" -> TestBusinessListCYAController.show()
  )

  object TestBusinessListCYAController extends BusinessListCYAController(
    mockAuthService,
    mockIncomeTaxSubscriptionConnector,
    mockMessagesControllerComponents
  )

  ".show" should {

    "return OK (200)" when {
      "the connector returns successful json" in {
        mockAuthSuccess()
        mockGetAllSelfEmployments()(Right(Some(GetAllSelfEmploymentDataModel(testGetAllSelfEmploymentModel))))

        val result = TestBusinessListCYAController.show()(FakeRequest())
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
      }

      "the connector returns an invalid Json" in {
        mockAuthSuccess()
        mockGetAllSelfEmployments()(Left(InvalidJson))

        val response = intercept[InternalServerException](await(TestBusinessListCYAController.show()(FakeRequest())))
        response.message mustBe ("[BusinessListCYAController][show] - Invalid Json")
      }
    }

    "return (303) redirect to business start date page" when {
      "the connector returns no data" in {
        mockAuthSuccess()
        mockGetAllSelfEmployments()(Right(None))

        val result = TestBusinessListCYAController.show()(FakeRequest())
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(routes.BusinessStartDateController.show().url)
      }
    }

    "throw an internal server error" when {
      "there is an unexpected status failure" in {
        mockAuthSuccess()
        mockGetAllSelfEmployments()(Left(GetAllSelfEmploymentConnectionFailure(INTERNAL_SERVER_ERROR)))

        val response = intercept[InternalServerException](await(TestBusinessListCYAController.show()(FakeRequest())))
        response.message mustBe "[BusinessListCYAController][show] - GetAllSelfEmploymentConnectionFailure status: 500"
      }
    }

  }

  "the back url" should {
    "return a url for the business trade name page" in {
      mockAuthSuccess()

      TestBusinessListCYAController.backUrl() mustBe routes.BusinessTradeNameController.show().url
    }
  }
  authorisationTests()

}
