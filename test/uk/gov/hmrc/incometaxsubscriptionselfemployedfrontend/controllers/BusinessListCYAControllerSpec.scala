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

import play.api.mvc.{Action, AnyContent, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.{InvalidJson, UnexpectedStatusFailure}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.mocks.MockIncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.AddAnotherBusinessForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.submapping.YesNoMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ImplicitDateFormatterImpl
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.mocks.individual.MockCheckYourAnswers

import scala.concurrent.Future

class BusinessListCYAControllerSpec extends ControllerBaseSpec with MockIncomeTaxSubscriptionConnector with MockCheckYourAnswers {

  implicit val mockImplicitDateFormatter: ImplicitDateFormatterImpl = new ImplicitDateFormatterImpl(mockLanguageUtils)

  val id: String = "testId"

  override val controllerName: String = "BusinessListCYAController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map(
    "show" -> TestBusinessListCYAController.show()
  )

  object TestBusinessListCYAController extends BusinessListCYAController(
    mockAuthService,
    checkYourAnswers,
    mockIncomeTaxSubscriptionConnector,
    mockMessagesControllerComponents
  )

  val businessData: SelfEmploymentData = SelfEmploymentData(
    id = id,
    businessStartDate = Some(BusinessStartDate(DateModel("1", "1", "2017"))),
    businessName = Some(BusinessNameModel("ABC Limited")),
    businessTradeName = Some(BusinessTradeNameModel("Plumbing")),
    businessAddress = Some(BusinessAddressModel("12345", Address(Seq("line1"), "TF3 4NT")))
  )

  "show" should {

    "return OK (200)" when {
      "the connector returns successful json" in {
        mockAuthSuccess()
        mockGetSelfEmployments[Seq[SelfEmploymentData]]("Businesses")(Right(Some(Seq(
          businessData
        ))))
        mockCheckYourAnswers()

        val result: Future[Result] = TestBusinessListCYAController.show()(fakeRequest)
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
      }
    }

    "return (303) redirect to business start date page" when {
      "no businesses are returned" in {
        mockAuthSuccess()
        mockGetSelfEmployments[Seq[SelfEmploymentData]]("Businesses")(Right(None))

        val result: Future[Result] = TestBusinessListCYAController.show()(fakeRequest)
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(routes.InitialiseController.initialise.url)
      }
      "no complete businesses are returned" in {
        mockAuthSuccess()
        mockGetSelfEmployments[Seq[SelfEmploymentData]]("Businesses")(Right(Some(Seq(
          businessData.copy(businessName = None)
        ))))

        val result: Future[Result] = TestBusinessListCYAController.show()(fakeRequest)
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(routes.InitialiseController.initialise.url)
      }
    }

    "throw an internal server error" when {
      "there is an unexpected status failure" in {
        mockAuthSuccess()
        mockGetSelfEmployments[Seq[SelfEmploymentData]]("Businesses")(Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))

        val response = intercept[InternalServerException](await(TestBusinessListCYAController.show()(fakeRequest)))
        response.message mustBe "[BusinessListCYAController][show] - getSelfEmployments connection failure, status: 500"
      }

      "the connector returns an invalid Json" in {
        mockAuthSuccess()
        mockGetSelfEmployments[Seq[SelfEmploymentData]]("Businesses")(Left(InvalidJson))

        val response = intercept[InternalServerException](await(TestBusinessListCYAController.show()(fakeRequest)))
        response.message mustBe ("[BusinessListCYAController][show] - Invalid Json")
      }
    }

  }

  "submit" should {
    "return 303, SEE_OTHER" when {
      "the connector returns successful json and submit with Yes option redirect to Initialise controller" in {
        mockAuthSuccess()
        mockGetSelfEmployments[Seq[SelfEmploymentData]]("Businesses")(Right(Some(Seq(
          businessData
        ))))

        val result: Future[Result] = TestBusinessListCYAController.submit()(fakeRequest
          .withFormUrlEncodedBody(AddAnotherBusinessForm.addAnotherBusiness -> YesNoMapping.option_yes))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.InitialiseController.initialise.url)

      }

      "the connector returns successful json and submit with No option redirect to Business Accounting method page" in {
        mockAuthSuccess()
        mockGetSelfEmployments[Seq[SelfEmploymentData]]("Businesses")(Right(Some(Seq(
          businessData
        ))))

        val result: Future[Result] = TestBusinessListCYAController.submit()(fakeRequest
          .withFormUrlEncodedBody(AddAnotherBusinessForm.addAnotherBusiness -> YesNoMapping.option_no))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.BusinessAccountingMethodController.show().url)
      }

    }

    "return (303) redirect to business start date page" when {
      "no businesses are returned" in {
        mockAuthSuccess()
        mockGetSelfEmployments[Seq[SelfEmploymentData]]("Businesses")(Right(None))

        val result: Future[Result] = TestBusinessListCYAController.submit()(fakeRequest
          .withFormUrlEncodedBody(AddAnotherBusinessForm.addAnotherBusiness -> YesNoMapping.option_no))
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(routes.InitialiseController.initialise.url)
      }
      "no complete businesses are returned" in {
        mockAuthSuccess()
        mockGetSelfEmployments[Seq[SelfEmploymentData]]("Businesses")(Right(Some(Seq(
          businessData.copy(businessName = None)
        ))))

        val result: Future[Result] = TestBusinessListCYAController.submit()(fakeRequest
          .withFormUrlEncodedBody(AddAnotherBusinessForm.addAnotherBusiness -> YesNoMapping.option_no))
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(routes.InitialiseController.initialise.url)
      }
    }

    "throw an internal server error" when {
      "there is an unexpected status failure" in {
        mockAuthSuccess()
        mockGetSelfEmployments[Seq[SelfEmploymentData]]("Businesses")(Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))

        val response = intercept[InternalServerException](await(TestBusinessListCYAController.submit()(fakeRequest)))
        response.message mustBe "[BusinessListCYAController][submit] - getSelfEmployments connection failure, status: 500"
      }

      "the connector returns an invalid Json" in {
        mockAuthSuccess()
        mockGetSelfEmployments[Seq[SelfEmploymentData]]("Businesses")(Left(InvalidJson))

        val response = intercept[InternalServerException](await(TestBusinessListCYAController.submit()(fakeRequest
          .withFormUrlEncodedBody(AddAnotherBusinessForm.addAnotherBusiness -> YesNoMapping.option_yes))))
        response.message mustBe ("[BusinessListCYAController][submit] - Invalid Json")
      }
    }

  }

  authorisationTests()

}
