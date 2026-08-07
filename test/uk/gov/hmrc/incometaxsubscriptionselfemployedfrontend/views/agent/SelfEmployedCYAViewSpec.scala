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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.routes
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.{AccountingPeriodUtil, ImplicitDateFormatter, ImplicitDateFormatterImpl, ViewSpec}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.SelfEmployedCYA

import java.time.format.DateTimeFormatter

class SelfEmployedCYAViewSpec extends ViewSpec with FeatureSwitching {

  val implicitDateFormatter: ImplicitDateFormatter = app.injector.instanceOf[ImplicitDateFormatterImpl]
  val checkYourAnswers: SelfEmployedCYA = app.injector.instanceOf[SelfEmployedCYA]

  val olderThanLimitDate: DateModel = DateModel.dateConvert(AccountingPeriodUtil.getStartDateLimit.minusDays(1))
  val limitDate: DateModel = DateModel.dateConvert(AccountingPeriodUtil.getStartDateLimit)

  "Check Your Answers" must {

    "have the correct template details" when {
      "there is no error" in new TemplateViewTest(
        view = page(fullSelfEmploymentsCYAModel, isGlobalEdit = false),
        title = CheckYourAnswersMessages.title,
        isAgent = true,
        hasSignOutLink = true
      )
    }

    "have the correct heading and caption" in {
      document().mainContent.mustHaveHeadingAndCaption(
        heading = CheckYourAnswersMessages.heading,
        caption = CheckYourAnswersMessages.caption,
        isSection = false
      )
    }

    "have an inset note" in {
      document().mainContent.selectHead(".govuk-inset-text").text mustBe CheckYourAnswersMessages.inset
    }

    "have the correct paragraph" in {
      val text = document().mainContent.selectHead(".govuk-body").text
      text mustBe CheckYourAnswersMessages.para
    }

    "have the first subheading" in {
      document().mainContent.selectNth("h2", 2).text mustBe CheckYourAnswersMessages.subheadingOne
    }

    "have the second subheading" in {
      document().mainContent.selectNth("h2", 3).text mustBe CheckYourAnswersMessages.subheadingTwo
    }

    "have the correct business details action" when {
      "business details are present" in {
        val link = businessDetailsAction(answers = fullSelfEmploymentsCYAModel)
        link.ownText().trim mustBe CheckYourAnswersMessages.change
        link.attr("href") mustBe routes.FullIncomeSourceController.show(testId, isEditMode = true).url
        link.selectHead(".govuk-visually-hidden").text mustBe CheckYourAnswersMessages.subheadingOne
      }

      "business details are missing" in {
        val link = businessDetailsAction(answers = emptySelfEmploymentsCYAModel)
        link.ownText().trim mustBe CheckYourAnswersMessages.add
        link.attr("href") mustBe routes.FullIncomeSourceController.show(testId, isEditMode = true).url
        link.selectHead(".govuk-visually-hidden").text mustBe CheckYourAnswersMessages.subheadingOne
      }

      "only the business trade is present" in {
        val answers = emptySelfEmploymentsCYAModel.copy(businessTradeName = Some("Plumbing"))
        val link = businessDetailsAction(answers)
        link.ownText().trim mustBe CheckYourAnswersMessages.change
      }

      "only the business name is present" in {
        val answers = emptySelfEmploymentsCYAModel.copy(businessName = Some("ABC Limited"))
        val link = businessDetailsAction(answers)
        link.ownText().trim mustBe CheckYourAnswersMessages.change
      }

      "only the business start date is present" in {
        val answers = emptySelfEmploymentsCYAModel.copy(businessStartDate = Some(limitDate))
        val link = businessDetailsAction(answers)
        link.ownText().trim mustBe CheckYourAnswersMessages.change
      }

      "in global edit mode" in {
        val link = businessDetailsAction(answers = fullSelfEmploymentsCYAModel, isGlobalEdit = true)
        link.ownText().trim mustBe CheckYourAnswersMessages.change
        link.attr("href") mustBe routes.FullIncomeSourceController.show(testId, isEditMode = true, isGlobalEdit = true).url
      }
    }

    "have the correct business address action" when {
      "a business address is present" in {
        val link = businessAddressAction(answers = fullSelfEmploymentsCYAModel)
        link.ownText().trim mustBe CheckYourAnswersMessages.change
        link.attr("href") mustBe routes.UkAddressConfirmationController.show(testId, isEditMode = true).url

        link.selectHead(".govuk-visually-hidden").text mustBe CheckYourAnswersMessages.subheadingTwo
      }

      "a business address is missing" in {
        val link = businessAddressAction(answers = emptySelfEmploymentsCYAModel)
        link.ownText().trim mustBe CheckYourAnswersMessages.add
        link.attr("href") mustBe routes.UkAddressConfirmationController.show(testId, isEditMode = true).url
        link.selectHead(".govuk-visually-hidden").text mustBe CheckYourAnswersMessages.subheadingTwo
      }

      "in global edit mode" in {
        val link = businessAddressAction(answers = fullSelfEmploymentsCYAModel, isGlobalEdit = true)
        link.ownText().trim mustBe CheckYourAnswersMessages.change
        link.attr("href") mustBe routes.UkAddressConfirmationController.show(testId, isEditMode = true, isGlobalEdit = true).url
      }
    }

    "have a summary of the users answers" when {
      "start date is a date older than the limit" in {
        val content = document(emptySelfEmploymentsCYAModel.copy(businessStartDate = Some(olderThanLimitDate))).mainContent
        content.mustHaveSummaryList("dl.govuk-summary-list:nth-of-type(1)")(
          Seq(
            tradeRow(value = None),
            nameRow(value = None),
            startDateRow(
              value = Some(CheckYourAnswersMessages.beforeLimit)
            )
          )
        )

        content.mustHaveSummaryList("dl.govuk-summary-list:nth-of-type(2)")(Seq(addressRow(value = None)))
      }

      "start date is not older than the limit" in {
        val content = document(emptySelfEmploymentsCYAModel.copy(businessStartDate = Some(limitDate))).mainContent

        content.mustHaveSummaryList("dl.govuk-summary-list:nth-of-type(1)")(
          Seq(
            tradeRow(value = None),
            nameRow(value = None),
            startDateRow(
              value = Some(
                limitDate.toLocalDate.format(
                  DateTimeFormatter.ofPattern("d MMMM yyyy")
                )
              )
            )
          )
        )

        content.mustHaveSummaryList("dl.govuk-summary-list:nth-of-type(2)")(Seq(addressRow(value = None)))
      }

      "the business details are full" in {
        val content = document(answers = fullSelfEmploymentsCYAModel).mainContent

        content.mustHaveSummaryList("dl.govuk-summary-list:nth-of-type(1)")(
          Seq(
            tradeRow(value = Some("Plumbing")),
            nameRow(value = Some("ABC Limited")),
            startDateRow(
              value = Some(CheckYourAnswersMessages.beforeLimit)
            )
          )
        )

        content.mustHaveSummaryList("dl.govuk-summary-list:nth-of-type(2)")(Seq(addressRow(value = Some("line 1 TF3 4NT United Kingdom"))))
      }

      "the business details are empty" in {
        val content = document(answers = emptySelfEmploymentsCYAModel).mainContent

        content.mustHaveSummaryList("dl.govuk-summary-list:nth-of-type(1)")(
          Seq(
            tradeRow(value = None),
            nameRow(value = None),
            startDateRow(value = None)
          )
        )

        content.mustHaveSummaryList("dl.govuk-summary-list:nth-of-type(2)")(Seq(addressRow(value = None)))
      }
    }

    "start date before limit field" when {
      "is true" in {
        val content = document(fullSelfEmploymentsCYAModel.copy(startDateBeforeLimit = Some(true))).mainContent

        content.mustHaveSummaryList("dl.govuk-summary-list:nth-of-type(1)")(
          Seq(
            tradeRow(value = Some("Plumbing")),
            nameRow(value = Some("ABC Limited")),
            startDateRow(
              value = Some(CheckYourAnswersMessages.beforeLimit)
            )
          )
        )
      }

      "is false" in {
        val content = document(
          fullSelfEmploymentsCYAModel.copy(
            startDateBeforeLimit = Some(false),
            businessStartDate = Some(limitDate)
          )
        ).mainContent

        content.mustHaveSummaryList("dl.govuk-summary-list:nth-of-type(1)")(
          Seq(
            tradeRow(value = Some("Plumbing")),
            nameRow(value = Some("ABC Limited")),
            startDateRow(
              value = Some(
                limitDate.toLocalDate.format(
                  DateTimeFormatter.ofPattern("d MMMM yyyy")
                )
              )
            )
          )
        )
      }
    }

    "have a form" which {
      def form: Element = document().mainContent.getForm

      "has the correct attributes" in {
        form.attr("method") mustBe testCall.method
        form.attr("action") mustBe testCall.url
      }
      "has a confirm and continue button" in {
        form.selectNth(".govuk-button", 1).text mustBe CheckYourAnswersMessages.confirmAndContinue
      }
      "has a save and come back later button" in {
        val saveAndComeBackLater = form.selectNth(".govuk-button", 2)
        saveAndComeBackLater.text mustBe CheckYourAnswersMessages.saveAndBack
        saveAndComeBackLater.attr("href") mustBe s"${appConfig.subscriptionFrontendClientProgressSavedUrl}?location=sole-trader-check-your-answers"
      }
    }
  }

