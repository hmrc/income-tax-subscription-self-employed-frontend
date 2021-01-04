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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers

import play.api.mvc.{Action, AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.businessesKey
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.{InvalidJson, UnexpectedStatusFailure}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.mocks.MockIncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._

import scala.concurrent.Future

class RemoveBusinessControllerSpec extends ControllerBaseSpec with MockIncomeTaxSubscriptionConnector {

  val id: String = "testId"

  override val controllerName: String = "RemoveBusinessController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map(
    "show" -> TestRemoveBusinessController.show(id)
  )

  object TestRemoveBusinessController extends RemoveBusinessController(
    mockAuthService,
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
    "return (303) redirect to business CYA page when Remove business has multiple business and click remove business" when {
      "the connector returns successful json" in {
        mockAuthSuccess()
        mockGetSelfEmployments[Seq[SelfEmploymentData]]("Businesses")(Right(Some(Seq(
          businessData.copy(id = "testId2")
        ))))
        mockSaveSelfEmployments[Seq[SelfEmploymentData]](
          id = businessesKey,
          value = Seq(businessData.copy(id = "testId2"))
        )(Right(PostSelfEmploymentsHttpParser.PostSelfEmploymentsSuccessResponse))

        val result: Future[Result] = TestRemoveBusinessController.show(id)(FakeRequest())
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(routes.BusinessListCYAController.show().url)
      }
    }

    "return (303) redirect to how-do-you-receive-your-income page when Remove business has one business and click remove business" when {
      "the connector returns successful json" in {
        mockAuthSuccess()
        mockGetSelfEmployments[Seq[SelfEmploymentData]]("Businesses")(Right(Some(Seq(
          businessData
        ))))
        mockSaveSelfEmployments[Seq[SelfEmploymentData]](
          id = businessesKey,
          value = Seq()
        )(Right(PostSelfEmploymentsHttpParser.PostSelfEmploymentsSuccessResponse))

        val result: Future[Result] = TestRemoveBusinessController.show(id)(FakeRequest())
        status(result) mustBe 303
        redirectLocation(result).get must include("/report-quarterly/income-and-expenses/sign-up/details/income-receive")
      }
    }

    "return (303) redirect to business start date page" when {
      "no businesses are returned" in {
        mockAuthSuccess()
        mockGetSelfEmployments[Seq[SelfEmploymentData]]("Businesses")(Right(None))

        val result: Future[Result] = TestRemoveBusinessController.show(id)(FakeRequest())
        status(result) mustBe 303
        redirectLocation(result).get must include("/report-quarterly/income-and-expenses/sign-up/self-employments/details")
      }
      "no complete businesses are returned" in {
        mockAuthSuccess()
        mockGetSelfEmployments[Seq[SelfEmploymentData]]("Businesses")(Right(Some(Seq(
          businessData.copy(businessName = None)
        ))))

        val result: Future[Result] = TestRemoveBusinessController.show(id)(FakeRequest())
        status(result) mustBe 303
        redirectLocation(result) mustBe Some(routes.InitialiseController.initialise().url)
      }
    }

    "throw an internal server error" when {
      "there is an unexpected status failure when mockGetSelfEmployments returns error" in {
        mockAuthSuccess()
        mockGetSelfEmployments[Seq[SelfEmploymentData]]("Businesses")(Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))

        val response = intercept[InternalServerException](await(TestRemoveBusinessController.show(id)(FakeRequest())))
        response.message mustBe "[RemoveBusinessController][show] - getSelfEmployments connection failure, status: 500"
      }

      "there is an unexpected status failure when mockSaveSelfEmployments returns error" in {
        mockAuthSuccess()
        mockGetSelfEmployments[Seq[SelfEmploymentData]]("Businesses")(Right(Some(Seq(
          businessData
        ))))
        mockSaveSelfEmployments[Seq[SelfEmploymentData]](
          id = businessesKey,
          value = Seq()
        )(Left(PostSelfEmploymentsHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))

        val response = intercept[InternalServerException](await(TestRemoveBusinessController.show(id)(FakeRequest())))
        response.message mustBe "[RemoveBusinessController][show] - saveSelfEmployments failure, error: UnexpectedStatusFailure(500)"
      }

      "the connector returns an invalid Json" in {
        mockAuthSuccess()
        mockGetSelfEmployments[Seq[SelfEmploymentData]]("Businesses")(Left(InvalidJson))

        val response = intercept[InternalServerException](await(TestRemoveBusinessController.show(id)(FakeRequest())))
        response.message mustBe ("[RemoveBusinessController][show] - Invalid Json")
      }
    }

  }

  authorisationTests()

}
