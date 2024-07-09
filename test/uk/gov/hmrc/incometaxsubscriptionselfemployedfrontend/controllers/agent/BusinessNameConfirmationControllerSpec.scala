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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.ControllerBaseSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.BusinessNameConfirmationForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.submapping.YesNoMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.ClientDetails
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.MultipleSelfEmploymentsService.SaveSelfEmploymentDataFailure
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.mocks.{MockClientDetailsRetrieval, MockMultipleSelfEmploymentsService, MockSessionDataService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ITSASessionKeys
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.BusinessNameConfirmation

import scala.concurrent.Future

class BusinessNameConfirmationControllerSpec extends ControllerBaseSpec
  with MockSessionDataService with MockMultipleSelfEmploymentsService
  with MockClientDetailsRetrieval {

  val id: String = "testId"
  val clientDetails: ClientDetails = ClientDetails(
    name = "FirstName LastName",
    nino = testNino
  )

  val fakeRequestWithClientDetails: FakeRequest[AnyContentAsEmpty.type] = fakeRequest
    .withSession(
      ITSASessionKeys.FirstName -> "FirstName",
      ITSASessionKeys.LastName -> "LastName",
      ITSASessionKeys.REFERENCE -> "test-reference"
    )

  val testBusinessName = "test-business-name"

  object TestBusinessNameConfirmationController extends BusinessNameConfirmationController(
    mockMessagesControllerComponents,
    mockClientDetailsRetrieval,
    mockAuthService,
    mockMultipleSelfEmploymentsService,
    mock[BusinessNameConfirmation]
  )(
    mockSessionDataService,
    appConfig
  )

  override val controllerName: String = "BusinessNameConfirmationController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map(
    "show" -> TestBusinessNameConfirmationController.show(id),
    "submit" -> TestBusinessNameConfirmationController.submit(id)
  )

  trait Setup {
    val mockBusinessNameConfirmation: BusinessNameConfirmation = mock[BusinessNameConfirmation]

    val controller: BusinessNameConfirmationController = new BusinessNameConfirmationController(
      mockMessagesControllerComponents,
      mockClientDetailsRetrieval,
      mockAuthService,
      mockMultipleSelfEmploymentsService,
      mockBusinessNameConfirmation
    )(
      mockSessionDataService,
      appConfig
    )
  }

  "show" must {
    "return OK (200) with the page contents which has the already existing business name" when {
      "a business has already been previously added" in new Setup {
        mockAuthSuccess()
        mockFetchFirstBusinessName(Right(Some(testBusinessName)))

        when(mockBusinessNameConfirmation(
          ArgumentMatchers.any(),
          ArgumentMatchers.eq(routes.BusinessNameConfirmationController.submit(id)),
          ArgumentMatchers.eq(appConfig.clientYourIncomeSourcesUrl),
          ArgumentMatchers.eq(clientDetails),
          ArgumentMatchers.eq(testBusinessName),
          ArgumentMatchers.eq(true)
        )(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(HtmlFormat.empty)

        val response: Future[Result] = controller.show(id)(fakeRequestWithClientDetails)

        status(response) mustBe OK
        contentType(response) mustBe Some(HTML)
      }
    }
    "return OK (200) with the page contents which has the clients personal name" when {
      "there is no previously added business" in new Setup {
        mockAuthSuccess()
        mockFetchFirstBusinessName(Right(None))

        when(mockBusinessNameConfirmation(
          ArgumentMatchers.any(),
          ArgumentMatchers.eq(routes.BusinessNameConfirmationController.submit(id)),
          ArgumentMatchers.eq(appConfig.clientYourIncomeSourcesUrl),
          ArgumentMatchers.eq(clientDetails),
          ArgumentMatchers.eq(clientDetails.name),
          ArgumentMatchers.eq(false)
        )(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(HtmlFormat.empty)

        val response: Future[Result] = controller.show(id)(fakeRequestWithClientDetails)

        status(response) mustBe OK
        contentType(response) mustBe Some(HTML)
      }
    }
    "throw an INTERNAL_SERVER_EXCEPTION" when {
      "there was an error response fetching the sole trader businesses" in new Setup {
        mockAuthSuccess()
        mockFetchFirstBusinessName(Left(GetSelfEmploymentsHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))

        intercept[InternalServerException](await(controller.show(id)(fakeRequestWithClientDetails)))
          .message mustBe "[BusinessNameConfirmationController][withBusinessOrClientsName] - Unable to retrieve businesses"
      }
    }
  }

  "submit" when {
    "there is a previously added business name" when {
      "there is an error in the form submission" must {
        "return BAD_REQUEST with the page contents" in new Setup {
          mockAuthSuccess()
          mockFetchFirstBusinessName(Right(Some(testBusinessName)))

          when(
            mockBusinessNameConfirmation(
              ArgumentMatchers.any(),
              ArgumentMatchers.eq(routes.BusinessNameConfirmationController.submit(id)),
              ArgumentMatchers.eq(appConfig.clientYourIncomeSourcesUrl),
              ArgumentMatchers.eq(clientDetails),
              ArgumentMatchers.eq(testBusinessName),
              ArgumentMatchers.eq(true)
            )(ArgumentMatchers.any(), ArgumentMatchers.any())
          ).thenReturn(HtmlFormat.empty)

          val response: Future[Result] = controller.submit(id)(fakeRequestWithClientDetails)

          status(response) mustBe BAD_REQUEST
          contentType(response) mustBe Some(HTML)
        }
      }
      "the form submitted Yes" must {
        "save the users name as the business name and redirect to the business start date page" in new Setup {
          mockAuthSuccess()
          mockFetchFirstBusinessName(Right(Some(testBusinessName)))
          mockSaveBusinessName(id, testBusinessName)(Right(PostSubscriptionDetailsSuccessResponse))

          val response: Future[Result] = controller.submit(id)(fakeRequestWithClientDetails.withFormUrlEncodedBody(
            BusinessNameConfirmationForm.fieldName -> YesNoMapping.option_yes
          ))

          status(response) mustBe SEE_OTHER
          redirectLocation(response) mustBe Some(routes.BusinessStartDateController.show(id).url)
        }
        "throw an internal server exception" when {
          "the save of the business name failed" in new Setup {
            mockAuthSuccess()
            mockFetchFirstBusinessName(Right(Some(testBusinessName)))
            mockSaveBusinessName(id, testBusinessName)(Left(SaveSelfEmploymentDataFailure))

            intercept[InternalServerException](await(controller.submit(id)(fakeRequestWithClientDetails.withFormUrlEncodedBody(
              BusinessNameConfirmationForm.fieldName -> YesNoMapping.option_yes
            )))).message mustBe "[BusinessNameConfirmationController][submit] - Unable to save business name"
          }
        }
      }
      "the form submitted No" must {
        "redirect the user to the business name page" in new Setup {
          mockAuthSuccess()
          mockFetchFirstBusinessName(Right(Some(testBusinessName)))

          val response: Future[Result] = controller.submit(id)(fakeRequestWithClientDetails.withFormUrlEncodedBody(
            BusinessNameConfirmationForm.fieldName -> YesNoMapping.option_no
          ))

          status(response) mustBe SEE_OTHER
          redirectLocation(response) mustBe Some(routes.BusinessNameController.show(id).url)
        }
      }
    }
    "there is a name in the users session" when {
      "there is an error in the form submission" must {
        "return BAD_REQUEST with the page contents" in new Setup {
          mockAuthSuccess()
          mockFetchFirstBusinessName(Right(None))

          when(
            mockBusinessNameConfirmation(
              ArgumentMatchers.any(),
              ArgumentMatchers.eq(routes.BusinessNameConfirmationController.submit(id)),
              ArgumentMatchers.eq(appConfig.clientYourIncomeSourcesUrl),
              ArgumentMatchers.eq(clientDetails),
              ArgumentMatchers.eq(clientDetails.name),
              ArgumentMatchers.eq(false)
            )(ArgumentMatchers.any(), ArgumentMatchers.any())
          ).thenReturn(HtmlFormat.empty)

          val response: Future[Result] = controller.submit(id)(fakeRequestWithClientDetails)

          status(response) mustBe BAD_REQUEST
          contentType(response) mustBe Some(HTML)
        }
      }
      "the form submitted Yes" must {
        "save the users name as the business name and redirect to the business start date page" in new Setup {
          mockAuthSuccess()
          mockFetchFirstBusinessName(Right(None))
          mockSaveBusinessName(id, clientDetails.name)(Right(PostSubscriptionDetailsSuccessResponse))

          val response: Future[Result] = controller.submit(id)(fakeRequestWithClientDetails.withFormUrlEncodedBody(
            BusinessNameConfirmationForm.fieldName -> YesNoMapping.option_yes
          ))

          status(response) mustBe SEE_OTHER
          redirectLocation(response) mustBe Some(routes.BusinessStartDateController.show(id).url)
        }
        "throw an internal server exception" when {
          "the save of the business name failed" in new Setup {
            mockAuthSuccess()
            mockFetchFirstBusinessName(Right(None))
            mockSaveBusinessName(id, clientDetails.name)(Left(SaveSelfEmploymentDataFailure))

            intercept[InternalServerException](await(controller.submit(id)(fakeRequestWithClientDetails.withFormUrlEncodedBody(
              BusinessNameConfirmationForm.fieldName -> YesNoMapping.option_yes
            )))).message mustBe "[BusinessNameConfirmationController][submit] - Unable to save business name"
          }
        }
      }
      "the form submitted No" must {
        "redirect the user to the business name page" in new Setup {
          mockAuthSuccess()
          mockFetchFirstBusinessName(Right(None))

          val response: Future[Result] = controller.submit(id)(fakeRequestWithClientDetails.withFormUrlEncodedBody(
            BusinessNameConfirmationForm.fieldName -> YesNoMapping.option_no
          ))

          status(response) mustBe SEE_OTHER
          redirectLocation(response) mustBe Some(routes.BusinessNameController.show(id).url)
        }
      }
    }
  }

  authorisationTests()

}
