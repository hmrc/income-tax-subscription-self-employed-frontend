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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent

import play.api.mvc.{Action, AnyContent}
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitch.StartDateBeforeLimit
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.addresslookup.mocks.MockAddressLookupConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSubscriptionDetailsSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup.PostAddressLookupHttpParser.{PostAddressLookupSuccessResponse, UnexpectedStatusFailure}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.ControllerBaseSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.Address
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.mocks.{MockMultipleSelfEmploymentsService, MockSessionDataService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.TestModels._

class AddressLookupRoutingControllerSpec extends ControllerBaseSpec
  with MockAddressLookupConnector
  with MockSessionDataService
  with MockMultipleSelfEmploymentsService
  with FeatureSwitching {

  override def beforeEach(): Unit = {
    disable(StartDateBeforeLimit)
    super.beforeEach()
  }

  val isAgent = true

  val businessId = "testId1"

  override val controllerName: String = "AddressLookupRoutingController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map(
    "initialiseAddressLookupJourney" -> TestAddressLookupRoutingController.initialiseAddressLookupJourney(businessId, isEditMode = false, isGlobalEdit = false),
    "addressLookupRedirect" -> TestAddressLookupRoutingController.addressLookupRedirect(businessId, None, isEditMode = false, isGlobalEdit = false)
  )

  object TestAddressLookupRoutingController extends AddressLookupRoutingController(
    mockMessagesControllerComponents,
    mockAuthService,
    mockAddressLookupConnector,
    mockMultipleSelfEmploymentsService
  )(
    mockSessionDataService,
    appConfig
  )

  val continueUrl = s"http://localhost:9563/report-quarterly/income-and-expenses/sign-up/self-employments/client/details/address-lookup/$businessId"
  private val addressId = "12345"
  private val redirectUrl = "http://testLocation?id=" + addressId

  val testAddress: Address = Address(
    lines = Seq("1 Long Road", "Lonely Town"), postcode = Some("ZZ1 1ZZ")
  )

  "checkAddressLookupJourney" when {
    "the user has no already added business addresses" should {
      "redirect to the address lookup initialise route" when {
        "edit mode is false" in {
          mockAuthSuccess()
          mockFetchFirstAddress(Right(None))

          val result = TestAddressLookupRoutingController.checkAddressLookupJourney(businessId, isEditMode = false)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.AddressLookupRoutingController.initialiseAddressLookupJourney(businessId).url)
        }
        "edit mode is true" in {
          mockAuthSuccess()
          mockFetchFirstAddress(Right(None))

          val result = TestAddressLookupRoutingController.checkAddressLookupJourney(businessId, isEditMode = true)(fakeRequest)

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.AddressLookupRoutingController.initialiseAddressLookupJourney(businessId, isEditMode = true).url)
        }
      }
    }
    "the user has an already added business address" should {
      "redirect to the business address confirmation page" in {
        mockAuthSuccess()
        mockFetchFirstAddress(Right(Some(testAddress)))

        val result = TestAddressLookupRoutingController.checkAddressLookupJourney(businessId, isEditMode = false)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.BusinessAddressConfirmationController.show(businessId).url)
      }
    }

    "there was an error returned when fetching the first added address" should {
      "throw an internal server exception" in {
        mockFetchFirstAddress(Left(GetSelfEmploymentsHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))

        val result = TestAddressLookupRoutingController.checkAddressLookupJourney(businessId, isEditMode = false)(fakeRequest)

        intercept[InternalServerException](await(result))
          .message mustBe "[AddressLookupRoutingController][checkAddressLookupJourney] - Error when retrieving any address"
      }
    }
  }

  "initialiseAddressLookupJourney" should {

    "return ok (200)" when {
      "the connector returns data" in {
        mockAuthSuccess()
        mockInitialiseAddressLookup(continueUrl, isAgent = isAgent)(
          Right(PostAddressLookupSuccessResponse(Some(redirectUrl)))
        )

        val result = TestAddressLookupRoutingController.initialiseAddressLookupJourney(businessId, isEditMode = false, isGlobalEdit = false)(fakeRequest)

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(redirectUrl)
      }
    }
    "Throw an internal exception" when {
      "there is an unexpected status failure" in {
        mockAuthSuccess()
        mockInitialiseAddressLookup(continueUrl, isAgent = isAgent)(
          Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR))
        )

        val result = intercept[InternalServerException](
          await(TestAddressLookupRoutingController.initialiseAddressLookupJourney(businessId, isEditMode = false, isGlobalEdit = false)(fakeRequest))
        )
        result.message mustBe "[AddressLookupRoutingController][initialiseAddressLookupJourney] - Unexpected response, status: 500"
      }
    }
  }

  "addressLookupRedirect" when {

    "is in edit mode" should {
      "redirect to sole trader check your answer page" when {
        "the address lookup service returns valid data" in {
          mockAuthSuccess()
          mockFetchAccountingMethod(Right(Some(testAccountingMethodModel)))
          mockGetAddressDetails(addressId)(Right(Some(testValidBusinessAddressModel)))
          mockSaveBusinessAddress(businessId, testValidBusinessAddressModel)(Right(PostSubscriptionDetailsSuccessResponse))

          val result = TestAddressLookupRoutingController.addressLookupRedirect(businessId, Some(addressId), isEditMode = true, isGlobalEdit = false)(fakeRequest)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe
            Some(routes.SelfEmployedCYAController.show(businessId, isEditMode = true).url)
        }
      }
    }

    "is in global edit mode" should {
      "redirect to sole trader check your answer page" when {
        "the address lookup service returns valid data" in {
          mockAuthSuccess()
          mockFetchAccountingMethod(Right(Some(testAccountingMethodModel)))
          mockGetAddressDetails(addressId)(Right(Some(testValidBusinessAddressModel)))
          mockSaveBusinessAddress(businessId, testValidBusinessAddressModel)(Right(PostSubscriptionDetailsSuccessResponse))

          val result = TestAddressLookupRoutingController.addressLookupRedirect(businessId, Some(addressId), isEditMode = true, isGlobalEdit = true)(fakeRequest)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe
            Some(routes.SelfEmployedCYAController.show(businessId, isEditMode = true, isGlobalEdit = true).url)
        }
      }
    }

    "is not in edit mode" when {
      "accounting method is defined" should {
        "redirect to sole trader check your answers page" when {
          "the address lookup service returns valid data" in {
            mockAuthSuccess()
            mockFetchAccountingMethod(Right(Some(testAccountingMethodModel)))
            mockGetAddressDetails(addressId)(Right(Some(testValidBusinessAddressModel)))
            mockSaveBusinessAddress(businessId, testValidBusinessAddressModel)(Right(PostSubscriptionDetailsSuccessResponse))

            val result = TestAddressLookupRoutingController.addressLookupRedirect(businessId, Some(addressId), isEditMode = false, isGlobalEdit = false)(fakeRequest)
            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe
              Some(routes.SelfEmployedCYAController.show(businessId).url)
          }
        }
      }

      "accounting method is not defined" should {
        "redirect to sole trader accounting method page" when {
          "the address lookup service returns valid data" in {
            mockAuthSuccess()
            mockFetchAccountingMethod(Right(None))
            mockGetAddressDetails(addressId)(Right(Some(testValidBusinessAddressModel)))
            mockSaveBusinessAddress(businessId, testValidBusinessAddressModel)(Right(PostSubscriptionDetailsSuccessResponse))

            val result = TestAddressLookupRoutingController.addressLookupRedirect(businessId, Some(addressId), isEditMode = false, isGlobalEdit = false)(fakeRequest)
            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe
              Some(routes.SelfEmployedCYAController.show(businessId).url)
          }
        }
      }

    }

    "the start date before limit feature switch is enabled" should {
      "redirect to sole trader check your answer page" when {
        "the address lookup service returns valid data" in {
          enable(StartDateBeforeLimit)
          mockAuthSuccess()
          mockFetchAccountingMethod(Right(None))
          mockGetAddressDetails(addressId)(Right(Some(testValidBusinessAddressModel)))
          mockSaveBusinessAddress(businessId, testValidBusinessAddressModel)(Right(PostSubscriptionDetailsSuccessResponse))

          val result = TestAddressLookupRoutingController.addressLookupRedirect(businessId, Some(addressId), isEditMode = false, isGlobalEdit = false)(fakeRequest)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe
            Some(routes.SelfEmployedCYAController.show(businessId).url)
        }
      }
    }

    authorisationTests()
  }
}
