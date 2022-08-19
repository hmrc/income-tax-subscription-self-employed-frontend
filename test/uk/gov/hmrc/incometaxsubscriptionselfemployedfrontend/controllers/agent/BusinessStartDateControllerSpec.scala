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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent

import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent}
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitchingTestUtils
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.UnexpectedStatusFailure
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSubscriptionDetailsSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.mocks.MockIncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.{ControllerBaseSpec, agent}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.BusinessStartDateForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.FormUtil._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{DateModel, BusinessStartDate => BusinessStartDateModel}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.mocks.MockMultipleSelfEmploymentsService
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ImplicitDateFormatter
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.TestModels._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.BusinessStartDate
import uk.gov.hmrc.play.language.LanguageUtils

class BusinessStartDateControllerSpec extends ControllerBaseSpec
  with MockMultipleSelfEmploymentsService
  with MockIncomeTaxSubscriptionConnector
  with ImplicitDateFormatter
  with FeatureSwitchingTestUtils {

  val businessStartDate: BusinessStartDate = mock[BusinessStartDate]
  val id: String = "testId"

  override val languageUtils: LanguageUtils = app.injector.instanceOf[LanguageUtils]
  override val controllerName: String = "BusinessStartDateController"
  override val authorisedRoutes: Map[String, Action[AnyContent]] = Map(
    "show" -> TestBusinessStartDateController.show(id, isEditMode = false),
    "submit" -> TestBusinessStartDateController.submit(id, isEditMode = false)
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(businessStartDate)
  }

  def mockBusinessStartDate(isEditMode: Boolean, backUrl: String): Unit = {
    when(businessStartDate(
      ArgumentMatchers.any(),
      ArgumentMatchers.any(),
      ArgumentMatchers.eq(isEditMode),
      ArgumentMatchers.eq(backUrl)
    )(any(), any(), any())) thenReturn HtmlFormat.empty
  }

  def businessStartDateForm(fill: Option[BusinessStartDateModel] = None, bind: Option[Map[String, String]] = None): Form[BusinessStartDateModel] = {
    val filledForm = BusinessStartDateForm.businessStartDateForm(
      minStartDate = BusinessStartDateForm.minStartDate,
      maxStartDate = BusinessStartDateForm.maxStartDate,
      d => d.toLongDate()
    ).fill(fill)
    bind match {
      case Some(data) => filledForm.bind(data)
      case None => filledForm
    }
  }

  object TestBusinessStartDateController extends BusinessStartDateController(
    mockMessagesControllerComponents,
    mockMultipleSelfEmploymentsService,
    mockIncomeTaxSubscriptionConnector,
    mockAuthService,
    mockLanguageUtils,
    businessStartDate
  )

  def modelToFormData(model: BusinessStartDateModel): Seq[(String, String)] = {
    BusinessStartDateForm.businessStartDateForm(BusinessStartDateForm.minStartDate, BusinessStartDateForm.maxStartDate, d => d.toString).fill(model).data.toSeq
  }

  "Show" should {
    "return ok (200)" when {
      "the connector returns data" in {
        val returnedModel: BusinessStartDateModel = BusinessStartDateModel(DateModel("01", "01", "2000"))

        mockAuthSuccess()
        mockFetchBusinessStartDate(id)(Right(Some(returnedModel)))
        mockBusinessStartDate(
          isEditMode = false,
          backUrl = routes.BusinessNameController.show(id).url
        )

        val result = TestBusinessStartDateController.show(id, isEditMode = false)(fakeRequest)

        status(result) mustBe OK
        contentType(result) mustBe Some(HTML)
      }
      "the connector returns no data" in {
        mockAuthSuccess()
        mockFetchBusinessStartDate(id)(Right(None))
        mockBusinessStartDate(
          isEditMode = false,
          backUrl = routes.BusinessNameController.show(id).url
        )

        val result = TestBusinessStartDateController.show(id, isEditMode = false)(fakeRequest)

        status(result) mustBe OK
        contentType(result) mustBe Some(HTML)
      }
      "the page is in edit mode" in {
        val returnedModel: BusinessStartDateModel = BusinessStartDateModel(DateModel("01", "01", "2000"))

        mockAuthSuccess()
        mockFetchBusinessStartDate(id)(Right(Some(returnedModel)))
        mockBusinessStartDate(
          isEditMode = true,
          backUrl = agent.routes.SelfEmployedCYAController.show(id, isEditMode = true).url
        )

        val result = TestBusinessStartDateController.show(id, isEditMode = true)(fakeRequest)

        status(result) mustBe OK
        contentType(result) mustBe Some(HTML)
      }
    }
    "Throw an internal exception error" when {
      "the connector returns an error fetching the business start date" in {
        mockAuthSuccess()
        mockFetchBusinessStartDate(id)(Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))
        intercept[InternalServerException](await(TestBusinessStartDateController.show(id, isEditMode = false)(fakeRequest)))
      }
    }

  }

  "Submit" when {
    "it is not in edit mode" should {
      "return 303, SEE_OTHER" when {
        "the user submits valid data" in {
          mockAuthSuccess()
          mockSaveBusinessStartDate(id, testBusinessStartDateModel)(Right(PostSubscriptionDetailsSuccessResponse))

          val result = TestBusinessStartDateController.submit(id, isEditMode = false)(
            fakeRequest.withFormUrlEncodedBody(modelToFormData(testBusinessStartDateModel): _*)
          )

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe
            Some(agent.routes.BusinessTradeNameController.show(id).url)
        }
      }
      "return 400, SEE_OTHER" when {
        "the user submits invalid data" in {
          mockAuthSuccess()
          mockSaveBusinessStartDate(id, testBusinessStartDateModel)(Right(PostSubscriptionDetailsSuccessResponse))
          mockBusinessStartDate(
            isEditMode = false,
            backUrl = routes.BusinessNameController.show(id).url
          )

          val result = TestBusinessStartDateController.submit(id, isEditMode = false)(fakeRequest)

          status(result) mustBe BAD_REQUEST
          contentType(result) mustBe Some(HTML)
        }
      }
    }
    "it is in edit mode" should {
      "return 303, SEE_OTHER" when {
        "the user submits valid data" in {
          mockAuthSuccess()
          mockSaveBusinessStartDate(id, testBusinessStartDateModel)(Right(PostSubscriptionDetailsSuccessResponse))

          val result = TestBusinessStartDateController.submit(id, isEditMode = true)(
            fakeRequest.withFormUrlEncodedBody(modelToFormData(testBusinessStartDateModel): _*)
          )

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe
            Some(routes.SelfEmployedCYAController.show(id, isEditMode = true).url)
        }
      }
      "return 400, SEE_OTHER" when {
        "the user submits invalid data" in {
          mockAuthSuccess()
          mockSaveBusinessStartDate(id, testBusinessStartDateModel)(Right(PostSubscriptionDetailsSuccessResponse))
          mockBusinessStartDate(
            isEditMode = true,
            backUrl = routes.SelfEmployedCYAController.show(id, isEditMode = true).url
          )

          val result = TestBusinessStartDateController.submit(id, isEditMode = true)(fakeRequest)

          status(result) mustBe BAD_REQUEST
          contentType(result) mustBe Some(HTML)
        }
      }
    }
  }

  "The back url" when {
    "in edit mode" should {
      s"redirect to ${routes.SelfEmployedCYAController.show(id, isEditMode = true).url}" in {
        TestBusinessStartDateController.backUrl(id, isEditMode = true) mustBe routes.SelfEmployedCYAController.show(id, isEditMode = true).url
      }
    }
    "not in edit mode" should {
      s"redirect to ${routes.BusinessNameController.show(id).url}" in {
        TestBusinessStartDateController.backUrl(id, isEditMode = false) mustBe routes.BusinessNameController.show(id).url
      }
    }
  }

}
