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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.api.i18n.Lang
import play.api.mvc.{Cookie, Cookies, Headers}
import play.api.test.FakeRequest
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ViewSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.ChangeAccountingMethod

class ChangeAccountingMethodViewSpec extends ViewSpec with FeatureSwitching {

  object ChangeAccountingMethodMessages {
    val heading = "Changing accounting method"
    val titleSuffix = " - Use software to send Income Tax updates - GOV.UK"
    val title: String = heading + titleSuffix
    val line_1: String = "If you have more than one sole trader business, all your businesses must have the same accounting method."
    val warning: String = "Warning"
    val warningText: String = "If you change the accounting method for this business, it will automatically update the accounting method across all your sole trader businesses."
    val backLink: String = "Back"
    val continue: String = "Continue"
  }

  private val changeAccountingMethodView = app.injector.instanceOf[ChangeAccountingMethod]

  "Business Accounting Method Page" must {

    "have a title" in {
      document.title mustBe ChangeAccountingMethodMessages.title
    }

    "have a back link" in {
      document.getBackLinkByClass.text mustBe ChangeAccountingMethodMessages.backLink
      document.getBackLinkByClass.attr("href") mustBe testBackUrl
    }

    "have a heading" in {
      document.getH1Element.text mustBe ChangeAccountingMethodMessages.heading
    }

    "have a paragraph" in {
      document.getParagraphNth(1) mustBe ChangeAccountingMethodMessages.line_1
    }

    "have a warning text section" in {
      val warningSection = document.selectHead(".govuk-warning-text")
      warningSection.text mustBe s"! ${ChangeAccountingMethodMessages.warning} ${ChangeAccountingMethodMessages.warningText}"
    }

    "have a form" which {
      def form: Element = document.selectHead("form")

      "has the correct attributes" in {
        form.attr("method") mustBe testCall.method
        form.attr("action") mustBe testCall.url
      }
      "has a continue button" in {
        form.select("button").text mustBe ChangeAccountingMethodMessages.continue
      }
    }
  }

  private def page = {
    changeAccountingMethodView(
      testCall,
      testBackUrl
    )(FakeRequest(), implicitly)
  }

  private def document: Document = {
    Jsoup.parse(page.body)
  }
}
