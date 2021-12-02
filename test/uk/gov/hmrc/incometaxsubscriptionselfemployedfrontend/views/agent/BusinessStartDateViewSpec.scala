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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.agent

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.api.data.FormError
import play.twirl.api.Html
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.BusinessStartDateForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ViewSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.BusinessStartDate

class BusinessStartDateViewSpec extends ViewSpec {

  object BusinessStartDateMessages {
    val heading: String = "When did your client’s sole trader business start trading?"
    val exampleStartDate = "For example, 1 8 2014"
    val continue = "Continue"
    val update = "Update"
  }

  val taxYearEnd: Int = 2020

  val businessStartDateView: BusinessStartDate = app.injector.instanceOf[BusinessStartDate]

  def page(isEditMode: Boolean = false, error: Option[FormError] = None): Html = {
    val form = BusinessStartDateForm.businessStartDateForm("minStartDateError", "maxStartDateError")
    businessStartDateView(
      startDateForm = error match {
        case Some(value) => form.withError(value)
        case None => form
      },
      postAction = testCall,
      isEditMode = isEditMode,
      backUrl = testBackUrl
    )(fakeTestRequest, implicitly)
  }

  def document(isEditMode: Boolean = false, error: Option[FormError] = None): Document = Jsoup.parse(page(isEditMode, error).body)

  val testError: FormError = FormError(BusinessStartDateForm.startDate, "test error message")

  "BusinessStartDate" must {
    "have the correct template details" when {
      "there is no error on the page" in new TemplateViewTest(
        view = page(),
        title = BusinessStartDateMessages.heading,
        isAgent = true,
        backLink = Some(testBackUrl),
        hasSignOutLink = true
      )
      "there is an error on the page" in new TemplateViewTest(
        view = page(error = Some(testError)),
        title = BusinessStartDateMessages.heading,
        isAgent = true,
        backLink = Some(testBackUrl),
        hasSignOutLink = true,
        error = Some(testError)
      )
    }

    "have a form" which {

      "has the correct action and method assigned" in {
        val form: Element = document().getForm
        form.attr("method") mustBe testCall.method
        form.attr("action") mustBe testCall.url
      }

      "has a correct date input field with the legend as the page heading" when {
        "there is no error on the page" in {
          document().getForm.mustHaveDateInput(
            name = BusinessStartDateForm.startDate,
            label = BusinessStartDateMessages.heading,
            hint = Some(BusinessStartDateMessages.exampleStartDate)
          )
        }
        "there is an error on the page" in {
          document(error = Some(testError)).getForm.mustHaveDateInput(
            name = BusinessStartDateForm.startDate,
            label = BusinessStartDateMessages.heading,
            hint = Some(BusinessStartDateMessages.exampleStartDate),
            error = Some(testError)
          )
        }
      }

      "has a button to continue" when {
        "not in edit mode" in {
          document().getForm.getGovukButton.text mustBe BusinessStartDateMessages.continue
        }
        "in edit mode" in {
          document(isEditMode = true).getForm.getGovukButton.text mustBe BusinessStartDateMessages.update
        }
      }

    }


  }

}
