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
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ViewSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.AccountingMethodModel
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.BusinessAccountingMethodForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.business_accounting_method




class BusinessAccountingMethodViewSpec extends ViewSpec {

  val backUrl: String = testBackUrl
  val action: Call = testCall
  val emptyError = "Select if your client uses cash accounting or standard accounting"

  object BusinessAccountingMethodMessages {
    val title = "What accounting method does your client use for their self-employed business?"
    val titleSuffix = " - Report your income and expenses quarterly - GOV.UK"
    val heading: String = title
    val cash = "Cash accounting"
    val accruals = "Standard accounting"
    val continue = "Continue"
    val update = "Update"
    val backLink = "Back"
  }

  class Setup(businessAccountingMethodForm: Form[AccountingMethodModel] = BusinessAccountingMethodForm.businessAccountingMethodForm,
              isEditMode: Boolean = false) {
    val page: HtmlFormat.Appendable = business_accounting_method(
      businessAccountingMethodForm,
      testCall,
      testBackUrl,
      isEditMode
    )(FakeRequest(), implicitly, appConfig)

    val document: Document = Jsoup.parse(page.body)
  }

  "Business Accounting Method Page" must {

    "have a title" in new Setup {
      document.title mustBe BusinessAccountingMethodMessages.title + BusinessAccountingMethodMessages.titleSuffix
    }

    "have a backlink" in new Setup {
      document.getBackLink.text mustBe BusinessAccountingMethodMessages.backLink
      document.getBackLink.attr("href") mustBe testBackUrl
    }

    "have a heading" in new Setup {
      document.getH1Element.text mustBe BusinessAccountingMethodMessages.heading

    }
    "have a radio button for cash accounting" in new Setup {
      document.getRadioButtonByIndex(0).select("#businessAccountingMethod-Cash").size() mustBe 1
    }

    "have a radio button for standard accounting" in new Setup {
      document.getRadioButtonByIndex(1).select("#businessAccountingMethod-Standard").size() mustBe 1
    }

    "have a Form" in new Setup {
      document.getForm.attr("method") mustBe testCall.method
      document.getForm.attr("action") mustBe testCall.url
    }

    "have a continue button when not in edit mode" in new Setup {
      document.getSubmitButton.text mustBe BusinessAccountingMethodMessages.continue
    }

    "have a update button when in edit mode" in new Setup(isEditMode = true) {
      document.getSubmitButton.text mustBe BusinessAccountingMethodMessages.update
    }

    "must display empty form error summary when submit with an empty form" in new Setup(BusinessAccountingMethodForm.businessAccountingMethodForm.withError("", emptyError)) {
      document.mustHaveErrorSummary(List[String](emptyError))
    }

    "must display empty form error message when submit with an empty form" in new Setup(BusinessAccountingMethodForm.businessAccountingMethodForm.withError(BusinessAccountingMethodForm.businessAccountingMethod, emptyError)) {

      document.listErrorMessages(List[String](emptyError))
    }
  }
}
