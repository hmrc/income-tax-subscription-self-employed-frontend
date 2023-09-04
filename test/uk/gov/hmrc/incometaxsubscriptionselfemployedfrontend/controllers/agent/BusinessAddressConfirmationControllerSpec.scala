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

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK, SEE_OTHER}
import play.api.mvc.{Action, AnyContent, AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{HTML, await, contentType, defaultAwaitTimeout, redirectLocation, status}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSubscriptionDetailsSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.mocks.MockIncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.ControllerBaseSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.BusinessAddressConfirmationForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.submapping.YesNoMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{Address, BusinessAddressModel, SelfEmploymentData}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.MultipleSelfEmploymentsService.SaveSelfEmploymentDataFailure
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.mocks.MockMultipleSelfEmploymentsService
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ITSASessionKeys
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.BusinessAddressConfirmation

import scala.concurrent.Future

class BusinessAddressConfirmationControllerSpec extends ControllerBaseSpec
  with MockIncomeTaxSubscriptionConnector with MockMultipleSelfEmploymentsService {

  val id: String = "testId"
  val name: String = "FirstName LastName"
  val address: Address = Address(
    Seq(
      "1 Long Road",
      "Lonely town"
    ),
    Some("ZZ11ZZ")
  )
  val fakeRequestWithName: FakeRequest[AnyContentAsEmpty.type] = fakeRequest.withSession(ITSASessionKeys.FullNameSessionKey -> name)

  object TestBusinessAddressConfirmationController extends BusinessAddressConfirmationController(
    mockMessagesControllerComponents,
    mockAuthService,
    mockMultipleSelfEmploymentsService,
    mock[BusinessAddressConfirmation],
    appConfig
  )(mockIncomeTaxSubscriptionConnector)

  override val controllerName: String = "BusinessNameConfirmationController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map(
    "show" -> TestBusinessAddressConfirmationController.show(id),
    "submit" -> TestBusinessAddressConfirmationController.submit(id)
  )

  trait Setup {
    val mockBusinessAddressConfirmation: BusinessAddressConfirmation = mock[BusinessAddressConfirmation]

    val controller: BusinessAddressConfirmationController = new BusinessAddressConfirmationController(
      mockMessagesControllerComponents,
      mockAuthService,
      mockMultipleSelfEmploymentsService,
      mockBusinessAddressConfirmation,
      appConfig
    )(mockIncomeTaxSubscriptionConnector)
  }

  "show" should {
    "throw an exception" when {
      "there was an error fetching all businesses" in new Setup {
        mockAuthSuccess()
        mockFetchAllBusinesses(Left(GetSelfEmploymentsHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))

        intercept[InternalServerException](await(controller.show(id)(fakeRequest)))
          .message mustBe "[BusinessAddressConfirmationController][withFirstBusiness] - Unable to retrieve businesses"
      }
    }
    "redirect the user to the address lookup initialise" when {
      "there are no previous businesses" in new Setup {
        mockAuthSuccess()
        mockFetchAllBusinesses(Right(Seq.empty[SelfEmploymentData]))

        val response: Future[Result] = controller.show(id)(fakeRequest)

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(routes.AddressLookupRoutingController.initialiseAddressLookupJourney(id).url)
      }
      "there is a previous business but it has no address" in new Setup {
        mockAuthSuccess()
        mockFetchAllBusinesses(Right(Seq(
          SelfEmploymentData(id = id))
        ))

        val response: Future[Result] = controller.show(id)(fakeRequest)

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(routes.AddressLookupRoutingController.initialiseAddressLookupJourney(id).url)
      }
    }
    "return OK with the page content" when {
      "a previous business address was found" in new Setup {
        mockAuthSuccess()
        mockFetchAllBusinesses(Right(Seq(
          SelfEmploymentData(
            id = id,
            businessAddress = Some(BusinessAddressModel(address))
          )
        )))

        when(mockBusinessAddressConfirmation(
          ArgumentMatchers.any(),
          ArgumentMatchers.eq(routes.BusinessAddressConfirmationController.submit(id)),
          ArgumentMatchers.eq(routes.BusinessTradeNameController.show(id).url),
          ArgumentMatchers.eq(address),
          ArgumentMatchers.any()
        )(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(HtmlFormat.empty)

        val response: Future[Result] = controller.show(id)(fakeRequest)

        status(response) mustBe OK
        contentType(response) mustBe Some(HTML)
      }
    }
  }

  "submit" must {
    "throw an internal server exception" when {
      "there was an error fetching all businesses" in new Setup {
        mockAuthSuccess()
        mockFetchAllBusinesses(Left(GetSelfEmploymentsHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))

        intercept[InternalServerException](await(controller.submit(id)(fakeRequest)))
          .message mustBe "[BusinessAddressConfirmationController][withFirstBusiness] - Unable to retrieve businesses"
      }
      "there was an error saving the business address" in new Setup {
        mockAuthSuccess()
        mockFetchAllBusinesses(Right(Seq(
          SelfEmploymentData(
            id = id,
            businessAddress = Some(BusinessAddressModel(address))
          )
        )))
        mockSaveBusinessAddress(id, BusinessAddressModel(address))(Left(SaveSelfEmploymentDataFailure))

        intercept[InternalServerException](await(controller.submit(id)(
          fakeRequest.withFormUrlEncodedBody(BusinessAddressConfirmationForm.fieldName -> YesNoMapping.option_yes)
        ))).message mustBe "[BusinessAddressConfirmationController][saveBusinessAddress] - Unable to save business address"
      }
    }
    "redirect the user to the address lookup initialise" when {
      "there are no previous businesses" in new Setup {
        mockAuthSuccess()
        mockFetchAllBusinesses(Right(Seq.empty[SelfEmploymentData]))

        val response: Future[Result] = controller.submit(id)(fakeRequest)

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(routes.AddressLookupRoutingController.initialiseAddressLookupJourney(id).url)
      }
      "there is a previous business but it has no address" in new Setup {
        mockAuthSuccess()
        mockFetchAllBusinesses(Right(Seq(
          SelfEmploymentData(id = id))
        ))

        val response: Future[Result] = controller.submit(id)(fakeRequest)

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(routes.AddressLookupRoutingController.initialiseAddressLookupJourney(id).url)
      }
      "the user selects 'No' that their address is not the same" in new Setup {
        mockAuthSuccess()
        mockFetchAllBusinesses(Right(Seq(
          SelfEmploymentData(
            id = id,
            businessAddress = Some(BusinessAddressModel(address))
          )
        )))

        val response: Future[Result] = controller.submit(id)(
          fakeRequest.withFormUrlEncodedBody(BusinessAddressConfirmationForm.fieldName -> YesNoMapping.option_no)
        )

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(routes.AddressLookupRoutingController.initialiseAddressLookupJourney(id).url)
      }
    }
    "save the business address and redirect the user to the check your answers" when {
      "the user selects 'Yes' that their address is the same" in new Setup {
        mockAuthSuccess()
        mockFetchAllBusinesses(Right(Seq(
          SelfEmploymentData(
            id = id,
            businessAddress = Some(BusinessAddressModel(address))
          )
        )))
        mockSaveBusinessAddress(id, BusinessAddressModel(address))(Right(PostSubscriptionDetailsSuccessResponse))

        val response: Future[Result] = controller.submit(id)(
          fakeRequest.withFormUrlEncodedBody(BusinessAddressConfirmationForm.fieldName -> YesNoMapping.option_yes)
        )

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(routes.SelfEmployedCYAController.show(id).url)
      }
    }
    "return BAD_REQUEST with the page content" when {
      "the user does not select an option" in new Setup {
        mockAuthSuccess()
        mockFetchAllBusinesses(Right(Seq(
          SelfEmploymentData(
            id = id,
            businessAddress = Some(BusinessAddressModel(address))
          )
        )))

        when(mockBusinessAddressConfirmation(
          ArgumentMatchers.any(),
          ArgumentMatchers.eq(routes.BusinessAddressConfirmationController.submit(id)),
          ArgumentMatchers.eq(routes.BusinessTradeNameController.show(id).url),
          ArgumentMatchers.eq(address),
          ArgumentMatchers.any()
        )(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(HtmlFormat.empty)

        val response: Future[Result] = controller.submit(id)(
          fakeRequest
        )

        status(response) mustBe BAD_REQUEST
        contentType(response) mustBe Some(HTML)
      }
    }
  }

  authorisationTests()

}
