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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent

import play.api.mvc.{Action, AnyContent}
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.addresslookup.mocks.MockAddressLookupConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSubscriptionDetailsSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup.GetAddressLookupDetailsHttpParser
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup.PostAddressLookupHttpParser.{PostAddressLookupSuccessResponse, UnexpectedStatusFailure}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.mocks.MockIncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.ControllerBaseSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.mocks.MockMultipleSelfEmploymentsService
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.TestModels._

class AddressLookupRoutingControllerSpec extends ControllerBaseSpec
  with MockAddressLookupConnector with MockMultipleSelfEmploymentsService with MockIncomeTaxSubscriptionConnector {
  val itsaId = "testId1"

  override val controllerName: String = "AddressLookupRoutingController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map(
    "initialiseAddressLookupJourney" -> TestAddressLookupRoutingController.initialiseAddressLookupJourney(itsaId),
    "addressLookupRedirect" -> TestAddressLookupRoutingController.addressLookupRedirect(itsaId, None)
  )

  object TestAddressLookupRoutingController extends AddressLookupRoutingController(
    mockMessagesControllerComponents,
    mockAuthService,
    mockAddressLookupConnector,
    mockIncomeTaxSubscriptionConnector,
    mockMultipleSelfEmploymentsService
  )

  val continueUrl = s"http://localhost:9563/report-quarterly/income-and-expenses/sign-up/self-employments/client/details/address-lookup/$itsaId"
  val redirectUrl = "http://testLocation?id=12345"

  "initialiseAddressLookupJourney" should {

    "return ok (200)" when {
      "the connector returns data" in {
        mockAuthSuccess()
        mockInitialiseAddressLookup(continueUrl, isAgent = true)(
          Right(PostAddressLookupSuccessResponse(Some(redirectUrl)))
        )
        mockSaveAddressRedirect(itsaId, redirectUrl)(Right(PostSubscriptionDetailsSuccessResponse))

        val result = TestAddressLookupRoutingController.initialiseAddressLookupJourney(itsaId)(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(redirectUrl)
      }
    }
    "Throw an internal exception" when {
      "there is an unexpected status failure" in {
        mockAuthSuccess()
        mockInitialiseAddressLookup(continueUrl, isAgent = true)(
          Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR))
        )

        val result = intercept[InternalServerException](await(TestAddressLookupRoutingController.initialiseAddressLookupJourney(itsaId)(fakeRequest)))
        result.message mustBe ("[AddressLookupRoutingController][initialiseAddressLookupJourney] - Unexpected response, status: 500")
      }
    }
  }

  "addressLookupRedirect" should {

    "return 303, SEE_OTHER)" when {
      "the address lookup service returns valid data" in {
        mockAuthSuccess()
        mockGetAddressDetails("12345")(Right(Some(testValidBusinessAddressModel)))
        mockSaveBusinessAddress(itsaId, testValidBusinessAddressModel)(Right(PostSubscriptionDetailsSuccessResponse))

        val result = TestAddressLookupRoutingController.addressLookupRedirect(itsaId, Some("12345"))(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe
          Some(uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.routes.BusinessListCYAController.show.url)
      }
    }

    "Throw an internal exception" when {
      "there is an unexpected status failure" in {
        mockAuthSuccess()
        mockGetAddressDetails("12345")(Left(GetAddressLookupDetailsHttpParser.UnexpectedStatusFailure(500)))

        val result = intercept[InternalServerException](await(TestAddressLookupRoutingController.addressLookupRedirect("12345", Some("12345"))(fakeRequest)))
        result.message mustBe ("[AddressLookupRoutingController][addressLookupRedirect] - Unexpected response, status: 500")
      }
      "there is an invalid Json" in {
        mockAuthSuccess()
        mockGetAddressDetails("12345")(Left(GetAddressLookupDetailsHttpParser.InvalidJson))
        val response = intercept[InternalServerException](await(TestAddressLookupRoutingController.addressLookupRedirect("12345", Some("12345"))(fakeRequest)))
        response.message mustBe ("[AddressLookupRoutingController][addressLookupRedirect] - Invalid json response")
      }
    }

    authorisationTests()
  }
}
