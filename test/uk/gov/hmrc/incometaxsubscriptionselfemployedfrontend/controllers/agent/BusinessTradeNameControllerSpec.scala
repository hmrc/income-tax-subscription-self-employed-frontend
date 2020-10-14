/*
 * Copyright 2020 HM Revenue & Customs
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

import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.UnexpectedStatusFailure
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSelfEmploymentsSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.mocks.MockIncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.ControllerBaseSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.BusinessTradeNameForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{Address, BusinessAddressModel, BusinessNameModel, BusinessStartDate, BusinessTradeNameModel, DateModel, SelfEmploymentData}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.mocks.MockMultipleSelfEmploymentsService
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.TestModels.{testInvalidBusinessTradeNameModel, testValidBusinessTradeNameModel}

class BusinessTradeNameControllerSpec extends ControllerBaseSpec
  with MockMultipleSelfEmploymentsService {

  val id: String = "testId"


  override val controllerName: String = "BusinessTradeNameController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map(
    "show" -> TestBusinessTradeNameController.show(id,isEditMode = false),
    "submit" -> TestBusinessTradeNameController.submit(id,isEditMode = false)
  )

  object TestBusinessTradeNameController extends BusinessTradeNameController(
    mockMessagesControllerComponents,
    mockMultipleSelfEmploymentsService,
    mockAuthService
  )

  def modelToFormData(businessTradeNameModel: BusinessTradeNameModel): Seq[(String, String)] = {
    BusinessTradeNameForm.businessTradeNameValidationForm(Nil).fill(businessTradeNameModel).data.toSeq
  }

  val selfEmploymentData: SelfEmploymentData = SelfEmploymentData(
    id = id,
    businessStartDate = Some(BusinessStartDate(DateModel("1", "1", "1"))),
    businessName = Some(BusinessNameModel("testName")),
    businessTradeName = Some(BusinessTradeNameModel("testTrade")),
    businessAddress = Some(BusinessAddressModel("12345", Address(Seq("line1"), "TF3 4NT")))
  )

  "Show" should {

    "return ok (200)" when {
      "the connector returns data" in {
        mockAuthSuccess()
        mockFetchAllBusinesses(Right(Seq(selfEmploymentData)))

        val result = TestBusinessTradeNameController.show(id,isEditMode = false)(FakeRequest())

        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
      }
      "the connector returns no data" in {
        mockAuthSuccess()
        mockFetchAllBusinesses(Right(Seq(selfEmploymentData.copy(businessTradeName = None))))

        val result = TestBusinessTradeNameController.show(id,isEditMode = false)(FakeRequest())

        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
      }
    }
    "Throw an internal exception error" when {
      "the connector returns an error" in {
        mockAuthSuccess()
        mockFetchAllBusinesses(Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))
        intercept[InternalServerException](await(TestBusinessTradeNameController.show(id,isEditMode = false)(FakeRequest())))
      }
    }

    "return see other (303)" when {
      "the connector returns data for the current business but the name is not present" in {
        mockAuthSuccess()
        mockFetchAllBusinesses(Right(Seq(selfEmploymentData.copy(businessName = None, businessTradeName = None))))

        val result = TestBusinessTradeNameController.show(id, isEditMode = false)(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.BusinessNameController.show(id).url)
      }
    }
    "Throw an internal server exception error" when {
      "the connector returns an error" in {
        mockAuthSuccess()
        mockFetchAllBusinesses(Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))

        intercept[InternalServerException](await(TestBusinessTradeNameController.show(id, isEditMode = false)(FakeRequest())))
      }
    }

  }

  "Submit when not in edit mode" should {

    "return 303, SEE_OTHER)" when {
      "the user submits valid data" in {
        mockAuthSuccess()
        mockFetchAllBusinesses(Right(Seq(selfEmploymentData.copy(businessTradeName = None))))
        mockSaveBusinessTrade(id, testValidBusinessTradeNameModel)(Right(PostSelfEmploymentsSuccessResponse))

        val result = TestBusinessTradeNameController.submit(id,isEditMode = false)(
          FakeRequest().withFormUrlEncodedBody(modelToFormData(testValidBusinessTradeNameModel): _*)
        )

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.AddressLookupRoutingController.initialiseAddressLookupJourney(id).url)
      }
    }
    "return 400, SEE_OTHER)" when {
      "the user submits invalid data" in {
        mockAuthSuccess()
        mockFetchAllBusinesses(Right(Seq(selfEmploymentData.copy(businessTradeName = None))))
        mockSaveBusinessTrade(id, testInvalidBusinessTradeNameModel)(Right(PostSelfEmploymentsSuccessResponse))

        val result = TestBusinessTradeNameController.submit(id,isEditMode = false)(FakeRequest())

        status(result) mustBe BAD_REQUEST
        contentType(result) mustBe Some("text/html")
      }
      "the user submits a trade which causes a duplicate business name/trade combo" in {
        mockAuthSuccess()
        mockFetchAllBusinesses(Right(Seq(
          selfEmploymentData.copy(
            id = "idOne",
            businessName = Some(BusinessNameModel("nameOne")),
            businessTradeName = Some(BusinessTradeNameModel("tradeOne"))
          ),
          selfEmploymentData.copy(
            id = "idTwo",
            businessName = Some(BusinessNameModel("nameOne")),
            businessTradeName = None
          ))))

        val result = TestBusinessTradeNameController.submit("idTwo", isEditMode = false)(
          FakeRequest().withFormUrlEncodedBody(modelToFormData(BusinessTradeNameModel("tradeOne")): _*)
        )

        status(result) mustBe BAD_REQUEST
        contentType(result) mustBe Some("text/html")
      }
    }
  }

  "Submit when in edit mode" should {

    "return 303, SEE_OTHER)" when {
      "the user submits valid data in edit mode" in {
        mockAuthSuccess()
        mockFetchAllBusinesses(Right(Seq(selfEmploymentData)))
        mockSaveBusinessTrade(id, testValidBusinessTradeNameModel)(Right(PostSelfEmploymentsSuccessResponse))

        val result = TestBusinessTradeNameController.submit(id,isEditMode = true)(
          FakeRequest().withFormUrlEncodedBody(modelToFormData(testValidBusinessTradeNameModel): _*)
        )
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.BusinessListCYAController.show().url)

      }
      "the user does not update their trade in edit mode" in {
        mockAuthSuccess()
        mockFetchAllBusinesses(
          Right(Seq(selfEmploymentData))
        )
        mockSaveBusinessTrade(id, testValidBusinessTradeNameModel)(Right(PostSelfEmploymentsSuccessResponse))

        val result = TestBusinessTradeNameController.submit(id, isEditMode = true)(
          FakeRequest().withFormUrlEncodedBody(modelToFormData(testValidBusinessTradeNameModel): _*)
        )

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.BusinessListCYAController.show().url)
      }
    }
    "return 400, SEE_OTHER)" when {
      "the user submits invalid data" in {
        mockAuthSuccess()
        mockFetchAllBusinesses(Right(Seq(selfEmploymentData)))
        mockSaveBusinessTrade(id, testValidBusinessTradeNameModel)(Right(PostSelfEmploymentsSuccessResponse))

        val result = TestBusinessTradeNameController.submit(id,isEditMode = true)(FakeRequest())

        status(result) mustBe BAD_REQUEST
        contentType(result) mustBe Some("text/html")
      }
    }
  }

  "Submit when not in edit mode" should {

    "return 303, SEE_OTHER)" when {
      "the user submits valid data in non-edit mode" in {
        mockAuthSuccess()
        mockFetchAllBusinesses(Right(Seq(selfEmploymentData)))
        mockSaveBusinessTrade(id, testValidBusinessTradeNameModel)(Right(PostSelfEmploymentsSuccessResponse))

        val result = TestBusinessTradeNameController.submit(id, isEditMode = false)(
          FakeRequest().withFormUrlEncodedBody(modelToFormData(testValidBusinessTradeNameModel): _*)
        )
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.AddressLookupRoutingController.initialiseAddressLookupJourney(id).url)

      }
      "the user does not update their trade in non-edit mode" in {
        mockAuthSuccess()
        mockFetchAllBusinesses(
          Right(Seq(selfEmploymentData))
        )
        mockSaveBusinessTrade(id, testValidBusinessTradeNameModel)(Right(PostSelfEmploymentsSuccessResponse))

        val result = TestBusinessTradeNameController.submit(id, isEditMode = false)(
          FakeRequest().withFormUrlEncodedBody(modelToFormData(testValidBusinessTradeNameModel): _*)
        )

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.AddressLookupRoutingController.initialiseAddressLookupJourney(id).url)
      }
    }
    "return 400, SEE_OTHER)" when {
      "the user submits invalid data when in non-edit mode" in {
        mockAuthSuccess()
        mockFetchAllBusinesses(Right(Seq(selfEmploymentData)))
        mockSaveBusinessTrade(id, testValidBusinessTradeNameModel)(Right(PostSelfEmploymentsSuccessResponse))

        val result = TestBusinessTradeNameController.submit(id,isEditMode = false)(FakeRequest())

        status(result) mustBe BAD_REQUEST
        contentType(result) mustBe Some("text/html")
      }
    }
  }

  "The back url" when {
    "in edit mode" should {
      s"redirect to ${routes.BusinessListCYAController.show().url}" in {
        TestBusinessTradeNameController.backUrl(id,isEditMode = true) mustBe routes.BusinessListCYAController.show().url
      }
    }
    "not in edit mode" should {
      s"redirect to ${routes.BusinessNameController.show(id).url}" in {
        TestBusinessTradeNameController.backUrl(id,isEditMode = false) mustBe routes.BusinessNameController.show(id).url
      }
    }
  }

  authorisationTests()

}
