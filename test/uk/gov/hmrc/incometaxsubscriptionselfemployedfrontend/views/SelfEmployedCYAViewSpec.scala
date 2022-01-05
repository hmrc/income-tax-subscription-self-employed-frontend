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
import play.api.data.FormError
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.AddAnotherBusinessForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.{ImplicitDateFormatter, ImplicitDateFormatterImpl, ViewSpec}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.SelfEmployedCYA

class SelfEmployedCYAViewSpec extends ViewSpec {

  object CheckYourAnswersMessages {
    val heading = "Check your details"
    val confirmAndContinue = "Confirm and continue"
    val continue = "Continue"
    val saveAndBack = "Save and come back later"
    val change = "Change"
    val back = "Back"
    val incomplete = "Incomplete"
    val tradingStartDate = "Business trading start date"
    val changeTradingStartDate = "Change Business trading start date"
    val businessName = "Business name"
    val changeBusinessName = "Change Business name"
    val businessAddress = "Business address"
    val changeBusinessAddress = "Change Business address"
    val businessTrade = "Type of trade"
    val changeBusinessTrade = "Change type of trade"
    val accountingMethod = "Accounting method for sole trader income"
    val changeAccountingMethod = "Change Accounting method for sole trader income"
    val addAccountingMethod = "Add Accounting method for sole trader income"
    val addAnotherBusinessHeading = "Do you want to add another sole trader business?"
    val yes = "Yes"
    val no = "No"
  }

  def testSelfEmploymentsCYAModel: SelfEmploymentsCYAModel = SelfEmploymentsCYAModel(
    id = testId,
    businessStartDate = Some(BusinessStartDate(DateModel("1", "1", "2018"))),
    businessName = Some(BusinessNameModel(s"ABC Limited")),
    businessTradeName = Some(BusinessTradeNameModel(s"Plumbing")),
    businessAddress = Some(BusinessAddressModel(s"AuditRefId", Address(Seq(s"line", "line9", "line99"), "TF3 4NT"))),
    businessAddressRedirect = Some(s"test address redirect"),
    accountingMethod = Some(AccountingMethodModel(Cash))
  )

  val implicitDateFormatter: ImplicitDateFormatter = app.injector.instanceOf[ImplicitDateFormatterImpl]

  val checkYourAnswers: SelfEmployedCYA = app.injector.instanceOf[SelfEmployedCYA]

  val backUrl: Option[String] = Some(testBackUrl)

  val testId: String = "testId"

  class SetupIncomplete(answers: SelfEmploymentsCYAModel = SelfEmploymentsCYAModel(
    id = testId,
    businessStartDate = Some(BusinessStartDate(DateModel("1", "1", "2018"))),
    businessName = Some(BusinessNameModel(s"ABC Limited")),
    businessTradeName = Some(BusinessTradeNameModel(s"Plumbing")),
    businessAddress = Some(BusinessAddressModel(s"AuditRefId", Address(Seq(s"line", "line9", "line99"), "TF3 4NT"))),
    businessAddressRedirect = Some(s"test address redirect")
  )) {
    val page: HtmlFormat.Appendable = checkYourAnswers(
      answers,
      testCall,
      backUrl
    )(FakeRequest(), implicitly)

    val document: Document = Jsoup.parse(page.body)
  }

  class SetupComplete(confirmed: Boolean = false) {
    val answers: SelfEmploymentsCYAModel = SelfEmploymentsCYAModel(
      id = testId,
      businessStartDate = Some(BusinessStartDate(DateModel("1", "1", "2018"))),
      businessName = Some(BusinessNameModel(s"ABC Limited")),
      businessTradeName = Some(BusinessTradeNameModel(s"Plumbing")),
      businessAddress = Some(BusinessAddressModel(s"AuditRefId", Address(Seq(s"line", "line9", "line99"), "TF3 4NT"))),
      confirmed = confirmed,
      businessAddressRedirect = Some(s"test address redirect"),
      accountingMethod = Some(AccountingMethodModel(Cash))
    )
    val page: HtmlFormat.Appendable = checkYourAnswers(
      answers,
      testCall,
      backUrl = None
    )(FakeRequest(), implicitly)

    val document: Document = Jsoup.parse(page.body)
  }

  val testError: FormError = FormError(AddAnotherBusinessForm.addAnotherBusiness, "test error message")

  "Check Your Answers" must {

    "have the correct template details" when {
      "there is no error" in new TemplateViewTest(
        view = checkYourAnswers(
          answers = testSelfEmploymentsCYAModel,
          postAction = testCall,
          backUrl
        )(FakeRequest(), implicitly),
        title = CheckYourAnswersMessages.heading,
        hasSignOutLink = true,
        backLink = backUrl
      )
    }

    "have a heading" in new SetupComplete {
      document.getH1Element.text mustBe CheckYourAnswersMessages.heading
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
              document.getSummaryList().getSummaryListRow(5).getSummaryListValue.text mustBe "Cash accounting"
            }
            "has a change link" in new SetupComplete {
              val changeLink: Element = document.getSummaryList().getSummaryListRow(5).getSummaryListActions.selectHead("a")
              changeLink.selectHead("span[aria-hidden=true]").text mustBe CheckYourAnswersMessages.change
              changeLink.selectHead("span[class=govuk-visually-hidden]").text mustBe CheckYourAnswersMessages.changeAccountingMethod
              changeLink.attr("href") mustBe routes.BusinessAccountingMethodController.show(id = Some("testId"), isEditMode = true).url
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
          val buttonLink: Element = document.selectHead(".govuk-button--secondary")
          buttonLink.text mustBe CheckYourAnswersMessages.saveAndBack
          buttonLink.attr("href") mustBe appConfig.subscriptionFrontendProgressSavedUrl
        }

        "not have a save and come back later button if confirmed" in new SetupComplete(true) {
          val buttonLink = document.selectOptionally(".govuk-button--secondary")
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
              changeLink.selectHead("span[aria-hidden=true]").text mustBe CheckYourAnswersMessages.incomplete
              changeLink.selectHead("span[class=govuk-visually-hidden]").text mustBe CheckYourAnswersMessages.addAccountingMethod
              changeLink.attr("href") mustBe routes.BusinessAccountingMethodController.show(id = Some("testId"), isEditMode = true).url
            }
          }

          "have a disabled save and continue button" in new SetupIncomplete {
            val continueButton = document.selectHead(".govuk-button--disabled")
            continueButton.text mustBe CheckYourAnswersMessages.confirmAndContinue
          }

          "have a save and come back later button" in new SetupIncomplete {
            val buttonLink: Element = document.selectHead(".govuk-button--secondary")
            buttonLink.text mustBe CheckYourAnswersMessages.saveAndBack
            buttonLink.attr("href") mustBe appConfig.subscriptionFrontendProgressSavedUrl
          }

          "have a back button" in new SetupIncomplete {
            document.getBackLinkByClass.text mustBe CheckYourAnswersMessages.back
          }

        }

      }
    }


  }

}
