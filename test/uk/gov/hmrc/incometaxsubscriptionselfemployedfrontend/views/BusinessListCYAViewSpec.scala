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
import play.api.data.{Form, FormError}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.AddAnotherBusinessForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.{ImplicitDateFormatter, ImplicitDateFormatterImpl, ViewSpec}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.{BusinessListCYA => CheckYourAnswers}

class BusinessListCYAViewSpec extends ViewSpec {

  object CheckYourAnswersMessages {
    val heading = "Check your answers"
    val title = "Check your answers"

    def subHeading(businessNumber: Int): String = s"Business $businessNumber"

    def removeBusiness(businessNumber: Int): String = s"Remove business $businessNumber"

    val continue = "Continue"
    val change = "Change"
    val tradingStartDate = "Trading start date of business"
    val changeTradingStartDate = "Change trading start date of business"
    val businessName = "Business name"
    val changeBusinessName = "Change business name"
    val businessAddress = "Business address"
    val changeBusinessAddress = "Change business address"
    val businessTrade = "Business trade"
    val changeBusinessTrade = "Change business trade"
    val addAnotherBusinessHeading = "Do you want to add another sole trader business?"
    val yes = "Yes"
    val no = "No"
  }

  def selfEmploymentData(id: String): SelfEmploymentData = SelfEmploymentData(
    id = id,
    businessStartDate = Some(BusinessStartDate(DateModel("1", "1", "2018"))),
    businessName = Some(BusinessNameModel(s"ABC Limited $id")),
    businessTradeName = Some(BusinessTradeNameModel(s"Plumbing $id")),
    businessAddress = Some(BusinessAddressModel(s"AuditRefId$id", Address(Seq(s"line$id", "line9", "line99"), Some("TF3 4NT")))),
    addressRedirect = Some(s"test address redirect $id")
  )

  val implicitDateFormatter: ImplicitDateFormatter = app.injector.instanceOf[ImplicitDateFormatterImpl]

  val checkYourAnswers: CheckYourAnswers = app.injector.instanceOf[CheckYourAnswers]

  val maxBusinesses: Int = 5

  class Setup(addAnotherForm: Form[AddAnotherBusinessModel] = AddAnotherBusinessForm.addAnotherBusinessForm(1, maxBusinesses),
              businesses: Seq[SelfEmploymentData] = Seq(selfEmploymentData("1"))) {
    val page: HtmlFormat.Appendable = checkYourAnswers(
      addAnotherForm,
      businesses,
      testCall
    )(FakeRequest(), implicitly)

    val document: Document = Jsoup.parse(page.body)
  }

  val testError: FormError = FormError(AddAnotherBusinessForm.addAnotherBusiness, "test error message")

