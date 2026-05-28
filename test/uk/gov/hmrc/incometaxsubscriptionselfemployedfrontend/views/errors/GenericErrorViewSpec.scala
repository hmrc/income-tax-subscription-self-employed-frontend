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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.errors

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.convertToStringMustWrapperForVerb
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ViewSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.errors.GenericError

class GenericErrorViewSpec extends ViewSpec {

  val title = "Generic Error"
  val heading = "Sorry, there is a problem with this service"
  val message = "Try again later."

  def errorTemplate: GenericError = app.injector.instanceOf[GenericError]

  def page(isAgent: Boolean): HtmlFormat.Appendable =
    errorTemplate(
      title,
      heading,
      message,
      isAgent
    )(fakeTestRequest, implicitly)

  def document(isAgent: Boolean): Document = Jsoup.parse(page(isAgent).body)

  object GenericErrorMessages {
    val saved = "We saved your answers. They will be available for 30 days."
  }

  "GenericError" must {
    "has correct template for individuals" in new TemplateViewTest(
      view = page(false),
      isAgent = false,
      title = title,
      hasSignOutLink = true
    )

    "has correct template for agents" in new TemplateViewTest(
      view = page(true),
      isAgent = true,
      title = title,
      hasSignOutLink = true
    )

    "has a page heading" in {
      Seq(false, true).foreach { isAgent =>
        document(isAgent).mainContent.selectHead("h1").text mustBe heading
      }
    }

    "has a first paragraph" in {
      Seq(false, true).foreach { isAgent =>
        document(isAgent).mainContent.selectNth("p", 1).text mustBe message
      }
    }

    "has a second paragraph" in {
      Seq(false, true).foreach { isAgent =>
        document(isAgent).mainContent.selectNth("p", 2).text mustBe GenericErrorMessages.saved
      }
    }
  }
}
