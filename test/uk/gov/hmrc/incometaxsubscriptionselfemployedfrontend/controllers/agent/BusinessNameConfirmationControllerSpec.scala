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
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.mvc.{Action, AnyContent, AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{HTML, await, contentType, defaultAwaitTimeout, redirectLocation, status}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSubscriptionDetailsSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.mocks.MockIncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.ControllerBaseSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.BusinessNameConfirmationForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.submapping.YesNoMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{BusinessNameModel, ClientDetails}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.MultipleSelfEmploymentsService.SaveSelfEmploymentDataFailure
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.mocks.MockMultipleSelfEmploymentsService
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ITSASessionKeys
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.BusinessNameConfirmation

import scala.concurrent.Future

class BusinessNameConfirmationControllerSpec extends ControllerBaseSpec
  with MockIncomeTaxSubscriptionConnector with MockMultipleSelfEmploymentsService {

  val id: String = "testId"
  val clientDetails: ClientDetails = ClientDetails(
    name = "FirstName LastName",
    nino = "ZZ111111Z"
  )
  val fakeRequestWithClientDetails: FakeRequest[AnyContentAsEmpty.type] = fakeRequest
    .withSession(
      ITSASessionKeys.FirstName -> "FirstName",
      ITSASessionKeys.LastName -> "LastName",
      ITSASessionKeys.NINO -> "ZZ111111Z"
    )

  object TestBusinessNameConfirmationController extends BusinessNameConfirmationController(
    mockMessagesControllerComponents,
    mockAuthService,
    mockMultipleSelfEmploymentsService,
    mock[BusinessNameConfirmation],
    appConfig
  )(mockIncomeTaxSubscriptionConnector)

  override val controllerName: String = "BusinessNameConfirmationController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map(
    "show" -> TestBusinessNameConfirmationController.show(id),
    "submit" -> TestBusinessNameConfirmationController.submit(id)
  )

  trait Setup {
    val mockBusinessNameConfirmation: BusinessNameConfirmation = mock[BusinessNameConfirmation]

    val controller: BusinessNameConfirmationController = new BusinessNameConfirmationController(
      mockMessagesControllerComponents,
      mockAuthService,
      mockMultipleSelfEmploymentsService,
      mockBusinessNameConfirmation,
      appConfig
    )(mockIncomeTaxSubscriptionConnector)
  }

  "show" when {
    "there is a name in the users session" must {
      "return OK with the page contents" in new Setup {
        mockAuthSuccess()
        when(
          mockBusinessNameConfirmation(
            ArgumentMatchers.any(),
            ArgumentMatchers.eq(routes.BusinessNameConfirmationController.submit(id)),
            ArgumentMatchers.eq(appConfig.clientYourIncomeSourcesUrl),
            ArgumentMatchers.eq(clientDetails)
          )(ArgumentMatchers.any(), ArgumentMatchers.any())
        ).thenReturn(HtmlFormat.empty)

        val response: Future[Result] = controller.show(id)(fakeRequestWithClientDetails)

        status(response) mustBe OK
        contentType(response) mustBe Some(HTML)
      }
    }
  }

  "submit" when {
    "there is a name in the users session" when {
      "there is an error in the form submission" must {
        "return BAD_REQUEST with the page contents" in new Setup {
          mockAuthSuccess()

          when(
            mockBusinessNameConfirmation(
              ArgumentMatchers.any(),
              ArgumentMatchers.eq(routes.BusinessNameConfirmationController.submit(id)),
              ArgumentMatchers.eq(appConfig.clientYourIncomeSourcesUrl),
              ArgumentMatchers.eq(clientDetails)
            )(ArgumentMatchers.any(), ArgumentMatchers.any())
          ).thenReturn(HtmlFormat.empty)

          val response: Future[Result] = controller.submit(id)(fakeRequestWithClientDetails.withFormUrlEncodedBody())

          status(response) mustBe BAD_REQUEST
          contentType(response) mustBe Some(HTML)
        }
      }
      "the form submitted Yes" must {
        "save the users name as the business name and redirect to the business start date page" in new Setup {
          mockAuthSuccess()
          mockSaveBusinessName(id, BusinessNameModel(clientDetails.name))(Right(PostSubscriptionDetailsSuccessResponse))

          val response: Future[Result] = controller.submit(id)(fakeRequestWithClientDetails.withFormUrlEncodedBody(
            BusinessNameConfirmationForm.fieldName -> YesNoMapping.option_yes
          ))

          status(response) mustBe SEE_OTHER
          redirectLocation(response) mustBe Some(routes.BusinessStartDateController.show(id).url)
        }
        "throw an internal server exception" when {
          "the save of the business name failed" in new Setup {
            mockAuthSuccess()
            mockSaveBusinessName(id, BusinessNameModel(clientDetails.name))(Left(SaveSelfEmploymentDataFailure))

            intercept[InternalServerException](await(controller.submit(id)(fakeRequestWithClientDetails.withFormUrlEncodedBody(
              BusinessNameConfirmationForm.fieldName -> YesNoMapping.option_yes
            )))).message mustBe "[BusinessNameConfirmationController][submit] - Unable to save business name"
          }
        }
      }
      "the form submitted No" must {
        "redirect the user to the business name page" in new Setup {
          mockAuthSuccess()

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
