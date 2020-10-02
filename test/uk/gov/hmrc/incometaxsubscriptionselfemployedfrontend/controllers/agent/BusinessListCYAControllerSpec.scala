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

import play.api.mvc.{Action, AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetAllSelfEmployedDetailsHttpParser._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.mocks.MockIncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.ControllerBaseSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ImplicitDateFormatterImpl

import scala.concurrent.Future

class BusinessListCYAControllerSpec extends ControllerBaseSpec with MockIncomeTaxSubscriptionConnector {

  implicit val mockImplicitDateFormatter: ImplicitDateFormatterImpl = new ImplicitDateFormatterImpl(mockLanguageUtils)

  val id: String = "testId"

  override val controllerName: String = "BusinessListCYAController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map(
    "show" -> TestBusinessListCYAController.show()
  )

  object TestBusinessListCYAController extends BusinessListCYAController(
    mockAuthService,
    mockIncomeTaxSubscriptionConnector,
    mockMessagesControllerComponents
  )

  val businessData: GetAllSelfEmploymentModel = GetAllSelfEmploymentModel(
    BusinessStartDate(DateModel("1", "1", "2017")),
    BusinessNameModel("ABC Limited"),
    BusinessTradeNameModel("Plumbing"),
    BusinessAddressModel("12345", Address(Seq("line1"), "TF3 4NT"))
  )

  "show" should {

    "return OK (200)" when {
      "the connector returns successful json" in {
        mockAuthSuccess()
        mockGetAllSelfEmployedDetails[GetAllSelfEmploymentModel](Right(Some(
          businessData
        )))

        val result: Future[Result] = TestBusinessListCYAController.show()(FakeRequest())
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
      }
    }

    "return (303) redirect to date of commencement page" when {
      "no businesses are returned" in {
        mockAuthSuccess()
        mockGetAllSelfEmployedDetails[GetAllSelfEmploymentModel](Right(None))

        val result: Future[Result] = TestBusinessListCYAController.show()(FakeRequest())
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(routes.DateOfCommencementController.show().url)
      }
    }

    "throw an internal server error" when {
      "there is an unexpected status failure" in {
        mockAuthSuccess()
        mockGetAllSelfEmployedDetails[GetAllSelfEmploymentModel](Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))

        val response = intercept[InternalServerException](await(TestBusinessListCYAController.show()(FakeRequest())))
        response.message mustBe "[BusinessListCYAController][show] - getAllSelfEmployedDetails connection failure, status: 500"
      }

      "the connector returns an invalid Json" in {
        mockAuthSuccess()
        mockGetAllSelfEmployedDetails[GetAllSelfEmploymentModel](Left(InvalidJson))

        val response = intercept[InternalServerException](await(TestBusinessListCYAController.show()(FakeRequest())))
        response.message mustBe ("[BusinessListCYAController][show] - Invalid Json")
      }
    }

  }

  "submit" should {
    "return 303, SEE_OTHER" when {
      "submit redirect to BusinessCYA controller" in {
        mockAuthSuccess()

        val result: Future[Result] = TestBusinessListCYAController.submit()(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.BusinessListCYAController.show().url)

      }
    }
  }

  authorisationTests()

}
