/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.businessAccountingMethodKey
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.addresslookup.mocks.MockAddressLookupConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSubscriptionDetailsSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup.PostAddressLookupHttpParser.{PostAddressLookupSuccessResponse, UnexpectedStatusFailure}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.mocks.MockIncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.mocks.MockMultipleSelfEmploymentsService
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.TestModels._

class AddressLookupRoutingControllerSpec extends ControllerBaseSpec
  with MockAddressLookupConnector
  with MockIncomeTaxSubscriptionConnector
  with MockMultipleSelfEmploymentsService
  with FeatureSwitching {

  val isAgent = false

  val businessId = "testId1"

  override val controllerName: String = "AddressLookupRoutingController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map(
    "initialiseAddressLookupJourney" -> TestAddressLookupRoutingController.initialiseAddressLookupJourney(businessId, isEditMode = false),
    "addressLookupRedirect" -> TestAddressLookupRoutingController.addressLookupRedirect(businessId, None, isEditMode = false)
  )

  object TestAddressLookupRoutingController extends AddressLookupRoutingController(
    mockMessagesControllerComponents,
    mockAuthService,
    mockAddressLookupConnector,
    mockIncomeTaxSubscriptionConnector,
    mockMultipleSelfEmploymentsService
  )

  val continueUrl = s"http://localhost:9563/report-quarterly/income-and-expenses/sign-up/self-employments/details/address-lookup/$businessId"
  private val addressId = "12345"
  private val redirectUrl = "http://testLocation?id=" + addressId

  "initialiseAddressLookupJourney" should {

    "return ok (200)" when {
      "the connector returns data" in {
        mockAuthSuccess()
        mockInitialiseAddressLookup(continueUrl, isAgent = isAgent)(
          Right(PostAddressLookupSuccessResponse(Some(redirectUrl)))
        )

        val result = TestAddressLookupRoutingController.initialiseAddressLookupJourney(businessId, isEditMode = false)(fakeRequest)

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
          await(TestAddressLookupRoutingController.initialiseAddressLookupJourney(businessId, isEditMode = false)(fakeRequest))
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
          mockGetSelfEmployments(businessAccountingMethodKey)(
            Right(Some(testAccountingMethodModel))
          )
          mockGetAddressDetails(addressId)(Right(Some(testValidBusinessAddressModel)))
          mockSaveBusinessAddress(businessId, testValidBusinessAddressModel)(Right(PostSubscriptionDetailsSuccessResponse))

          val result = TestAddressLookupRoutingController.addressLookupRedirect(businessId, Some(addressId), isEditMode = true)(fakeRequest)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe
            Some(routes.SelfEmployedCYAController.show(businessId, isEditMode = true).url)
        }
      }
    }

    "is not in edit mode" when {
      "accounting method is defined" should {
        "redirect to sole trader check your answers page" when {
          "the address lookup service returns valid data" in {
            mockAuthSuccess()
            mockGetSelfEmployments(businessAccountingMethodKey)(
              Right(Some(testAccountingMethodModel))
            )
            mockGetAddressDetails(addressId)(Right(Some(testValidBusinessAddressModel)))
            mockSaveBusinessAddress(businessId, testValidBusinessAddressModel)(Right(PostSubscriptionDetailsSuccessResponse))

            val result = TestAddressLookupRoutingController.addressLookupRedirect(businessId, Some(addressId), isEditMode = false)(fakeRequest)
            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe
              Some(routes.SelfEmployedCYAController.show(businessId).url)
          }
        }
      }
    }

    "is not in edit mode" when {
      "accounting method is not defined" should {
        "redirect to sole trader accounting method page" when {
          "the address lookup service returns valid data" in {
            mockAuthSuccess()
            mockGetSelfEmployments(businessAccountingMethodKey)(Right(None))
            mockGetAddressDetails(addressId)(Right(Some(testValidBusinessAddressModel)))
            mockSaveBusinessAddress(businessId, testValidBusinessAddressModel)(Right(PostSubscriptionDetailsSuccessResponse))

            val result = TestAddressLookupRoutingController.addressLookupRedirect(businessId, Some(addressId), isEditMode = false)(fakeRequest)
            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe
              Some(routes.BusinessAccountingMethodController.show(businessId).url)
          }
        }
      }
    }

    authorisationTests()
  }
}
