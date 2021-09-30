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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessAccountingMethodForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.AccountingMethodModel
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ViewSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.BusinessAccountingMethod

class BusinessAccountingMethodViewSpec extends ViewSpec {
  val backUrl: String = testBackUrl
  val action: Call = testCall
  val emptyError = "Select if you use cash accounting or standard accounting"

  object BusinessAccountingMethodMessages {
    val title = "What accounting method do you use for your sole trader business?"
    val titleSuffix = " - Use software to send Income Tax updates - GOV.UK"
    val heading: String = title
    val line_1: String = "If you have more than one sole trader business, all your businesses need to have the same accounting method."
    val accordion = "Show me an example"
    val accordionLine_1 = "You created an invoice for someone in March 2017, but did not receive the money until May 2017. If you tell HMRC you received this income in:"
    val accordionBullet_1 = "May 2017, you use ‘cash accounting’"
    val accordionBullet_2 = "March 2017, you use ‘standard accounting’"
    val cash = "Cash accounting"
    val cashDescription = "You record on the date you receive money or pay a bill. Most sole traders and small businesses use this method."
    val accruals = "Standard accounting"
    val accrualsDescription = "You record on the date you send or receive an invoice, even if you do not receive or pay any money. This is also called ‘accruals’ or ‘traditional accounting’."
    val continue = "Continue"
    val update = "Update"
    val backLink = "Back"
  }

  private val businessAccountingMethodView = app.injector.instanceOf[BusinessAccountingMethod]

  "Business Accounting Method Page" must {

    "have a title" in {
      document().title mustBe BusinessAccountingMethodMessages.title + BusinessAccountingMethodMessages.titleSuffix
    }

    "have a backlink" in {
      document().getBackLinkByClass.text mustBe BusinessAccountingMethodMessages.backLink
      document().getBackLinkByClass.attr("href") mustBe testBackUrl
    }

    "have a heading" in {
      document().getH1Element.text mustBe BusinessAccountingMethodMessages.heading
    }

    "have a paragraph" in {
      document().getParagraphNth(2) mustBe BusinessAccountingMethodMessages.line_1
    }

    "have an accordion summary" in {
      document().select(".govuk-details__summary-text").text() mustBe BusinessAccountingMethodMessages.accordion
    }

    "have an accordion heading" in {
      document().getParagraphNth(3) mustBe BusinessAccountingMethodMessages.accordionLine_1
    }

    "have an accordion bullets list 1" in {
      document().getBulletPointNth() mustBe BusinessAccountingMethodMessages.accordionBullet_1
    }

    "have an accordion bullets list 2" in {
      document().getBulletPointNth(1) mustBe BusinessAccountingMethodMessages.accordionBullet_2
    }

    //radio button test
    "have a radio button for cash accounting" in {
      document().getGovukRadioButtonByIndex().select("#businessAccountingMethod-Cash").size() mustBe 1
    }

    "have a cash accounting heading for the radio button" in {
      document().getGovukRadioButtonByIndex().select("label").text() mustBe BusinessAccountingMethodMessages.cash
    }

    "have the correct description for the cash accounting radio button" in {
      val startIndex: Int = 16
      document().getGovukRadioButtonByIndex().select(".govuk-radios__hint").text() mustBe BusinessAccountingMethodMessages.cashDescription
    }

    "have a radio button for standard accounting" in {
      document().getGovukRadioButtonByIndex(1).select("#businessAccountingMethod-Standard").size() mustBe 1
    }

    "have a standard accounting heading for the radio button" in {
      document().getGovukRadioButtonByIndex(1).select("label").text() mustBe BusinessAccountingMethodMessages.accruals
    }

    "have the correct description for the standard accounting radio button" in {
      val startIndex: Int = 20
      document().getGovukRadioButtonByIndex(1).select(".govuk-radios__hint").text() mustBe BusinessAccountingMethodMessages.accrualsDescription
    }

    "have a Form" in {
      document().getForm.attr("method") mustBe testCall.method
      document().getForm.attr("action") mustBe testCall.url
    }

    "have a continue button when it is not in edit mode" in {
      document().getButtonByClass mustBe BusinessAccountingMethodMessages.continue
    }

    "have an update button when it is in edit mode" in {
      document(BusinessAccountingMethodForm.businessAccountingMethodForm, isEditMode = true).getButtonByClass mustBe BusinessAccountingMethodMessages.update
    }

  }

  "must display empty form error summary when submit with an empty form" in {
    document(BusinessAccountingMethodForm.businessAccountingMethodForm.withError("", emptyError)).mustHaveErrorSummaryByNewGovUkClass(List[String](emptyError))
  }

  "must display empty form error message when submit with an empty form" in {
    document(
      BusinessAccountingMethodForm
        .businessAccountingMethodForm
        .withError(BusinessAccountingMethodForm.businessAccountingMethod, emptyError)
    ).mustHaveGovUkErrorNotificationMessage(emptyError)
  }

  private def page(businessAccountingMethodForm: Form[AccountingMethodModel], isEditMode: Boolean) = {
    businessAccountingMethodView(
      businessAccountingMethodForm,
      testCall,
      isEditMode,
      testBackUrl
    )(FakeRequest(), implicitly, appConfig)
  }

  private def document(
                        businessAccountingMethodForm: Form[AccountingMethodModel] = BusinessAccountingMethodForm.businessAccountingMethodForm,
                        isEditMode: Boolean = false): Document = {
    Jsoup.parse(page(businessAccountingMethodForm, isEditMode).body)
  }
}
