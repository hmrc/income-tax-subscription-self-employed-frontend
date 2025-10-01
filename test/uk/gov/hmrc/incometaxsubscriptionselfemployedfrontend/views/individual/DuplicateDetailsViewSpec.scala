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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.individual

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.individual.routes
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ViewSpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.individual.{DuplicateDetails => DuplicateDetailsView}

class DuplicateDetailsViewSpec extends ViewSpec {

  val duplicateDetailsView: DuplicateDetailsView = app.injector.instanceOf[DuplicateDetailsView]

  val id: String = "test-id"
  val testName: String = "test-name"
  val testTrade: String = "test-trade"

  def view(isEditMode: Boolean, isGlobalEdit: Boolean): HtmlFormat.Appendable = duplicateDetailsView(
    id = id,
    trade = testTrade,
    name = testName,
    isEditMode = isEditMode,
    isGlobalEdit = isGlobalEdit,
    backUrl = testBackUrl
  )(fakeTestRequest, implicitly)

  def document(isEditMode: Boolean = false, isGlobalEdit: Boolean = false): Document = {
    Jsoup.parse(view(isEditMode, isGlobalEdit).body)
  }

  def mainContent(isEditMode: Boolean = false, isGlobalEdit: Boolean = false): Element = document(isEditMode, isGlobalEdit).mainContent

  "DuplicateDetails" must {
    "use the correct template" in new TemplateViewTest(
      view = view(isEditMode = false, isGlobalEdit = false),
      title = DuplicateDetailsMessages.heading,
      isAgent = false,
      backLink = Some(testBackUrl),
      hasSignOutLink = true
    )

    "have the correct heading" in {
      mainContent().getH1Element.text mustBe DuplicateDetailsMessages.heading
    }

    "has an initial paragraph with their trade and name" in {
      mainContent().selectNth("p", 1).text mustBe DuplicateDetailsMessages.para(testTrade, testName)
    }

    "has a bullet point list" which {
      "has a lead-in line" in {
        mainContent().selectNth("p", 2).text mustBe DuplicateDetailsMessages.Options.leadInLine
      }

      def bulletList(isEditMode: Boolean = false, isGlobalEdit: Boolean = false): Element = mainContent(isEditMode, isGlobalEdit).selectHead("ul")

      "has a first bullet point with a link to go back" when {
        "not in edit mode" in {
          val point: Element = bulletList().selectNth("li", 1).selectHead("a")
          point.text mustBe DuplicateDetailsMessages.Options.bulletOne
          point.attr("href") mustBe routes.FullIncomeSourceController.show(id).url
        }
        "in edit mode" in {
          val point: Element = bulletList(isEditMode = true).selectNth("li", 1).selectHead("a")
          point.text mustBe DuplicateDetailsMessages.Options.bulletOne
          point.attr("href") mustBe routes.FullIncomeSourceController.show(id, isEditMode = true).url
        }
        "in global edit mode" in {
          val point: Element = bulletList(isGlobalEdit = true).selectNth("li", 1).selectHead("a")
          point.text mustBe DuplicateDetailsMessages.Options.bulletOne
          point.attr("href") mustBe routes.FullIncomeSourceController.show(id, isGlobalEdit = true).url
        }
      }

      "has a second bullet point with a link to add a new business" in {
        val point: Element = bulletList().selectNth("li", 2).selectHead("a")
        point.text mustBe DuplicateDetailsMessages.Options.bulletTwo
        point.attr("href") mustBe routes.InitialiseController.initialise.url
      }

      "has a third bullet point with a link to add a new business" in {
        val point: Element = bulletList().selectNth("li", 3).selectHead("a")
        point.text mustBe DuplicateDetailsMessages.Options.bulletThree
        point.attr("href") mustBe appConfig.yourIncomeSourcesUrl
      }
    }

  }


  object DuplicateDetailsMessages {
    val heading = "There is a problem"

    def para(trade: String, name: String) = s"You cannot enter the trade ($trade) twice for the business named $name."

    object Options {
      val leadInLine: String = "You can:"
      val bulletOne: String = "change the original entry"
      val bulletTwo: String = "add a different sole trader business"
      val bulletThree: String = "go to your income sources"
    }
  }


}
