/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.{ImplicitDateFormatter, ImplicitDateFormatterImpl, ViewSpec}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.check_your_answers

class CheckYourAnswersViewSpec extends ViewSpec {

  object CheckYourAnswersMessages {
    val title = "Check your answers"
    val heading: String = title
    val continue = "Continue"
    val backLink = "Back"
    val change = "Change"
    val tradingStartDate = "Trading start date of business"
    val businessName = "Business name"
    val businessTrade = "Business trade"
  }

  val backUrl: String = testBackUrl
  val action: Call = testCall
  val getAllSelfEmploymentModel: GetAllSelfEmploymentModel = GetAllSelfEmploymentModel(
    businessStartDate = BusinessStartDate(DateModel("1", "1", "2018")),
    businessName = BusinessNameModel("ABC Limited"),
    businessTradeName = BusinessTradeNameModel("Plumbing")
  )

  val implicitDateFormatter: ImplicitDateFormatter = app.injector.instanceOf[ImplicitDateFormatterImpl]

  trait Setup {
    val page: HtmlFormat.Appendable = check_your_answers(
      getAllSelfEmploymentModel,
      testCall,
      testBackUrl,
      implicitDateFormatter
    )(FakeRequest(), implicitly, appConfig)

    val document: Document = Jsoup.parse(page.body)
  }

  "Check Your Answers" must {

    "have a title" in new Setup {
      document.title mustBe CheckYourAnswersMessages.title
    }

    "have a heading" in new Setup {
      document.getH1Element.text mustBe CheckYourAnswersMessages.heading
    }

    "have a Form" in new Setup {
      document.getForm.attr("method") mustBe testCall.method
      document.getForm.attr("action") mustBe testCall.url
    }

    "have a check your answers table" which {

      "has a row for the trading start date" which {
        "has a label to identify it" in new Setup {
          document.getSummaryList.getSummaryListRow(1).getSummaryListKey.text mustBe CheckYourAnswersMessages.tradingStartDate
        }
        "has a answer the user gave" in new Setup {
          document.getSummaryList.getSummaryListRow(1).getSummaryListValue.text mustBe "1 January 2018"
        }
        "has a change link" in new Setup {
          val changeLink: Element = document.getSummaryList.getSummaryListRow(1).getSummaryListActions.selectFirst("a")
          changeLink.text mustBe CheckYourAnswersMessages.change
          changeLink.attr("href") mustBe routes.BusinessStartDateController.show(/*isEditMode = true*/).url
        }
      }

      "has a row for the business name" which {
        "has a label to identify it" in new Setup {
          document.getSummaryList.getSummaryListRow(2).getSummaryListKey.text mustBe CheckYourAnswersMessages.businessName
        }
        "has a answer the user gave" in new Setup {
          document.getSummaryList.getSummaryListRow(2).getSummaryListValue.text mustBe "ABC Limited"
        }
        "has a change link" in new Setup {
          val changeLink: Element = document.getSummaryList.getSummaryListRow(2).getSummaryListActions.selectFirst("a")
          changeLink.text mustBe CheckYourAnswersMessages.change
          changeLink.attr("href") mustBe routes.BusinessNameController.show(isEditMode = true).url
        }
      }

      "has a row for the business trade" which {
        "has a label to identify it" in new Setup {
          document.getSummaryList.getSummaryListRow(3).getSummaryListKey.text mustBe CheckYourAnswersMessages.businessTrade
        }
        "has a answer the user gave" in new Setup {
          document.getSummaryList.getSummaryListRow(3).getSummaryListValue.text mustBe "Plumbing"
        }
        "has a change link" in new Setup {
          val changeLink: Element = document.getSummaryList.getSummaryListRow(3).getSummaryListActions.selectFirst("a")
          changeLink.text mustBe CheckYourAnswersMessages.change
          changeLink.attr("href") mustBe routes.BusinessTradeNameController.show(isEditMode = true).url
        }
      }
    }

    "have a continue button" in new Setup {
      document.getSubmitButton.text mustBe CheckYourAnswersMessages.continue
    }

    "have a backlink" in new Setup {
      document.getBackLink.text mustBe CheckYourAnswersMessages.backLink
      document.getBackLink.attr("href") mustBe testBackUrl
    }

  }

}
