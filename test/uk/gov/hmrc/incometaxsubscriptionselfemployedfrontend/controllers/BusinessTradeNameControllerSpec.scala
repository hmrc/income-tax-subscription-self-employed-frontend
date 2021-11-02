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

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.mvc.{Action, AnyContent}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitch.SaveAndRetrieve
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.UnexpectedStatusFailure
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSelfEmploymentsSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessTradeNameForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.mocks.MockMultipleSelfEmploymentsService
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.TestModels._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.BusinessTradeName

class BusinessTradeNameControllerSpec extends ControllerBaseSpec
  with MockMultipleSelfEmploymentsService with FeatureSwitching {

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(SaveAndRetrieve)
  }

  val id: String = "testId"

  override val controllerName: String = "BusinessTradeNameController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map()

  private def withController(testCode: BusinessTradeNameController => Any): Unit = {
    val businessTradeNameView = mock[BusinessTradeName]

    when(businessTradeNameView(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
    (ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(HtmlFormat.empty)

    val controller = new BusinessTradeNameController(
      mockMessagesControllerComponents,
      businessTradeNameView,
      mockMultipleSelfEmploymentsService,
      mockAuthService
    )

    testCode(controller)
  }

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
      "the connector returns data" in withController { controller =>
        mockAuthSuccess()
        mockFetchAllBusinesses(Right(Seq(selfEmploymentData)))

        val result = controller.show(id, isEditMode = false)(FakeRequest())


        status(result) mustBe OK
        contentType(result) mustBe Some("text/html")
      }
    }

    "the connector returns data for the current business but with no trade" in withController { controller => {
      mockAuthSuccess()
      mockFetchAllBusinesses(Right(Seq(selfEmploymentData.copy(businessTradeName = None))))


      val result = controller.show(id, isEditMode = false)(FakeRequest())

      status(result) mustBe OK
      contentType(result) mustBe Some("text/html")
    }
    }

    "return see other (303)" when {
      "the connector returns data for the current business but the name is not present" in withController { controller => {
        mockAuthSuccess()
        mockFetchAllBusinesses(Right(Seq(selfEmploymentData.copy(businessName = None, businessTradeName = None))))


        val result = controller.show(id, isEditMode = false)(FakeRequest())

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.BusinessNameController.show(id).url)
      }
      }
    }

    "throw an internal exception error" when {
      "the connector returns an error" in withController { controller => {
        mockAuthSuccess()
        mockFetchAllBusinesses(Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))


        intercept[InternalServerException](await(controller.show(id, isEditMode = false)(FakeRequest())))
      }
      }
    }

  }

  "Submit - it is not in edit mode" should {

    "return 303, SEE_OTHER" when {
      "the user submits valid data" in withController { controller => {
        mockAuthSuccess()
        mockFetchAllBusinesses(Right(Seq(selfEmploymentData.copy(businessTradeName = None))))
        mockSaveBusinessTrade(id, testValidBusinessTradeNameModel)(Right(PostSelfEmploymentsSuccessResponse))


        val result = controller.submit(id, isEditMode = false)(
          FakeRequest().withFormUrlEncodedBody(modelToFormData(testValidBusinessTradeNameModel): _*)
        )

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.AddressLookupRoutingController.initialiseAddressLookupJourney(id).url)
      }
      }
    }

    "return 400, SEE_OTHER)" when {
      "the user submits invalid data" in withController { controller => {
        mockAuthSuccess()
        mockFetchAllBusinesses(Right(Seq(selfEmploymentData.copy(businessTradeName = None))))
        mockSaveBusinessTrade(id, testInvalidBusinessTradeNameModel)(Right(PostSelfEmploymentsSuccessResponse))


        val result = controller.submit(id, isEditMode = false)(FakeRequest())

        status(result) mustBe BAD_REQUEST
        contentType(result) mustBe Some("text/html")
      }
      }
      "the user submits a trade which causes a duplicate business name/trade combo" in withController { controller => {
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


        val result = controller.submit("idTwo", isEditMode = false)(
          FakeRequest().withFormUrlEncodedBody(modelToFormData(BusinessTradeNameModel("tradeOne")): _*)
        )

        status(result) mustBe BAD_REQUEST
        contentType(result) mustBe Some("text/html")
      }
      }
    }
  }

  "Submit - it is in edit mode" should {

    s"return a redirect to '${routes.BusinessListCYAController.show().url}" when {
      "the user submits valid data" in withController { controller => {
        mockAuthSuccess()
        mockFetchAllBusinesses(Right(Seq(selfEmploymentData)))
        mockSaveBusinessTrade(id, testValidBusinessTradeNameModel)(Right(PostSelfEmploymentsSuccessResponse))


        val result = controller.submit(id, isEditMode = true)(
          FakeRequest().withFormUrlEncodedBody(modelToFormData(testValidBusinessTradeNameModel): _*)
        )

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.BusinessListCYAController.show().url)
      }
      }
      "the user does not update their trade" in withController { controller => {
        mockAuthSuccess()
        mockFetchAllBusinesses(
          Right(Seq(selfEmploymentData))
        )
        mockSaveBusinessTrade(id, testValidBusinessTradeNameModel)(Right(PostSelfEmploymentsSuccessResponse))
        val result = controller.submit(id, isEditMode = true)(
          FakeRequest().withFormUrlEncodedBody(modelToFormData(testValidBusinessTradeNameModel): _*)
        )


        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.BusinessListCYAController.show().url)
      }
      }
    }
  }

  "Submit - it is not in edit mode" should {

    "return a redirect to address look up" when {
      "the user submits valid data" in withController { controller => {
        mockAuthSuccess()
        mockFetchAllBusinesses(Right(Seq(selfEmploymentData)))
        mockSaveBusinessTrade(id, testValidBusinessTradeNameModel)(Right(PostSelfEmploymentsSuccessResponse))


        val result = controller.submit(id, isEditMode = false)(
          FakeRequest().withFormUrlEncodedBody(modelToFormData(testValidBusinessTradeNameModel): _*)
        )

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.AddressLookupRoutingController.initialiseAddressLookupJourney(id).url)
      }
      }
      "the user does not update their trade" in withController { controller => {
        mockAuthSuccess()
        mockFetchAllBusinesses(
          Right(Seq(selfEmploymentData))
        )
        mockSaveBusinessTrade(id, testValidBusinessTradeNameModel)(Right(PostSelfEmploymentsSuccessResponse))
        val result = controller.submit(id, isEditMode = false)(
          FakeRequest().withFormUrlEncodedBody(modelToFormData(testValidBusinessTradeNameModel): _*)
        )


        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.AddressLookupRoutingController.initialiseAddressLookupJourney(id).url)
      }
      }
    }
  }

  "The back url" when {
    "in edit mode" should {
      s"redirect to ${routes.BusinessListCYAController.show().url}" in withController { controller => {
        controller.backUrl(id, isEditMode = true) mustBe routes.BusinessListCYAController.show().url
      }
      }
      "not in edit mode" when {
        "save and retrive is enabled" should {
          s"redirect to ${routes.BusinessNameController.show(id).url}" in withController { controller =>
            enable(SaveAndRetrieve)
            controller.backUrl(id, isEditMode = false) mustBe routes.BusinessStartDateController.show(id).url
          }
        }

        "save and retrive is disabled" should {
          s"redirect to ${routes.BusinessNameController.show(id).url}" in withController { controller =>
            controller.backUrl(id, isEditMode = false) mustBe routes.BusinessNameController.show(id).url
          }
        }
      }

    }
  }

  authorisationTests()

}
