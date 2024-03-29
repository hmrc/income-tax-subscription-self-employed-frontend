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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.agent

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.routes
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.{ImplicitDateFormatter, ImplicitDateFormatterImpl, ViewSpec}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.SelfEmployedCYA

class SelfEmployedCYAViewSpec extends ViewSpec {

  object CheckYourAnswersMessages {
    val captionHidden = "This section is"
    val caption = "FirstName LastName | ZZ 11 11 11 Z"
    val heading = "Check your answers"
    val title = "Check your answers - sole trader business"
    val confirmAndContinue = "Confirm and continue"
    val continue = "Continue"
    val saveAndBack = "Save and come back later"
    val change = "Change"
    val back = "Back"
    val add = "Add"
    val tradingStartDate = "Trading start date"
    val changeTradingStartDate = "Change trading start date"
    val addTradingStartDate = "Add trading start date"
    val businessName = "Business name"
    val changeBusinessName = "Change business name"
    val addBusinessName = "Add business name"
    val businessAddress = "Address"
    val changeBusinessAddress = "Change address"
    val addBusinessAddress = "Add address"
    val businessTrade = "Type of trade"
    val changeBusinessTrade = "Change type of trade"
    val addTypeOfTrade = "Add type of trade"
    val accountingMethod = "Accounting method for sole trader income"
    val changeAccountingMethod = "Change accounting method for sole trader income"
    val addAccountingMethod = "Add accounting method for sole trader income"
    val yes = "Yes"
    val no = "No"
  }

  def testSelfEmploymentsCYAModel: SelfEmploymentsCYAModel = SelfEmploymentsCYAModel(
    id = testId,
    businessStartDate = Some(DateModel("1", "1", "2018")),
    businessName = Some(s"ABC Limited"),
    businessTradeName = Some(s"Plumbing"),
    businessAddress = Some(Address(Seq(s"line", "line9", "line99"), Some("TF3 4NT"))),
    accountingMethod = Some(Cash),
    totalSelfEmployments = 1
  )

  val implicitDateFormatter: ImplicitDateFormatter = app.injector.instanceOf[ImplicitDateFormatterImpl]

  val checkYourAnswers: SelfEmployedCYA = app.injector.instanceOf[SelfEmployedCYA]

  val backUrl: Option[String] = Some(testBackUrl)

  val testId: String = "testId"

  class SetupIncomplete(answers: SelfEmploymentsCYAModel = SelfEmploymentsCYAModel(
    id = testId,
    businessStartDate = Some(DateModel("1", "1", "2018")),
    businessName = Some(s"ABC Limited"),
    businessTradeName = Some(s"Plumbing"),
    businessAddress = Some(Address(Seq(s"line", "line9", "line99"), Some("TF3 4NT"))),
    totalSelfEmployments = 1
  )) {
    val page: HtmlFormat.Appendable = checkYourAnswers(
      answers,
      testCall,
      backUrl,
      ClientDetails("FirstName LastName", "ZZ111111Z")
    )(FakeRequest(), implicitly)

    val document: Document = Jsoup.parse(page.body)
  }

  class SetupComplete(confirmed: Boolean = false) {
    val answers: SelfEmploymentsCYAModel = SelfEmploymentsCYAModel(
      id = testId,
      businessStartDate = Some(DateModel("1", "1", "2018")),
      businessName = Some(s"ABC Limited"),
      businessTradeName = Some(s"Plumbing"),
      businessAddress = Some(Address(Seq(s"line", "line9", "line99"), Some("TF3 4NT"))),
      confirmed = confirmed,
      accountingMethod = Some(Cash),
      totalSelfEmployments = 1
    )
    val page: HtmlFormat.Appendable = checkYourAnswers(
      answers,
      testCall,
      backUrl = None,
      ClientDetails("FirstName LastName", "ZZ111111Z")
    )(FakeRequest(), implicitly)

    val document: Document = Jsoup.parse(page.body)
  }

