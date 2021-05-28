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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.templates

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ViewSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views


class MainTemplateViewSpec extends ViewSpec {

  implicit val request: FakeRequest[AnyContentAsEmpty.type] = fakeTestRequest

  object MainTemplateMessages {
    val title = "testTitle"
    val titleSuffix = " - Use software to send Income Tax updates - GOV.UK"
  }
  def page(enabledTimeout: Boolean): HtmlFormat.Appendable = views.html.templates.main_template(
  title = MainTemplateMessages.title,
  enableTimeout = enabledTimeout)(HtmlFormat.empty)(request, implicitly, appConfig)



  class Setup(enabledTimeOut: Boolean = true) {
    val doc: Document = Jsoup.parse(page(enabledTimeOut).body)
  }

  "The Main Template view" should {
    "display the title" in new Setup {
      doc.title mustBe MainTemplateMessages.title + MainTemplateMessages.titleSuffix

    }

    "show timeout" in new Setup(true) {
      val timeoutScriptText = doc.getElementById("timeoutScript").html()
      timeoutScriptText.contains(appConfig.countdownLength) mustBe true
      timeoutScriptText.contains(appConfig.timeoutLength) mustBe true
    }

    "not show timeout" in new Setup(false) {
      Option(doc.getElementById("timeoutScript")) mustBe None
    }
  }
}