  "Check Your Answers" must {

    "have the correct template details" when {
      "there is no error" in new TemplateViewTest(
        view = checkYourAnswers(
          AddAnotherBusinessForm.addAnotherBusinessForm(1, maxBusinesses),
          Seq(selfEmploymentData("1")),
          testCall
        )(FakeRequest(), implicitly),
        title = CheckYourAnswersMessages.title,
        hasSignOutLink = true
      )
      "there is an error" in new TemplateViewTest(
        view = checkYourAnswers(
          AddAnotherBusinessForm.addAnotherBusinessForm(1, maxBusinesses).withError(testError),
          Seq(selfEmploymentData("1")),
          testCall
        )(FakeRequest(), implicitly),
        title = CheckYourAnswersMessages.title,
        hasSignOutLink = true,
        error = Some(testError)
      )
    }

    "have a heading" in new Setup {
      document.getH1Element.text mustBe CheckYourAnswersMessages.heading
    }

    "have a Form" in new Setup {
      document.getForm.attr("method") mustBe testCall.method
      document.getForm.attr("action") mustBe testCall.url
    }

    "display a single business check your answers" which {

      "has a heading indicating the number of the business" in new Setup {
        document.getH2Element().text mustBe CheckYourAnswersMessages.subHeading(1)
      }

      "has a check your answers table" which {
        "has a row for the trading start date" which {
          "has a label to identify it" in new Setup {
            document.getSummaryList().getSummaryListRow(1).getSummaryListKey.text mustBe CheckYourAnswersMessages.tradingStartDate
          }
          "has a answer the user gave" in new Setup {
            document.getSummaryList().getSummaryListRow(1).getSummaryListValue.text mustBe "1 January 2018"
          }
          "has a change link with correct content" in new Setup {
            val changeLink: Element = document.getSummaryList().getSummaryListRow(1).getSummaryListActions.selectHead("a")
            changeLink.selectHead("span[aria-hidden=true]").text mustBe CheckYourAnswersMessages.change
            changeLink.selectHead("span[class=govuk-visually-hidden]").text mustBe CheckYourAnswersMessages.changeTradingStartDate
            changeLink.attr("href") mustBe routes.BusinessStartDateController.show(id = "1", isEditMode = true).url
          }
        }

        "has a row for the business name" which {
          "has a label to identify it" in new Setup {
            document.getSummaryList().getSummaryListRow(2).getSummaryListKey.text mustBe CheckYourAnswersMessages.businessName
          }
          "has a answer the user gave" in new Setup {
            document.getSummaryList().getSummaryListRow(2).getSummaryListValue.text mustBe "ABC Limited 1"
          }
          "has a change link" in new Setup {
            val changeLink: Element = document.getSummaryList().getSummaryListRow(2).getSummaryListActions.selectHead("a")
            changeLink.selectHead("span[aria-hidden=true]").text mustBe CheckYourAnswersMessages.change
            changeLink.selectHead("span[class=govuk-visually-hidden]").text mustBe CheckYourAnswersMessages.changeBusinessName
            changeLink.attr("href") mustBe routes.BusinessNameController.show(id = "1", isEditMode = true).url
          }
        }

        "has a row for the business trade" which {
          "has a label to identify it" in new Setup {
            document.getSummaryList().getSummaryListRow(3).getSummaryListKey.text mustBe CheckYourAnswersMessages.businessTrade
          }
          "has a answer the user gave" in new Setup {
            document.getSummaryList().getSummaryListRow(3).getSummaryListValue.text mustBe "Plumbing 1"
          }
          "has a change link" in new Setup {
            val changeLink: Element = document.getSummaryList().getSummaryListRow(3).getSummaryListActions.selectHead("a")
            changeLink.selectHead("span[aria-hidden=true]").text mustBe CheckYourAnswersMessages.change
            changeLink.selectHead("span[class=govuk-visually-hidden]").text mustBe CheckYourAnswersMessages.changeBusinessTrade
            changeLink.attr("href") mustBe routes.BusinessTradeNameController.show(id = "1", isEditMode = true).url
          }
        }

        "has a row for the business address" which {
          "has a label to identify it" in new Setup {
            document.getSummaryList().getSummaryListRow(4).getSummaryListKey.text mustBe CheckYourAnswersMessages.businessAddress
          }
          "has a answer the user gave" in new Setup {
            document.getSummaryList().getSummaryListRow(4).getSummaryListValue.text mustBe "line1, line9, line99, TF3 4NT"
          }
          "has a change link" in new Setup {
            val changeLink: Element = document.getSummaryList().getSummaryListRow(4).getSummaryListActions.selectHead("a")
            changeLink.selectHead("span[aria-hidden=true]").text mustBe CheckYourAnswersMessages.change
            changeLink.selectHead("span[class=govuk-visually-hidden]").text mustBe CheckYourAnswersMessages.changeBusinessAddress
            changeLink.attr("href") mustBe routes.AddressLookupRoutingController.initialiseAddressLookupJourney(businessId = "1", isEditMode = true).url
          }
        }

        "has a row for the remove business link" which {
          "has a remove business link" in new Setup {
            val removeLink: Element = document.getElementById("remove-business-1")
            removeLink.text mustBe CheckYourAnswersMessages.removeBusiness(1)
            removeLink.attr("href") mustBe routes.RemoveBusinessController.show("1").url
          }
        }
      }
    }

    "display multiple businesses when passed into the view" which {
      "for the first business" should {
        "have a heading indicating it's the first business" in new Setup(businesses = Seq(selfEmploymentData("1"), selfEmploymentData("2"))) {
          document.getH2Element().text mustBe CheckYourAnswersMessages.subHeading(1)
        }
        "have a check your answers table" which {
          "has a row for the trading start date" which {
            "has a label to identify it" in new Setup(businesses = Seq(selfEmploymentData("1"), selfEmploymentData("2"))) {
              document.getSummaryList().getSummaryListRow(1).getSummaryListKey.text mustBe CheckYourAnswersMessages.tradingStartDate
            }
            "has a answer the user gave" in new Setup(businesses = Seq(selfEmploymentData("1"), selfEmploymentData("2"))) {
              document.getSummaryList().getSummaryListRow(1).getSummaryListValue.text mustBe "1 January 2018"
            }
            "has a change link" in new Setup(businesses = Seq(selfEmploymentData("1"), selfEmploymentData("2"))) {
              val changeLink: Element = document.getSummaryList().getSummaryListRow(1).getSummaryListActions.selectHead("a")
              changeLink.selectHead("span[aria-hidden=true]").text mustBe CheckYourAnswersMessages.change
              changeLink.selectHead("span[class=govuk-visually-hidden]").text mustBe CheckYourAnswersMessages.changeTradingStartDate
              changeLink.attr("href") mustBe routes.BusinessStartDateController.show(id = "1", isEditMode = true).url
            }
          }

          "has a row for the business name" which {
            "has a label to identify it" in new Setup(businesses = Seq(selfEmploymentData("1"), selfEmploymentData("2"))) {
              document.getSummaryList().getSummaryListRow(2).getSummaryListKey.text mustBe CheckYourAnswersMessages.businessName
            }
            "has a answer the user gave" in new Setup(businesses = Seq(selfEmploymentData("1"), selfEmploymentData("2"))) {
              document.getSummaryList().getSummaryListRow(2).getSummaryListValue.text mustBe "ABC Limited 1"
            }
            "has a change link" in new Setup(businesses = Seq(selfEmploymentData("1"), selfEmploymentData("2"))) {
              val changeLink: Element = document.getSummaryList().getSummaryListRow(2).getSummaryListActions.selectHead("a")
              changeLink.selectHead("span[aria-hidden=true]").text mustBe CheckYourAnswersMessages.change
              changeLink.selectHead("span[class=govuk-visually-hidden]").text mustBe CheckYourAnswersMessages.changeBusinessName
              changeLink.attr("href") mustBe routes.BusinessNameController.show(id = "1", isEditMode = true).url
            }
          }

          "has a row for the business trade" which {
            "has a label to identify it" in new Setup(businesses = Seq(selfEmploymentData("1"), selfEmploymentData("2"))) {
              document.getSummaryList().getSummaryListRow(3).getSummaryListKey.text mustBe CheckYourAnswersMessages.businessTrade
            }
            "has a answer the user gave" in new Setup(businesses = Seq(selfEmploymentData("1"), selfEmploymentData("2"))) {
              document.getSummaryList().getSummaryListRow(3).getSummaryListValue.text mustBe "Plumbing 1"
            }
            "has a change link" in new Setup(businesses = Seq(selfEmploymentData("1"), selfEmploymentData("2"))) {
              val changeLink: Element = document.getSummaryList().getSummaryListRow(3).getSummaryListActions.selectHead("a")
              changeLink.selectHead("span[aria-hidden=true]").text mustBe CheckYourAnswersMessages.change
              changeLink.selectHead("span[class=govuk-visually-hidden]").text mustBe CheckYourAnswersMessages.changeBusinessTrade
              changeLink.attr("href") mustBe routes.BusinessTradeNameController.show(id = "1", isEditMode = true).url
            }
          }

          "has a row for the business address" which {
            "has a label to identify it" in new Setup(businesses = Seq(selfEmploymentData("1"), selfEmploymentData("2"))) {
              document.getSummaryList().getSummaryListRow(4).getSummaryListKey.text mustBe CheckYourAnswersMessages.businessAddress
            }
            "has a answer the user gave" in new Setup(businesses = Seq(selfEmploymentData("1"), selfEmploymentData("2"))) {
              document.getSummaryList().getSummaryListRow(4).getSummaryListValue.text mustBe "line1, line9, line99, TF3 4NT"
            }
            "has a change link" in new Setup(businesses = Seq(selfEmploymentData("1"), selfEmploymentData("2"))) {
              val changeLink: Element = document.getSummaryList().getSummaryListRow(4).getSummaryListActions.selectHead("a")
              changeLink.selectHead("span[aria-hidden=true]").text mustBe CheckYourAnswersMessages.change
              changeLink.selectHead("span[class=govuk-visually-hidden]").text mustBe CheckYourAnswersMessages.changeBusinessAddress
              changeLink.attr("href") mustBe routes.AddressLookupRoutingController.initialiseAddressLookupJourney(businessId = "1", isEditMode = true).url
            }
          }

          "has a row for the remove business link" which {
            "has a remove business link" in new Setup(businesses = Seq(selfEmploymentData("1"), selfEmploymentData("2"))) {
              val removeLink: Element = document.getElementById("remove-business-1")
              removeLink.text mustBe CheckYourAnswersMessages.removeBusiness(1)
              removeLink.attr("href") mustBe routes.RemoveBusinessController.show("1").url
            }
          }
        }
      }
      "for the second business" should {
        "have a heading indicating it's the second business" in new Setup(businesses = Seq(selfEmploymentData("1"), selfEmploymentData("2"))) {
          document.getH2Element(nth = 2).text mustBe CheckYourAnswersMessages.subHeading(2)
        }
        "have a check your answers table" which {
          "has a row for the trading start date" which {
            "has a label to identify it" in new Setup(businesses = Seq(selfEmploymentData("1"), selfEmploymentData("2"))) {
              document.getSummaryList(2).getSummaryListRow(1).getSummaryListKey.text mustBe CheckYourAnswersMessages.tradingStartDate
            }
            "has a answer the user gave" in new Setup(businesses = Seq(selfEmploymentData("1"), selfEmploymentData("2"))) {
              document.getSummaryList(2).getSummaryListRow(1).getSummaryListValue.text mustBe "1 January 2018"
            }
            "has a change link" in new Setup(businesses = Seq(selfEmploymentData("1"), selfEmploymentData("2"))) {
              val changeLink: Element = document.getSummaryList(2).getSummaryListRow(1).getSummaryListActions.selectHead("a")
              changeLink.selectHead("span[aria-hidden=true]").text mustBe CheckYourAnswersMessages.change
              changeLink.selectHead("span[class=govuk-visually-hidden]").text mustBe CheckYourAnswersMessages.changeTradingStartDate
              changeLink.attr("href") mustBe routes.BusinessStartDateController.show(id = "2", isEditMode = true).url
            }
          }

          "has a row for the business name" which {
            "has a label to identify it" in new Setup(businesses = Seq(selfEmploymentData("1"), selfEmploymentData("2"))) {
              document.getSummaryList(2).getSummaryListRow(2).getSummaryListKey.text mustBe CheckYourAnswersMessages.businessName
            }
            "has a answer the user gave" in new Setup(businesses = Seq(selfEmploymentData("1"), selfEmploymentData("2"))) {
              document.getSummaryList(2).getSummaryListRow(2).getSummaryListValue.text mustBe "ABC Limited 2"
            }
            "has a change link" in new Setup(businesses = Seq(selfEmploymentData("1"), selfEmploymentData("2"))) {
              val changeLink: Element = document.getSummaryList(2).getSummaryListRow(2).getSummaryListActions.selectHead("a")
              changeLink.selectHead("span[aria-hidden=true]").text mustBe CheckYourAnswersMessages.change
              changeLink.selectHead("span[class=govuk-visually-hidden]").text mustBe CheckYourAnswersMessages.changeBusinessName
              changeLink.attr("href") mustBe routes.BusinessNameController.show(id = "2", isEditMode = true).url
            }
          }

          "has a row for the business trade" which {
            "has a label to identify it" in new Setup(businesses = Seq(selfEmploymentData("1"), selfEmploymentData("2"))) {
              document.getSummaryList(2).getSummaryListRow(3).getSummaryListKey.text mustBe CheckYourAnswersMessages.businessTrade
            }
            "has a answer the user gave" in new Setup(businesses = Seq(selfEmploymentData("1"), selfEmploymentData("2"))) {
              document.getSummaryList(2).getSummaryListRow(3).getSummaryListValue.text mustBe "Plumbing 2"
            }
            "has a change link" in new Setup(businesses = Seq(selfEmploymentData("1"), selfEmploymentData("2"))) {
              val changeLink: Element = document.getSummaryList(2).getSummaryListRow(3).getSummaryListActions.selectHead("a")
              changeLink.selectHead("span[aria-hidden=true]").text mustBe CheckYourAnswersMessages.change
              changeLink.selectHead("span[class=govuk-visually-hidden]").text mustBe CheckYourAnswersMessages.changeBusinessTrade
              changeLink.attr("href") mustBe routes.BusinessTradeNameController.show(id = "2", isEditMode = true).url
            }
          }

          "has a row for the business address" which {
            "has a label to identify it" in new Setup(businesses = Seq(selfEmploymentData("1"), selfEmploymentData("2"))) {
              document.getSummaryList(2).getSummaryListRow(4).getSummaryListKey.text mustBe CheckYourAnswersMessages.businessAddress
            }
            "has a answer the user gave" in new Setup(businesses = Seq(selfEmploymentData("1"), selfEmploymentData("2"))) {
              document.getSummaryList(2).getSummaryListRow(4).getSummaryListValue.text mustBe "line2, line9, line99, TF3 4NT"
            }
            "has a change link" in new Setup(businesses = Seq(selfEmploymentData("1"), selfEmploymentData("2"))) {
              val changeLink: Element = document.getSummaryList(2).getSummaryListRow(4).getSummaryListActions.selectHead("a")
              changeLink.selectHead("span[aria-hidden=true]").text mustBe CheckYourAnswersMessages.change
              changeLink.selectHead("span[class=govuk-visually-hidden]").text mustBe CheckYourAnswersMessages.changeBusinessAddress
              changeLink.attr("href") mustBe routes.AddressLookupRoutingController.initialiseAddressLookupJourney(businessId = "2", isEditMode = true).url
            }
          }

          "has a row for the remove business link" which {
            "has a remove business link" in new Setup(businesses = Seq(selfEmploymentData("1"), selfEmploymentData("2"))) {
              val removeLink: Element = document.getElementById("remove-business-2")
              removeLink.text mustBe CheckYourAnswersMessages.removeBusiness(2)
              removeLink.attr("href") mustBe routes.RemoveBusinessController.show("2").url
            }
          }
        }
      }
    }

    "has a heading to add another sole trader business" in new Setup {
      document.selectHead("legend").text mustBe CheckYourAnswersMessages.addAnotherBusinessHeading
    }

    "have a radioset" which {
      lazy val radioset = new Setup().document.select("fieldset")

      "has only two options" in {
        radioset.select("div.govuk-radios__item").size() mustBe 2
      }

      "has a yes option" which {

        "has the correct label" in {
          radioset.select("""[for="yes-no"]""").text() mustBe CheckYourAnswersMessages.yes
        }

        "has the correct value" in {
          radioset.select("#yes-no").attr("value") mustBe "Yes"
        }
      }

      "has a no option" which {

        "has the correct label" in {
          radioset.select("""[for="yes-no-2"]""").text() mustBe CheckYourAnswersMessages.no
        }

        "has the correct value" in {
          radioset.select("#yes-no-2").attr("value") mustBe "No"
        }
      }
    }

    "have a continue button" in new Setup {
      document.select("button").last.text mustBe CheckYourAnswersMessages.continue
    }

  }

}