  "Check Your Answers" must {

    "have the correct template details" when {
      "there is no error" in new TemplateViewTest(
        view = checkYourAnswers(
          answers = testSelfEmploymentsCYAModel,
          postAction = testCall,
          backUrl,
          ClientDetails("FirstName LastName", "ZZ111111Z")
        )(FakeRequest(), implicitly),
        title = CheckYourAnswersMessages.title,
        isAgent = true,
        hasSignOutLink = true,
        backLink = backUrl
      )
    }

    "have a heading" in new SetupComplete {
      val header: Element = document.mainContent.getHeader
      header.getH1Element.text mustBe CheckYourAnswersMessages.heading
    }

    "have a caption" in new SetupComplete() {
      document.selectHead(".govuk-caption-l")
        .text() mustBe CheckYourAnswersMessages.caption
    }


    "display a business check your answers" when {
      "all the answers have been completed" should {

        "has a check your answers table" which {
          "has a row for the trading start date" which {
            "has a label to identify it" in new SetupComplete {
              document.getSummaryList().getSummaryListRow(2).getSummaryListKey.text mustBe CheckYourAnswersMessages.tradingStartDate
            }
            "has a answer the user gave" in new SetupComplete {
              document.getSummaryList().getSummaryListRow(2).getSummaryListValue.text mustBe "1 January 2018"
            }
            "has a change link with correct content" in new SetupComplete {
              val changeLink: Element = document.getSummaryList().getSummaryListRow(2).getSummaryListActions.selectHead("a")
              changeLink.selectHead("span[aria-hidden=true]").text mustBe CheckYourAnswersMessages.change
              changeLink.selectHead("span[class=govuk-visually-hidden]").text mustBe CheckYourAnswersMessages.changeTradingStartDate
              changeLink.attr("href") mustBe routes.BusinessStartDateController.show(id = "testId", isEditMode = true).url
            }
          }

          "has a row for the business name" which {
            "has a label to identify it" in new SetupComplete {
              document.getSummaryList().getSummaryListRow(1).getSummaryListKey.text mustBe CheckYourAnswersMessages.businessName
            }
            "has a answer the user gave" in new SetupComplete {
              document.getSummaryList().getSummaryListRow(1).getSummaryListValue.text mustBe "ABC Limited"
            }
            "has a change link" in new SetupComplete {
              val changeLink: Element = document.getSummaryList().getSummaryListRow(1).getSummaryListActions.selectHead("a")
              changeLink.selectHead("span[aria-hidden=true]").text mustBe CheckYourAnswersMessages.change
              changeLink.selectHead("span[class=govuk-visually-hidden]").text mustBe CheckYourAnswersMessages.changeBusinessName
              changeLink.attr("href") mustBe routes.BusinessNameController.show(id = "testId", isEditMode = true).url
            }
          }

          "has a row for the business trade" which {
            "has a label to identify it" in new SetupComplete {
              document.getSummaryList().getSummaryListRow(3).getSummaryListKey.text mustBe CheckYourAnswersMessages.businessTrade
            }
            "has a answer the user gave" in new SetupComplete {
              document.getSummaryList().getSummaryListRow(3).getSummaryListValue.text mustBe "Plumbing"
            }
            "has a change link" in new SetupComplete {
              val changeLink: Element = document.getSummaryList().getSummaryListRow(3).getSummaryListActions.selectHead("a")
              changeLink.selectHead("span[aria-hidden=true]").text mustBe CheckYourAnswersMessages.change
              changeLink.selectHead("span[class=govuk-visually-hidden]").text mustBe CheckYourAnswersMessages.changeBusinessTrade
              changeLink.attr("href") mustBe routes.BusinessTradeNameController.show(id = "testId", isEditMode = true).url
            }
          }

          "has a row for the business address" which {
            "has a label to identify it" in new SetupComplete {
              document.getSummaryList().getSummaryListRow(4).getSummaryListKey.text mustBe CheckYourAnswersMessages.businessAddress
            }
            "has a answer the user gave" in new SetupComplete {
              document.getSummaryList().getSummaryListRow(4).getSummaryListValue.text mustBe "line, line9, line99, TF3 4NT"
            }
            "has a change link" in new SetupComplete {
              val changeLink: Element = document.getSummaryList().getSummaryListRow(4).getSummaryListActions.selectHead("a")
              changeLink.selectHead("span[aria-hidden=true]").text mustBe CheckYourAnswersMessages.change
              changeLink.selectHead("span[class=govuk-visually-hidden]").text mustBe CheckYourAnswersMessages.changeBusinessAddress
              changeLink.attr("href") mustBe routes.AddressLookupRoutingController.initialiseAddressLookupJourney(businessId = "testId", isEditMode = true).url
            }
          }

          "has a row for the business accounting method" which {
            "has a label to identify it" in new SetupComplete {
              document.getSummaryList().getSummaryListRow(5).getSummaryListKey.text mustBe CheckYourAnswersMessages.accountingMethod
            }
            "has a answer the user gave" in new SetupComplete {
              document.getSummaryList().getSummaryListRow(5).getSummaryListValue.text mustBe "Cash basis accounting"
            }
            "has a change link" in new SetupComplete {
              val changeLink: Element = document.getSummaryList().getSummaryListRow(5).getSummaryListActions.selectHead("a")
              changeLink.selectHead("span[aria-hidden=true]").text mustBe CheckYourAnswersMessages.change
              changeLink.selectHead("span[class=govuk-visually-hidden]").text mustBe CheckYourAnswersMessages.changeAccountingMethod
              changeLink.attr("href") mustBe routes.BusinessAccountingMethodController.show(id = "testId", isEditMode = true).url
            }
          }

        }
        "have a confirm and continue button" in new SetupComplete {
          document.select("button").last.text mustBe CheckYourAnswersMessages.confirmAndContinue
        }

        "have a continue button if confirmed" in new SetupComplete(true) {
          document.select("button").last.text mustBe CheckYourAnswersMessages.continue
        }

        "have a save and come back later button" in new SetupComplete {
          val buttonLink: Element = document.mainContent.selectHead(".govuk-button--secondary")
          buttonLink.text mustBe CheckYourAnswersMessages.saveAndBack
          buttonLink.attr("href") mustBe
            appConfig.subscriptionFrontendClientProgressSavedUrl + "?location=sole-trader-check-your-answers"
        }

        "not have a save and come back later button if confirmed" in new SetupComplete(true) {
          val buttonLink: Option[Element] = document.mainContent.selectOptionally(".govuk-button--secondary")
          buttonLink mustBe None
        }

        "have no back button" in new SetupComplete {
          document.getBackLinkByClass mustBe empty
        }

      }
    }

    "display a business check your answers" when {
      "some of the answers have not been completed" should {

        "has a check your answers table" which {
          "has a row for the trading start date" which {
            "has a label to identify it" in new SetupIncomplete {
              document.getSummaryList().getSummaryListRow(2).getSummaryListKey.text mustBe CheckYourAnswersMessages.tradingStartDate
            }
            "has a answer the user gave" in new SetupIncomplete {
              document.getSummaryList().getSummaryListRow(2).getSummaryListValue.text mustBe "1 January 2018"
            }
            "has a change link with correct content" in new SetupIncomplete {
              val changeLink: Element = document.getSummaryList().getSummaryListRow(2).getSummaryListActions.selectHead("a")
              changeLink.selectHead("span[aria-hidden=true]").text mustBe CheckYourAnswersMessages.change
              changeLink.selectHead("span[class=govuk-visually-hidden]").text mustBe CheckYourAnswersMessages.changeTradingStartDate
              changeLink.attr("href") mustBe routes.BusinessStartDateController.show(id = "testId", isEditMode = true).url
            }
            "has an add link with correct content" in new SetupIncomplete(SelfEmploymentsCYAModel(testId, totalSelfEmployments = 1)) {
              val addLink: Element = document.getSummaryList().getSummaryListRow(2).getSummaryListActions.selectHead("a")
              addLink.selectHead("span[aria-hidden=true]").text mustBe CheckYourAnswersMessages.add
              addLink.selectHead("span[class=govuk-visually-hidden]").text mustBe CheckYourAnswersMessages.addTradingStartDate
            }
          }

          "has a row for the business name" which {
            "has a label to identify it" in new SetupIncomplete {
              document.getSummaryList().getSummaryListRow(1).getSummaryListKey.text mustBe CheckYourAnswersMessages.businessName
            }
            "has a answer the user gave" in new SetupIncomplete {
              document.getSummaryList().getSummaryListRow(1).getSummaryListValue.text mustBe "ABC Limited"
            }
            "has a change link" in new SetupIncomplete {
              val changeLink: Element = document.getSummaryList().getSummaryListRow(1).getSummaryListActions.selectHead("a")
              changeLink.selectHead("span[aria-hidden=true]").text mustBe CheckYourAnswersMessages.change
              changeLink.selectHead("span[class=govuk-visually-hidden]").text mustBe CheckYourAnswersMessages.changeBusinessName
              changeLink.attr("href") mustBe routes.BusinessNameController.show(id = "testId", isEditMode = true).url
            }
            "has an add link with correct content" in new SetupIncomplete(SelfEmploymentsCYAModel(testId, totalSelfEmployments = 1)) {
              val addLink: Element = document.getSummaryList().getSummaryListRow(1).getSummaryListActions.selectHead("a")
              addLink.selectHead("span[aria-hidden=true]").text mustBe CheckYourAnswersMessages.add
              addLink.selectHead("span[class=govuk-visually-hidden]").text mustBe CheckYourAnswersMessages.addBusinessName
            }
          }

          "has a row for the business trade" which {
            "has a label to identify it" in new SetupIncomplete {
              document.getSummaryList().getSummaryListRow(3).getSummaryListKey.text mustBe CheckYourAnswersMessages.businessTrade
            }
            "has a answer the user gave" in new SetupIncomplete {
              document.getSummaryList().getSummaryListRow(3).getSummaryListValue.text mustBe "Plumbing"
            }
            "has a change link" in new SetupIncomplete {
              val changeLink: Element = document.getSummaryList().getSummaryListRow(3).getSummaryListActions.selectHead("a")
              changeLink.selectHead("span[aria-hidden=true]").text mustBe CheckYourAnswersMessages.change
              changeLink.selectHead("span[class=govuk-visually-hidden]").text mustBe CheckYourAnswersMessages.changeBusinessTrade
              changeLink.attr("href") mustBe routes.BusinessTradeNameController.show(id = "testId", isEditMode = true).url
            }
            "has an add link with correct content" in new SetupIncomplete(SelfEmploymentsCYAModel(testId, totalSelfEmployments = 1)) {
              val addLink: Element = document.getSummaryList().getSummaryListRow(3).getSummaryListActions.selectHead("a")
              addLink.selectHead("span[aria-hidden=true]").text mustBe CheckYourAnswersMessages.add
              addLink.selectHead("span[class=govuk-visually-hidden]").text mustBe CheckYourAnswersMessages.addTypeOfTrade
            }
          }

          "has a row for the business address" which {
            "has a label to identify it" in new SetupIncomplete {
              document.getSummaryList().getSummaryListRow(4).getSummaryListKey.text mustBe CheckYourAnswersMessages.businessAddress
            }
            "has a answer the user gave" in new SetupIncomplete {
              document.getSummaryList().getSummaryListRow(4).getSummaryListValue.text mustBe "line, line9, line99, TF3 4NT"
            }
            "has a change link" in new SetupIncomplete {
              val changeLink: Element = document.getSummaryList().getSummaryListRow(4).getSummaryListActions.selectHead("a")
              changeLink.selectHead("span[aria-hidden=true]").text mustBe CheckYourAnswersMessages.change
              changeLink.selectHead("span[class=govuk-visually-hidden]").text mustBe CheckYourAnswersMessages.changeBusinessAddress
              changeLink.attr("href") mustBe routes.AddressLookupRoutingController.initialiseAddressLookupJourney(businessId = "testId", isEditMode = true).url
            }
            "has an add link with correct content" in new SetupIncomplete(SelfEmploymentsCYAModel(testId, totalSelfEmployments = 1)) {
              val addLink: Element = document.getSummaryList().getSummaryListRow(4).getSummaryListActions.selectHead("a")
              addLink.selectHead("span[aria-hidden=true]").text mustBe CheckYourAnswersMessages.add
              addLink.selectHead("span[class=govuk-visually-hidden]").text mustBe CheckYourAnswersMessages.addBusinessAddress
            }
          }

          "has a row for the business accounting method" which {
            "has a label to identify it" in new SetupIncomplete {
              document.getSummaryList().getSummaryListRow(5).getSummaryListKey.text mustBe CheckYourAnswersMessages.accountingMethod
            }
            "has a answer the user gave" in new SetupIncomplete {
              document.getSummaryList().getSummaryListRow(5).getSummaryListValue.text mustBe ""
            }
            "has a incomplete link" in new SetupIncomplete {
              val changeLink: Element = document.getSummaryList().getSummaryListRow(5).getSummaryListActions.selectHead("a")
              changeLink.selectHead("span[aria-hidden=true]").text mustBe CheckYourAnswersMessages.add
              changeLink.selectHead("span[class=govuk-visually-hidden]").text mustBe CheckYourAnswersMessages.addAccountingMethod
              changeLink.attr("href") mustBe routes.BusinessAccountingMethodController.show(id = "testId", isEditMode = true).url
            }
            "has a change link with correct content" in new SetupIncomplete(
              SelfEmploymentsCYAModel(testId, accountingMethod = Some(Cash), totalSelfEmployments = 1)
            ) {
              val addLink: Element = document.getSummaryList().getSummaryListRow(5).getSummaryListActions.selectHead("a")
              addLink.selectHead("span[aria-hidden=true]").text mustBe CheckYourAnswersMessages.change
              addLink.selectHead("span[class=govuk-visually-hidden]").text mustBe CheckYourAnswersMessages.changeAccountingMethod
            }
          }

          "the confirm and continue button is not disabled" in new SetupIncomplete {
            val buttonLink: Element = document.selectHead(".govuk-button")
            buttonLink.hasAttr("disabled") mustBe false
          }

          "have a save and come back later button" in new SetupIncomplete {
            val buttonLink: Element = document.mainContent.selectHead(".govuk-button--secondary")
            buttonLink.text mustBe CheckYourAnswersMessages.saveAndBack
            buttonLink.attr("href") mustBe
              appConfig.subscriptionFrontendClientProgressSavedUrl + "?location=sole-trader-check-your-answers"
          }

          "have a back button" in new SetupIncomplete {
            document.getBackLinkByClass.text mustBe CheckYourAnswersMessages.back
          }

        }

      }
    }


  }

}
