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
        hasSignOutLink = true,
        backLink = Some(testBackUrl)
      )
    }

    "have the correct heading and caption" in {
      document().mainContent.mustHaveHeadingAndCaption(
        heading = CheckYourAnswersMessages.heading,
        caption = CheckYourAnswersMessages.caption,
        isSection = false
      )
    }

    "have the correct paragraph" in {
      val text = document().mainContent.selectHead(".govuk-body").text
      text mustBe CheckYourAnswersMessages.para
    }

    "have a summary of the users answers" when {
      "start date is a date older than the limit" in {
        document(emptySelfEmploymentsCYAModel.copy(businessStartDate = Some(olderThanLimitDate))).mainContent.mustHaveSummaryList(".govuk-summary-list")(Seq(
          tradeRow(value = None),
          nameRow(value = None),
          startDateRow(value = Some(CheckYourAnswersMessages.beforeLimit)),
          addressRow(value = None)
        ))
      }
      "start date is not older than the limit" in {
        document(emptySelfEmploymentsCYAModel.copy(businessStartDate = Some(limitDate))).mainContent.mustHaveSummaryList(".govuk-summary-list")(Seq(
          tradeRow(value = None),
          nameRow(value = None),
          startDateRow(value = Some(limitDate.toLocalDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")))),
          addressRow(value = None)
        ))
      }

      "the business details are full" in {
        document(answers = fullSelfEmploymentsCYAModel).mainContent.mustHaveSummaryList(".govuk-summary-list")(Seq(
          tradeRow(value = Some("Plumbing")),
          nameRow(value = Some("ABC Limited")),
          startDateRow(value = Some(CheckYourAnswersMessages.beforeLimit)),
          addressRow(value = Some("line 1 TF3 4NT"))
        ))
      }

      "the business details are empty" in {
        document(answers = emptySelfEmploymentsCYAModel).mainContent.mustHaveSummaryList(".govuk-summary-list")(Seq(
          tradeRow(value = None),
          nameRow(value = None),
          startDateRow(value = None),
          addressRow(value = None)
        ))
      }

      "in edit mode" which {
        "all data is complete" in {
          document().mainContent.mustHaveSummaryList(".govuk-summary-list")(Seq(
            tradeRow(value = Some("Plumbing")),
            nameRow(value = Some("ABC Limited")),
            startDateRow(value = Some(CheckYourAnswersMessages.beforeLimit)),
            addressRow(value = Some("line 1 TF3 4NT"))
          ))
        }
        "all data is missing" in {
          document(emptySelfEmploymentsCYAModel).mainContent.mustHaveSummaryList(".govuk-summary-list")(Seq(
            tradeRow(value = None),
            nameRow(value = None),
            startDateRow(value = None),
            addressRow(value = None)
          ))
        }
        "start date is before limit field is present" which {
          "is true" in {
            document(fullSelfEmploymentsCYAModel.copy(startDateBeforeLimit = Some(true))).mainContent.mustHaveSummaryList(".govuk-summary-list")(Seq(
              tradeRow(value = Some("Plumbing")),
              nameRow(value = Some("ABC Limited")),
              startDateRow(value = Some(CheckYourAnswersMessages.beforeLimit)),
              addressRow(value = Some("line 1 TF3 4NT"))
            ))
          }
          "is false" in {
            document(fullSelfEmploymentsCYAModel.copy(startDateBeforeLimit = Some(false), businessStartDate = Some(limitDate)))
              .mainContent.mustHaveSummaryList(".govuk-summary-list")(Seq(
                tradeRow(value = Some("Plumbing")),
                nameRow(value = Some("ABC Limited")),
                startDateRow(value = Some(limitDate.toLocalDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy")))),
                addressRow(value = Some("line 1 TF3 4NT"))
              ))
          }
        }
      }

      "in global edit mode" which {
        "as the initial business" when {
          "all data is complete" in {
            document(isGlobalEdit = true).mainContent.mustHaveSummaryList(".govuk-summary-list")(Seq(
              tradeRow(value = Some("Plumbing"), globalEditMode = true),
              nameRow(value = Some("ABC Limited"), globalEditMode = true),
              startDateRow(value = Some(CheckYourAnswersMessages.beforeLimit), globalEditMode = true),
              addressRow(value = Some("line 1 TF3 4NT"), globalEditMode = true)
            ))
          }
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
  }

  object CheckYourAnswersMessages {
    val caption = "FirstName LastName | ZZ 11 11 11 Z"
    val heading = "Check your answers"
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
    businessAddress = Some(Address(Seq(s"line 1"), Some("TF3 4NT")))
  )

  lazy val emptySelfEmploymentsCYAModel: SelfEmploymentsCYAModel = SelfEmploymentsCYAModel(
    id = testId
  )

  def page(answers: SelfEmploymentsCYAModel, isGlobalEdit: Boolean): HtmlFormat.Appendable = checkYourAnswers(
    answers,
    testCall,
    backUrl = Some(testBackUrl),
    ClientDetails("FirstName LastName", "ZZ111111Z"),
    isGlobalEdit = isGlobalEdit
  )(FakeRequest(), implicitly)

  def document(answers: SelfEmploymentsCYAModel = fullSelfEmploymentsCYAModel, isGlobalEdit: Boolean = false): Document = {
    Jsoup.parse(page(answers, isGlobalEdit).body)
  }

  def simpleSummaryRow(key: String): (Option[String], Boolean) => SummaryListRowValues = {
    case (value, globalEditMode) =>
      SummaryListRowValues(
        key = key,
        value = value,
        actions = Seq(
          SummaryListActionValues(
            href = routes.FullIncomeSourceController.show(testId, isEditMode = true, isGlobalEdit = globalEditMode).url,
            text = (if (value.isDefined) CheckYourAnswersMessages.change else CheckYourAnswersMessages.add) + " " + key,
            visuallyHidden = key
          )
        )
      )
  }

  private def tradeRow(value: Option[String], globalEditMode: Boolean = false) = {
    simpleSummaryRow(CheckYourAnswersMessages.trade)(value, globalEditMode)
  }

  private def nameRow(value: Option[String], globalEditMode: Boolean = false) = {
    simpleSummaryRow(CheckYourAnswersMessages.name)(value, globalEditMode)
  }

  private def startDateRow(value: Option[String], globalEditMode: Boolean = false) = {
    simpleSummaryRow(CheckYourAnswersMessages.startDate)(value, globalEditMode)
  }

  private def addressRow(value: Option[String], globalEditMode: Boolean = false) = SummaryListRowValues(
    key = CheckYourAnswersMessages.address,
    value = value,
    actions = Seq(
      SummaryListActionValues(
        href = routes.AddressLookupRoutingController.initialiseAddressLookupJourney(testId, isEditMode = true, isGlobalEdit = globalEditMode).url,
        text = (if (value.isDefined) CheckYourAnswersMessages.change else CheckYourAnswersMessages.add) + " " + CheckYourAnswersMessages.address,
        visuallyHidden = CheckYourAnswersMessages.address
      )
    )
  )
}
