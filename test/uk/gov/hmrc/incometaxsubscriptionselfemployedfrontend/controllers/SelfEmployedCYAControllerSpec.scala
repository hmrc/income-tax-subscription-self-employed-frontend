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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.businessAccountingMethodKey
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitchingTestUtils
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.UnexpectedStatusFailure
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSubscriptionDetailsSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.mocks.MockIncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.mocks.MockMultipleSelfEmploymentsService
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.mocks.individual.MockSelfEmployedCYA

import scala.concurrent.Future

class SelfEmployedCYAControllerSpec extends ControllerBaseSpec
  with MockMultipleSelfEmploymentsService with MockIncomeTaxSubscriptionConnector with FeatureSwitchingTestUtils with MockSelfEmployedCYA {

  val id: String = "testId"

  override val controllerName: String = "SelfEmployedCYAController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map(
    "show" -> TestSelfEmployedCYAController.show(id, isEditMode = false),
    "submit" -> TestSelfEmployedCYAController.submit(id)
  )

  object TestSelfEmployedCYAController extends SelfEmployedCYAController(
    selfEmployedCYA,
    mockAuthService,
    mockIncomeTaxSubscriptionConnector,
    mockMultipleSelfEmploymentsService,
    mockMessagesControllerComponents
  )

  val selfEmployment: SelfEmploymentData = SelfEmploymentData(
    id = id,
    businessStartDate = Some(BusinessStartDate(DateModel("1", "1", "1980"))),
    businessName = Some(BusinessNameModel("testBusinessName")),
    businessTradeName = Some(BusinessTradeNameModel("testBusinessTrade")),
    businessAddress = Some(BusinessAddressModel(auditRef = "testAuditRef", address = Address(lines = Seq("line 1"), postcode = Some("ZZ1 1ZZ")))),
    addressRedirect = Some("/test-redirect")
  )

  val incompleteSelfEmployment: SelfEmploymentData = SelfEmploymentData(
    id = id,
    businessStartDate = Some(BusinessStartDate(DateModel("1", "1", "1980"))),
    businessName = Some(BusinessNameModel("testBusinessName")),
    businessTradeName = None,
    businessAddress = Some(BusinessAddressModel(auditRef = "testAuditRef", address = Address(lines = Seq("line 1"), postcode = Some("ZZ1 1ZZ")))),
    addressRedirect = Some("/test-redirect")
  )

  "show" when {
    "throw an internal server exception" when {
      "there was a problem retrieving the users accounting method" in {
        mockAuthSuccess()
        mockGetSelfEmployments(businessAccountingMethodKey)(Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))

        intercept[InternalServerException](await(TestSelfEmployedCYAController.show(id, isEditMode = false)(fakeRequest)))
          .message mustBe "[SelfEmployedCYAController][withSelfEmploymentCYAModel] - Failure retrieving accounting method"
      }
      "there was a problem retrieving the users self employment data" in {
        mockAuthSuccess()
        mockGetSelfEmployments[AccountingMethodModel](businessAccountingMethodKey)(Right(Some(AccountingMethodModel(Cash))))
        mockFetchBusiness(id)(Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))

        intercept[InternalServerException](await(TestSelfEmployedCYAController.show(id, isEditMode = false)(fakeRequest)))
          .message mustBe "[SelfEmployedCYAController][withSelfEmploymentCYAModel] - Failure retrieving self employment data"
      }
    }
    "return OK" when {
      "all data is retrieved successfully" in {
        mockAuthSuccess()
        mockGetSelfEmployments[AccountingMethodModel](businessAccountingMethodKey)(Right(Some(AccountingMethodModel(Cash))))
        mockFetchBusiness(id)(Right(Some(selfEmployment)))
        mockSelfEmployedCYA()

        val result: Future[Result] = TestSelfEmployedCYAController.show(id, isEditMode = false)(fakeRequest)

        status(result) mustBe OK
        contentType(result) mustBe Some(HTML)
      }
    }
  }

  "submit" when {
    "throw an internal server exception" when {
      "there was a problem retrieving the users accounting method" in {
        mockAuthSuccess()
        mockGetSelfEmployments(businessAccountingMethodKey)(Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))

        intercept[InternalServerException](await(TestSelfEmployedCYAController.show(id, isEditMode = false)(fakeRequest)))
          .message mustBe "[SelfEmployedCYAController][withSelfEmploymentCYAModel] - Failure retrieving accounting method"
      }
      "there was a problem retrieving the users self employment data" in {
        mockGetSelfEmployments[AccountingMethodModel](businessAccountingMethodKey)(Right(Some(AccountingMethodModel(Cash))))
        mockFetchBusiness(id)(Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))

        intercept[InternalServerException](await(TestSelfEmployedCYAController.show(id, isEditMode = false)(fakeRequest)))
          .message mustBe "[SelfEmployedCYAController][withSelfEmploymentCYAModel] - Failure retrieving self employment data"
      }
    }

    "return 303, (SEE_OTHER)" when {
      "the user submits valid full data" in {
        mockGetSelfEmployments[AccountingMethodModel](businessAccountingMethodKey)(Right(Some(AccountingMethodModel(Cash))))
        mockFetchBusiness(id)(Right(Some(selfEmployment)))
        mockConfirmBusiness(id)(Right(PostSubscriptionDetailsSuccessResponse))

        val result: Future[Result] = TestSelfEmployedCYAController.submit(id)(fakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(appConfig.taskListUrl)
      }
      "the user submits valid incomplete data" in {
        mockGetSelfEmployments[AccountingMethodModel](businessAccountingMethodKey)(Right(Some(AccountingMethodModel(Cash))))
        mockFetchBusiness(id)(Right(Some(incompleteSelfEmployment)))
      }

      "return 303, (SEE_OTHER) and redirect to the task list page" when {
        "the user submits valid full data" in {
          mockGetSelfEmployments[AccountingMethodModel](businessAccountingMethodKey)(Right(Some(AccountingMethodModel(Cash))))
          mockFetchBusiness(id)(Right(Some(selfEmployment)))
          mockConfirmBusiness(id)(Right(PostSubscriptionDetailsSuccessResponse))

          val result: Future[Result] = TestSelfEmployedCYAController.submit(id)(fakeRequest)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(appConfig.taskListUrl)
        }
        "the user submits valid incomplete data" in {
          mockGetSelfEmployments[AccountingMethodModel](businessAccountingMethodKey)(Right(Some(AccountingMethodModel(Cash))))
          mockFetchBusiness(id)(Right(Some(incompleteSelfEmployment)))

          val result: Future[Result] = TestSelfEmployedCYAController.submit(id)(fakeRequest)
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(appConfig.taskListUrl)
        }
      }
    }
  }

  "backUrl" should {
    "return the task list page" when {
      "in edit mode" in {
        TestSelfEmployedCYAController.backUrl(true) mustBe Some(appConfig.taskListUrl)
      }
    }
    "return nothing" when {
      "not in edit mode" in {
        TestSelfEmployedCYAController.backUrl(false) mustBe None
      }
    }
  }

}