  object CheckYourAnswersMessages {
    val caption = "FirstName LastName – ZZ 11 11 11 Z"
    val heading = "Check your answers"
    val inset = "Do not add limited companies or partnerships here."
    val subheadingOne = "Business details"
    val subheadingTwo = "Business address"
    val para = "Add or change any missing or incorrect details, then confirm that the information is correct."
    val title = "Check your answers - sole trader business"
    val confirmAndContinue = "Confirm and continue"
    val saveAndBack = "Save and come back later"
    val change = "Change"
    val add = "Add"
    val name = "Business name"
    val yes = "Yes"
    val no = "No"
    val trade = "Trade"
    val startDate = "Start date"
    val accountingMethod = "Accounting method"
    val address = "Address"
    val beforeLimit = s"Before 6 April ${AccountingPeriodUtil.getStartDateLimit.getYear}"
  }

  lazy val testId: String = "testId"

  lazy val fullSelfEmploymentsCYAModel: SelfEmploymentsCYAModel = SelfEmploymentsCYAModel(
    id = testId,
    businessStartDate = Some(DateModel("1", "1", "2018")),
    businessName = Some(s"ABC Limited"),
    businessTradeName = Some(s"Plumbing"),
    businessAddress = Some(Address(Seq(s"line 1"), Some("TF3 4NT"), Country.UK))
  )

