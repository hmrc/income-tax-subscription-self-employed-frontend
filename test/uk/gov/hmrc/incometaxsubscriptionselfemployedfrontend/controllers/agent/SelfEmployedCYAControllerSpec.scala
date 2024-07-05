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

import play.api.mvc.{Action, AnyContent, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.UnexpectedStatusFailure
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSubscriptionDetailsSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.ControllerBaseSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.mocks.{MockClientDetailsRetrieval, MockMultipleSelfEmploymentsService, MockSessionDataService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.mocks.agent.MockSelfEmployedCYA

import scala.concurrent.Future

class SelfEmployedCYAControllerSpec extends ControllerBaseSpec
  with MockMultipleSelfEmploymentsService with MockSessionDataService with MockSelfEmployedCYA with MockClientDetailsRetrieval {

  val id: String = "testId"

  override val controllerName: String = "SelfEmployedCYAController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map(
    "show" -> TestSelfEmployedCYAController.show(id, isEditMode = false),
    "submit" -> TestSelfEmployedCYAController.submit(id)
  )

  object TestSelfEmployedCYAController extends SelfEmployedCYAController(
    selfEmployedCYA,
    mockClientDetailsRetrieval,
    mockAuthService,
    mockMultipleSelfEmploymentsService,
    mockMessagesControllerComponents
  )(
    mockSessionDataService,
    appConfig
  )

  val soleTraderBusinesses: SoleTraderBusinesses = SoleTraderBusinesses(
    businesses = Seq(
      SoleTraderBusiness(
        id = id,
        startDate = Some(DateModel("1", "1", "1980")),
        name = Some("testBusinessName"),
        trade = Some("testBusinessTrade"),
        address = Some(Address(lines = Seq("line 1"), postcode = Some("ZZ1 1ZZ")))
      )
    ),
    accountingMethod = Some(Cash)
  )

  "show" should {
    "throw an internal server exception" when {
      "there was a problem retrieving the users sole trader businesses" in {
        mockAuthSuccess()
        mockFetchSoleTraderBusinesses(Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))

        intercept[InternalServerException](await(TestSelfEmployedCYAController.show(id, isEditMode = false)(fakeRequest)))
          .message mustBe "[SelfEmployedCYAController][fetchBusinessListAndAccountingMethod] - Failed to retrieve sole trader businesses"
      }
    }
    "return OK" when {
      "all data is retrieved successfully" in {
        mockAuthSuccess()
        mockFetchSoleTraderBusinesses(Right(Some(soleTraderBusinesses)))
        mockSelfEmployedCYA()

        val result: Future[Result] = TestSelfEmployedCYAController.show(id, isEditMode = false)(fakeRequest)

        status(result) mustBe OK
        contentType(result) mustBe Some(HTML)
      }
    }
  }

  "submit" should {
    "throw an internal server exception" when {
      "there was a problem retrieving the users sole trader businesses" in {
        mockAuthSuccess()
        mockFetchSoleTraderBusinesses(Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))

        intercept[InternalServerException](await(TestSelfEmployedCYAController.show(id, isEditMode = false)(fakeRequest)))
          .message mustBe "[SelfEmployedCYAController][fetchBusinessListAndAccountingMethod] - Failed to retrieve sole trader businesses"
      }
    }
    "return 303 (SEE_OTHER) to the your income sources page" when {
      "the user submits valid full data" in {
        mockFetchSoleTraderBusinesses(Right(Some(soleTraderBusinesses)))
        mockConfirmBusiness(id)(Right(PostSubscriptionDetailsSuccessResponse))

        val result: Future[Result] = TestSelfEmployedCYAController.submit(id)(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(appConfig.clientYourIncomeSourcesUrl)
      }
      "the user submits valid incomplete data" in {
        mockFetchSoleTraderBusinesses(Right(Some(soleTraderBusinesses.copy(accountingMethod = None))))

        val result: Future[Result] = TestSelfEmployedCYAController.submit(id)(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(appConfig.clientYourIncomeSourcesUrl)
      }
    }
  }

  "backUrl" when {
    "in edit mode" should {
      "return the your income source page" in {
        TestSelfEmployedCYAController.backUrl(true) mustBe Some(appConfig.clientYourIncomeSourcesUrl)
      }
    }
    "not in edit mode" should {
      "return no back url" in {
        TestSelfEmployedCYAController.backUrl(false) mustBe None
      }
    }
  }


}
