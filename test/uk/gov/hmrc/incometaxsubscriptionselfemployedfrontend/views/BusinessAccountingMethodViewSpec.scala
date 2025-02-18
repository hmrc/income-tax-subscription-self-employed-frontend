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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.api.data.Form
import play.api.mvc.Call
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessAccountingMethodForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{AccountingMethod, Accruals, Cash}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ViewSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.BusinessAccountingMethod

class BusinessAccountingMethodViewSpec extends ViewSpec with FeatureSwitching {
  val backUrl: String = testBackUrl
  val action: Call = testCall
  val emptyErrorKey = "error.business-accounting-method.empty"

  object BusinessAccountingMethodMessages {
    val title = "What accounting method do you use for your sole trader business?"
    val titleSuffix = " - Use software to send Income Tax updates - GOV.UK"
    val heading: String = title
    val captionHidden = "This section is"
    val captionVisual = "Sole trader"
    val line_1: String = "If you have more than one sole trader business, all your businesses need to have the same accounting method."
    val accordion = "Help with accounting methods"
    val accordion_subheading = "Example"
    val accordionLine_1 = "You created an invoice for someone in March 2017, but did not receive the money until May 2017. If you tell HMRC you received this income in:"
    val accordionBullet_1 = "May 2017, you use ‘cash basis accounting’"
    val accordionBullet_2 = "March 2017, you use ‘traditional accounting’"
    val cash = "Cash basis accounting"
    val cashDescription = "You record on the date you receive money or pay a bill. Most sole traders and small businesses use this method."
    val accruals = "Traditional accounting"
    val accrualsDescription = "You record on the date you send or receive an invoice, even if you do not receive or pay any money. This is also called ‘accruals’ or ‘standard accounting’."
    val continue = "Continue"
    val update = "Update"
    val saveAndContinue = "Save and continue"
    val saveAndComeBackLater = "Save and come back later"
    val backLink = "Back"
    val emptyError = "Select if you use cash basis accounting or traditional accounting."
  }

  private val businessAccountingMethodView = app.injector.instanceOf[BusinessAccountingMethod]

  "Business Accounting Method Page" must {

    "have the correct template" in new TemplateViewTest(
      view = page(
        businessAccountingMethodForm = BusinessAccountingMethodForm.businessAccountingMethodForm,
        isEditMode = false,
        backLink = testBackUrl
      ),
      title = BusinessAccountingMethodMessages.title,
      isAgent = false,
      backLink = Some(testBackUrl),
      hasSignOutLink = true
    )

    "have the correct heading and caption" in {
      document().mainContent.mustHaveHeadingAndCaption(
        heading = BusinessAccountingMethodMessages.heading,
        caption = BusinessAccountingMethodMessages.captionVisual,
        isSection = true
      )
    }

    "have a paragraph" in {
      document().getParagraphNth(2) mustBe BusinessAccountingMethodMessages.line_1
    }

    "have an accordion summary" in {
      document().select(".govuk-details__summary-text").text() mustBe BusinessAccountingMethodMessages.accordion
    }

    "have an accordion sub heading" in {
      document().getParagraphNth(3) mustBe BusinessAccountingMethodMessages.accordion_subheading
    }

    "have an accordion heading" in {
      document().getParagraphNth(4) mustBe BusinessAccountingMethodMessages.accordionLine_1
    }

    "have an accordion bullets list 1" in {
      document().getBulletPointNth() mustBe BusinessAccountingMethodMessages.accordionBullet_1
    }

    "have an accordion bullets list 2" in {
      document().getBulletPointNth(1) mustBe BusinessAccountingMethodMessages.accordionBullet_2
    }

    "has a set of radio buttons inputs" in {
      document().mainContent.mustHaveRadioInput(
        selector = "fieldset"
      )(
        name = BusinessAccountingMethodForm.businessAccountingMethod,
        legend = BusinessAccountingMethodMessages.heading,
        isHeading = false,
        isLegendHidden = true,
        hint = None,
        errorMessage = None,
        radioContents = Seq(
          RadioItem(
            content = Text(BusinessAccountingMethodMessages.cash),
            value = Some(Cash.CASH),
            hint = Some(Hint(content = Text(BusinessAccountingMethodMessages.cashDescription))),
          ),
          RadioItem(
            content = Text(BusinessAccountingMethodMessages.accruals),
            value = Some(Accruals.ACCRUALS),
            hint = Some(Hint(content = Text(BusinessAccountingMethodMessages.accrualsDescription))),
          )
        )
      )
    }

    "have a Form" in {
      document().getForm.attr("method") mustBe testCall.method
      document().getForm.attr("action") mustBe testCall.url
    }

    "have a save and continue button" in {
      document(BusinessAccountingMethodForm.businessAccountingMethodForm).select("button").last().text mustBe BusinessAccountingMethodMessages.saveAndContinue
    }

    "have a save and come back later link" in {
      val saveAndComeBackLink: Element = document(BusinessAccountingMethodForm.businessAccountingMethodForm).selectHead("a[role=button]")
      saveAndComeBackLink.text mustBe BusinessAccountingMethodMessages.saveAndComeBackLater
      saveAndComeBackLink.attr("href") mustBe
        appConfig.subscriptionFrontendProgressSavedUrl + "?location=sole-trader-accounting-type"
    }
  }

  "must display empty form error summary when submit with an empty form" in {
    document(
      BusinessAccountingMethodForm
        .businessAccountingMethodForm
        .withError(BusinessAccountingMethodForm.businessAccountingMethod, emptyErrorKey)
    ).mustHaveErrorSummaryByNewGovUkClass(List[String](BusinessAccountingMethodMessages.emptyError))
  }

  "must display empty form error message when submit with an empty form" in {
    document(
      BusinessAccountingMethodForm
        .businessAccountingMethodForm
        .withError(BusinessAccountingMethodForm.businessAccountingMethod, emptyErrorKey)
    ).mustHaveGovUkErrorNotificationMessage(BusinessAccountingMethodMessages.emptyError)
  }

  private def page(businessAccountingMethodForm: Form[AccountingMethod], isEditMode: Boolean, backLink: String) = {
    businessAccountingMethodView(
      businessAccountingMethodForm,
      testCall,
      isEditMode,
      backLink
    )(FakeRequest(), implicitly)
  }

  private def document(
                        businessAccountingMethodForm: Form[AccountingMethod] = BusinessAccountingMethodForm.businessAccountingMethodForm,
                        isEditMode: Boolean = false,
                        backLink: String = testBackUrl
                      ): Document = {
    Jsoup.parse(page(businessAccountingMethodForm, isEditMode, backLink).body)
  }
}
