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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.generic

import play.api.mvc.{Action, AnyContent}
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.businessAccountingMethodKey
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitch.SaveAndRetrieve
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.addresslookup.mocks.MockAddressLookupConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSubscriptionDetailsSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup.GetAddressLookupDetailsHttpParser
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup.PostAddressLookupHttpParser.{PostAddressLookupSuccessResponse, UnexpectedStatusFailure}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.mocks.MockIncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.ControllerBaseSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.mocks.MockMultipleSelfEmploymentsService
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.TestModels._

trait AddressLookupRoutingControllerGenericSpec extends ControllerBaseSpec
  with MockAddressLookupConnector
  with MockIncomeTaxSubscriptionConnector
  with MockMultipleSelfEmploymentsService
  with FeatureSwitching{

  def isAgent: Boolean

  override def beforeEach(): Unit = {
    disable(SaveAndRetrieve)
    super.beforeEach()
  }

  final val businessId = Math.random().toString
  final val addressId: String = Math.random().toString

  def testAddressLookupRoutingController: AddressLookupRoutingControllerGeneric

  def continueUrl: String
  def redirectUrl: String

  def authorisedRoutes: Map[String, Action[AnyContent]]

  "initialiseAddressLookupJourney" should {

    "return ok (200)" when {
      "the connector returns data" in {
        mockAuthSuccess()
        mockInitialiseAddressLookup(continueUrl, isAgent = isAgent)(
          Right(PostAddressLookupSuccessResponse(Some(redirectUrl)))
        )
        mockSaveAddressRedirect(businessId, redirectUrl)(Right(PostSubscriptionDetailsSuccessResponse))

        val result = testAddressLookupRoutingController.initialiseAddressLookupJourney(businessId, isEditMode = false)(fakeRequest)

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
          await(testAddressLookupRoutingController.initialiseAddressLookupJourney(businessId, isEditMode = false)(fakeRequest))
        )
        result.message mustBe "[AddressLookupRoutingController][initialiseAddressLookupJourney] - Unexpected response, status: 500"
      }
    }
  }

  "addressLookupRedirect" when {

    "save and retrieve is enabled" when {
      "is in edit mode" should {
        "redirect to sole trader check your answer page" when {
          "the address lookup service returns valid data" in {
            mockAuthSuccess()
            enable(SaveAndRetrieve)
            mockGetSelfEmployments(businessAccountingMethodKey)(
              Right(Some(testAccountingMethodModel))
            )
            mockGetAddressDetails(addressId)(Right(Some(testValidBusinessAddressModel)))
            mockSaveBusinessAddress(businessId, testValidBusinessAddressModel)(Right(PostSubscriptionDetailsSuccessResponse))

            val result = testAddressLookupRoutingController.addressLookupRedirect(businessId, Some(addressId), isEditMode = true)(fakeRequest)
            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe
              Some(redirect2)
          }
        }
      }

      "is not in edit mode" when {
        "accounting method is defined" should {
          "redirect to sole trader check your answers page" when {
            "the address lookup service returns valid data" in {
              mockAuthSuccess()
              enable(SaveAndRetrieve)
              mockGetSelfEmployments(businessAccountingMethodKey)(
                Right(Some(testAccountingMethodModel))
              )
              mockGetAddressDetails(addressId)(Right(Some(testValidBusinessAddressModel)))
              mockSaveBusinessAddress(businessId, testValidBusinessAddressModel)(Right(PostSubscriptionDetailsSuccessResponse))

              val result = testAddressLookupRoutingController.addressLookupRedirect(businessId, Some(addressId), isEditMode = false)(fakeRequest)
              status(result) mustBe SEE_OTHER
              redirectLocation(result) mustBe Some(redirect1)
            }
          }
        }
      }

      "is not in edit mode" when {
        "accounting method is not defined" should {
          "redirect to sole trader accounting method page" when {
            "the address lookup service returns valid data" in {
              mockAuthSuccess()
              enable(SaveAndRetrieve)
              mockGetSelfEmployments(businessAccountingMethodKey)(Right(None))
              mockGetAddressDetails(addressId)(Right(Some(testValidBusinessAddressModel)))
              mockSaveBusinessAddress(businessId, testValidBusinessAddressModel)(Right(PostSubscriptionDetailsSuccessResponse))

              val result = testAddressLookupRoutingController.addressLookupRedirect(businessId, Some(addressId), isEditMode = false)(fakeRequest)
              status(result) mustBe SEE_OTHER
              redirectLocation(result) mustBe
                Some(redirect3)
            }
          }
        }
      }
    }

    "save and retrieve is not enabled" when {
      "is in edit mode" should {
        "redirect to business list check your answer page" when {
          "the address lookup service returns valid data" in {
            mockAuthSuccess()
            mockGetSelfEmployments(businessAccountingMethodKey)(Right(None))
            mockGetAddressDetails(addressId)(Right(Some(testValidBusinessAddressModel)))
            mockSaveBusinessAddress(businessId, testValidBusinessAddressModel)(Right(PostSubscriptionDetailsSuccessResponse))

            val result = testAddressLookupRoutingController.addressLookupRedirect(businessId, Some(addressId), isEditMode = true)(fakeRequest)
            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe
              Some(redirect4)
          }
        }
      }

      "is not in edit mode" should {
        "redirect to business list check your answer page" when {
          "the address lookup service returns valid data" in {
            mockAuthSuccess()
                mockGetSelfEmployments(businessAccountingMethodKey)(Right(None))
            mockGetAddressDetails(addressId)(Right(Some(testValidBusinessAddressModel)))
            mockSaveBusinessAddress(businessId, testValidBusinessAddressModel)(Right(PostSubscriptionDetailsSuccessResponse))

            val result = testAddressLookupRoutingController.addressLookupRedirect(businessId, Some(addressId), isEditMode = false)(fakeRequest)
            status(result) mustBe SEE_OTHER
            redirectLocation(result) mustBe
              Some(redirect5)
          }
        }

        "Throw an internal exception" when {
          "there is an unexpected status failure" in {
            mockAuthSuccess()
            mockGetAddressDetails(addressId)(Left(GetAddressLookupDetailsHttpParser.UnexpectedStatusFailure(500)))
                mockGetSelfEmployments(businessAccountingMethodKey)(Right(None))
                val result = intercept[InternalServerException](
                  await(testAddressLookupRoutingController.addressLookupRedirect(businessId, Some(addressId), isEditMode = false)(fakeRequest))
                )
                result.message mustBe "[AddressLookupRoutingController][fetchAddress] - Unexpected response, status: 500"
          }

          "there is an invalid Json" in {
            mockAuthSuccess()
            mockGetSelfEmployments(businessAccountingMethodKey)(Right(None))
            mockGetAddressDetails(addressId)(Left(GetAddressLookupDetailsHttpParser.InvalidJson))
            val response = intercept[InternalServerException](
              await(testAddressLookupRoutingController.addressLookupRedirect(businessId, Some(addressId), isEditMode = false)(fakeRequest))
            )
            response.message mustBe "[AddressLookupRoutingController][fetchAddress] - Invalid json response"
          }

          "failure retrieving accounting method" in {
            mockAuthSuccess()
            mockGetAddressDetails(addressId)(Right(Some(testValidBusinessAddressModel)))
            mockGetSelfEmployments(businessAccountingMethodKey)(Left(GetSelfEmploymentsHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))
            val result = intercept[InternalServerException](
              await(testAddressLookupRoutingController.addressLookupRedirect(businessId, Some(addressId), isEditMode = false)(fakeRequest))
            )
            result.message mustBe "[AddressLookupRoutingController][fetchAccountMethod] - Failure retrieving accounting method"
          }
        }
      }
    }

    authorisationTests()
  }

  def redirect5: String

  def redirect4: String

  def redirect3: String

  def redirect2: String

  def redirect1: String
}