  lazy val emptySelfEmploymentsCYAModel: SelfEmploymentsCYAModel = SelfEmploymentsCYAModel(
    id = testId
  )

  def page(answers: SelfEmploymentsCYAModel, isGlobalEdit: Boolean): HtmlFormat.Appendable = checkYourAnswers(
    answers,
    testCall,
    ClientDetails("FirstName LastName", "ZZ111111Z"),
    isGlobalEdit = isGlobalEdit
  )(FakeRequest(), implicitly)

  def document(answers: SelfEmploymentsCYAModel = fullSelfEmploymentsCYAModel, isGlobalEdit: Boolean = false): Document = {
    Jsoup.parse(page(answers, isGlobalEdit).body)
  }

  private def businessDetailsAction(answers: SelfEmploymentsCYAModel, isGlobalEdit: Boolean = false): Element =
    document(answers = answers, isGlobalEdit = isGlobalEdit).mainContent.selectNth(".section-heading", 1).selectHead("a.govuk-link")

  private def businessAddressAction(answers: SelfEmploymentsCYAModel, isGlobalEdit: Boolean = false): Element =
    document(answers = answers, isGlobalEdit = isGlobalEdit).mainContent.selectNth(".section-heading", 2).selectHead("a.govuk-link")

  def simpleSummaryRow(key: String, value: Option[String]): SummaryListRowValues =
    SummaryListRowValues(
      key = key,
      value = value,
      actions = Nil
    )

  private def tradeRow(value: Option[String]) = simpleSummaryRow(CheckYourAnswersMessages.trade, value)

  private def nameRow(value: Option[String]) = simpleSummaryRow(CheckYourAnswersMessages.name, value)

  private def startDateRow(value: Option[String]) = simpleSummaryRow(CheckYourAnswersMessages.startDate, value)

  private def addressRow(value: Option[String]) = simpleSummaryRow(CheckYourAnswersMessages.address, value)
}
