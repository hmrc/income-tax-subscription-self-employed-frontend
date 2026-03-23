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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.individual

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK, SEE_OTHER}
import play.api.mvc.{Action, AnyContent, AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{HTML, await, contentType, defaultAwaitTimeout, redirectLocation, status}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.ControllerBaseSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.UkAddressConfirmationForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.submapping.YesNoMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{DateModel, SoleTraderBusiness}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.mocks.{MockMultipleSelfEmploymentsService, MockSessionDataService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ITSASessionKeys
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.individual.UkAddressConfirmation

import scala.concurrent.Future

class UkAddressConfirmationControllerSpec extends ControllerBaseSpec
  with MockSessionDataService with MockMultipleSelfEmploymentsService {

  val id: String = "testId"
  val name: String = "FirstName LastName"
  val business: SoleTraderBusiness = SoleTraderBusiness(
    id = id,
    confirmed = true,
    startDateBeforeLimit = Some(true),
    startDate = Some(DateModel("1", "1", "2001")),
    name = Some("Test Business"),
    trade = Some("Plumbing"),
    address = None
  )
  val fakeRequestWithName: FakeRequest[AnyContentAsEmpty.type] = fakeRequest.withSession(ITSASessionKeys.FullNameSessionKey -> name)

  object TestUkAddressConfirmationController extends UkAddressConfirmationController(
    mockMessagesControllerComponents,
    mockAuthService,
    mockMultipleSelfEmploymentsService,
    mock[UkAddressConfirmation]
  )(
    mockSessionDataService,
    appConfig
  )

  override val controllerName: String = "UkNameConfirmationController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map(
    "show" -> TestUkAddressConfirmationController.show(id, false, false),
    "submit" -> TestUkAddressConfirmationController.submit(id, false, false)
  )

  trait Setup {
    val mockUkAddressConfirmation: UkAddressConfirmation = mock[UkAddressConfirmation]

    val controller: UkAddressConfirmationController = new UkAddressConfirmationController(
      mockMessagesControllerComponents,
      mockAuthService,
      mockMultipleSelfEmploymentsService,
      mockUkAddressConfirmation
    )(
      mockSessionDataService,
      appConfig
    )
  }

  "show" should {
    "throw an exception" when {
      "there was an error fetching business" in new Setup {
        mockAuthSuccess()
        mockFetchBusiness(id)(Left(GetSelfEmploymentsHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))

        intercept[InternalServerException](await(controller.show(id, false, false)(fakeRequest)))
          .message mustBe "Cannot get business name"
      }
    }

    "return OK with the page content" when {
      "a business was found" in new Setup {
        mockAuthSuccess()
        mockFetchBusiness(id)(Right(Some(business)))

        when(mockUkAddressConfirmation(
          ArgumentMatchers.any(),
          ArgumentMatchers.any(),
          ArgumentMatchers.eq(business.name.getOrElse(""))
        )(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(HtmlFormat.empty)

        val response: Future[Result] = controller.show(id, false, false)(fakeRequest)

        status(response) mustBe OK
        contentType(response) mustBe Some(HTML)
      }
    }
  }

  "submit" must {
    "throw an internal server exception" when {
      "there was an error fetching a first address" in new Setup {
        mockAuthSuccess()
        mockFetchBusiness(id)(Left(GetSelfEmploymentsHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))

        intercept[InternalServerException](await(controller.submit(id, false, false)(fakeRequest)))
          .message mustBe "Cannot get business name"
      }
    }

    "redirect the user to the address lookup initialise" when {
      "the user selects 'No' that their address in the Uk" in new Setup {
        mockAuthSuccess()
        mockFetchBusiness(id)(Right(Some(business)))

        val response: Future[Result] = controller.submit(id, false, false)(
          fakeRequest.withFormUrlEncodedBody(UkAddressConfirmationForm.fieldName -> YesNoMapping.option_no)
        )

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(routes.AddressLookupRoutingController.initialiseAddressLookupJourney(id, false).url)
      }
      "the user selects 'Yes' that their address is the same" in new Setup {
        mockAuthSuccess()
        mockFetchBusiness(id)(Right(Some(business)))

        val response: Future[Result] = controller.submit(id, false, false)(
          fakeRequest.withFormUrlEncodedBody(UkAddressConfirmationForm.fieldName -> YesNoMapping.option_yes)
        )

        status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(routes.AddressLookupRoutingController.initialiseAddressLookupJourney(id, true).url)
      }
    }

    "return BAD_REQUEST with the page content" when {
      "the user does not select an option" in new Setup {
        mockAuthSuccess()
        mockFetchBusiness(id)(Right(Some(business)))

        when(mockUkAddressConfirmation(
          ArgumentMatchers.any(),
          ArgumentMatchers.any(),
          ArgumentMatchers.eq(business.name.getOrElse(""))
        )(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(HtmlFormat.empty)

        val response: Future[Result] = controller.submit(id, false, false)(
          fakeRequest
        )

        status(response) mustBe BAD_REQUEST
        contentType(response) mustBe Some(HTML)
      }
    }
  }
}
