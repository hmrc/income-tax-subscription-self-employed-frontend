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

import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.UnexpectedStatusFailure
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessNameForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.mocks.MockMultipleSelfEmploymentsService
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.TestModels._

class BusinessNameControllerSpec extends ControllerBaseSpec
  with MockMultipleSelfEmploymentsService {

  val id: String = "testId"

  override val controllerName: String = "BusinessNameController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map(
    "show" -> TestBusinessNameController.show(id, isEditMode = false),
    "submit" -> TestBusinessNameController.submit(id, isEditMode = false)
  )

  object TestBusinessNameController extends BusinessNameController(
    mockMessagesControllerComponents,
    mockMultipleSelfEmploymentsService,
    mockAuthService
  )

  val selfEmploymentData: SelfEmploymentData = SelfEmploymentData(
    id = id,
    businessStartDate = Some(BusinessStartDate(DateModel("1", "1", "1"))),
    businessName = Some(BusinessNameModel("testName")),
    businessTradeName = Some(BusinessTradeNameModel("testTrade"))
  )

  def modelToFormData(businessNameModel: BusinessNameModel): Seq[(String, String)] = {
    BusinessNameForm.businessNameValidationForm(Nil).fill(businessNameModel).data.toSeq
  }

  "show" should {
    "return ok (200)" when {
      "the connector returns data for the current business" in {
        mockAuthSuccess()
        mockFetchAllBusinesses(
          Right(Seq(selfEmploymentData))
        )
        val result = TestBusinessNameController.show(id, isEditMode = false)(FakeRequest())
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
      }
      "the connector returns data for the current business but with no business name" in {
        mockAuthSuccess()
        mockFetchAllBusinesses(
          Right(Seq(selfEmploymentData.copy(businessName = None)))
        )
        val result = TestBusinessNameController.show(id, isEditMode = false)(FakeRequest())
        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
      }
    }
    "Throw an internal exception error" when {
      "the connector returns an error" in {
        mockAuthSuccess()
        mockFetchAllBusinesses(
          Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR))
        )
        intercept[InternalServerException](await(TestBusinessNameController.show(id, isEditMode = false)(FakeRequest())))
      }

    }
  }

  "submit" when {
    "not in edit mode" should {
      s"return $SEE_OTHER" when {
        "the users input is valid and is saved" in {
          mockAuthSuccess()
          mockFetchAllBusinesses(
            Right(Seq(selfEmploymentData.copy(businessName = None, businessTradeName = None)))
          )
          mockSaveBusinessName(id, mockBusinessNameModel)(Right(PostSelfEmploymentsSuccessResponse))
          val result = TestBusinessNameController.submit(id, isEditMode = false)(
            FakeRequest().withFormUrlEncodedBody(modelToFormData(mockBusinessNameModel): _*)
          )
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.BusinessTradeNameController.show(id).url)
        }
      }
      s"return $BAD_REQUEST" when {
        "the user submits invalid data" in {
          mockAuthSuccess()
          mockFetchAllBusinesses(
            Right(Seq(selfEmploymentData.copy(businessName = None, businessTradeName = None)))
          )
          val result = TestBusinessNameController.submit(id, isEditMode = false)(FakeRequest())
          status(result) mustBe BAD_REQUEST
          contentType(result) mustBe Some("text/html")
        }
        "the user enters a business name which would cause a duplicate business name / trade combination" in {
          mockAuthSuccess()
          mockFetchAllBusinesses(
            Right(Seq(
              selfEmploymentData.copy(
                id = "idOne",
                businessName = Some(BusinessNameModel("nameOne")),
                businessTradeName = Some(BusinessTradeNameModel("tradeOne"))
              ),
              selfEmploymentData.copy(
                id = "idTwo",
                businessName = None,
                businessTradeName = Some(BusinessTradeNameModel("tradeOne"))
              )
            ))
          )
          val result = TestBusinessNameController.submit("idTwo", isEditMode = false)(
            FakeRequest().withFormUrlEncodedBody(modelToFormData(BusinessNameModel("nameOne")): _*)
          )
          status(result) mustBe BAD_REQUEST
          contentType(result) mustBe Some("text/html")
        }
      }
      "throw an exception" when {
        "an error is returned when retrieving all businesses" in {
          mockAuthSuccess()
          mockFetchAllBusinesses(
            Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR))
          )

          intercept[InternalServerException](await(TestBusinessNameController.submit(id, isEditMode = false)(FakeRequest())))
        }
      }
    }
    "in edit mode" should {
      s"return $SEE_OTHER" when {
        "the users answer is updated correctly" in {
          mockAuthSuccess()
          mockFetchAllBusinesses(
            Right(Seq(selfEmploymentData.copy(businessName = Some(BusinessNameModel("nameOne")))))
          )
          mockSaveBusinessName(id, mockBusinessNameModel)(Right(PostSelfEmploymentsSuccessResponse))
          val result = TestBusinessNameController.submit(id, isEditMode = true)(
            FakeRequest().withFormUrlEncodedBody(modelToFormData(mockBusinessNameModel): _*)
          )
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.BusinessListCYAController.show().url)
        }
        "the user does not update their answer" in {
          mockAuthSuccess()
          mockFetchAllBusinesses(
            Right(Seq(selfEmploymentData))
          )
          mockSaveBusinessName(id, mockBusinessNameModel)(Right(PostSelfEmploymentsSuccessResponse))
          val result = TestBusinessNameController.submit(id, isEditMode = true)(
            FakeRequest().withFormUrlEncodedBody(modelToFormData(mockBusinessNameModel): _*)
          )
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.BusinessListCYAController.show().url)
        }
      }
    }

  }

  "The back url" when {
    "in edit mode" should {
      s"redirect to ${routes.BusinessListCYAController.show().url}" in {
        TestBusinessNameController.backUrl(id, isEditMode = true) mustBe routes.BusinessListCYAController.show().url
      }
    }
    "not in edit mode" should {
      s"redirect to ${routes.BusinessStartDateController.show(id).url}" in {
        TestBusinessNameController.backUrl(id, isEditMode = false) mustBe routes.BusinessStartDateController.show(id).url
      }
    }
  }

  authorisationTests()

}
