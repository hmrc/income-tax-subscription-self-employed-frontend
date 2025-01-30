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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views

import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitch.StartDateBeforeLimit
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.individual.routes
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.{AccountingPeriodUtil, ImplicitDateFormatter, ImplicitDateFormatterImpl, ViewSpec}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.SelfEmployedCYA

import java.time.format.DateTimeFormatter

class SelfEmployedCYAViewSpec extends ViewSpec with FeatureSwitching {

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(StartDateBeforeLimit)
  }

  val checkYourAnswers: SelfEmployedCYA = app.injector.instanceOf[SelfEmployedCYA]
  val implicitDateFormatter: ImplicitDateFormatter = app.injector.instanceOf[ImplicitDateFormatterImpl]
  val testId: String = "testId"
  val olderThanLimitDate: DateModel = DateModel.dateConvert(AccountingPeriodUtil.getStartDateLimit.minusDays(1))
  val limitDate: DateModel = DateModel.dateConvert(AccountingPeriodUtil.getStartDateLimit)

  val fullSelfEmploymentsCYAModel: SelfEmploymentsCYAModel = SelfEmploymentsCYAModel(
    id = testId,
    businessStartDate = Some(DateModel("1", "1", "2018")),
    businessName = Some(s"ABC Limited"),
    businessTradeName = Some(s"Plumbing"),
    businessAddress = Some(Address(Seq(s"line 1"), Some("TF3 4NT"))),
    accountingMethod = Some(Cash),
    totalSelfEmployments = 1,
    isFirstBusiness = true
  )

  val emptySelfEmploymentsCYAModel: SelfEmploymentsCYAModel = SelfEmploymentsCYAModel(
    id = testId,
    totalSelfEmployments = 1,
    isFirstBusiness = true
  )

  val multiBusinessCYAModel: SelfEmploymentsCYAModel = fullSelfEmploymentsCYAModel.copy(totalSelfEmployments = 2)

  def page(answers: SelfEmploymentsCYAModel = fullSelfEmploymentsCYAModel, isGlobalEdit: Boolean): HtmlFormat.Appendable = {
    checkYourAnswers(
      answers,
      testCall,
      Some(testBackUrl),
      isGlobalEdit = isGlobalEdit
    )(FakeRequest(), implicitly)
  }

  def document(answers: SelfEmploymentsCYAModel = fullSelfEmploymentsCYAModel, isGlobalEdit: Boolean = false): Document = {
    Jsoup.parse(page(answers, isGlobalEdit).body)
  }

  "Check Your Answers" must {

    "have the correct template details" in new TemplateViewTest(
      view = checkYourAnswers(
        answers = fullSelfEmploymentsCYAModel,
        postAction = testCall,
        Some(testBackUrl),
        isGlobalEdit = true
      )(FakeRequest(), implicitly),
      title = CheckYourAnswersMessages.title,
      hasSignOutLink = true,
      backLink = Some(testBackUrl)
    )

    "have the correct heading and caption" in {
      document().mainContent.mustHaveHeadingAndCaption(
        heading = CheckYourAnswersMessages.heading,
        caption = CheckYourAnswersMessages.captionVisual,
        isSection = true
      )
    }

    "have a summary of the self employment answers" when {
      "in edit mode" when {
        "the answers are complete" in {
          document().mainContent.mustHaveSummaryList(".govuk-summary-list")(
            rows = Seq(
              tradeRow(Some("Plumbing"),
                editMode = true,
                changeHref = Some(routes.BusinessTradeNameController.show(testId, isEditMode = true).url)
              ),
              nameRow(Some("ABC Limited"),
                editMode = true,
                changeHref = Some(routes.BusinessNameController.show(testId, isEditMode = true).url)
              ),
              startDateRow(Some("1 January 2018"),
                editMode = true,
                changeHref = Some(routes.BusinessStartDateController.show(testId, isEditMode = true).url)
              ),
              addressRow(Some("line 1 TF3 4NT"),
                editMode = true
              ),
              accountingMethodRow(Some("Cash basis accounting"), multipleBusinesses = false, editMode = true)
            )
          )
        }
        "there exists multiple businesses" in {
          document(multiBusinessCYAModel).mainContent.mustHaveSummaryList(".govuk-summary-list")(
            rows = Seq(
              tradeRow(Some("Plumbing"),
                editMode = true,
                changeHref = Some(routes.BusinessTradeNameController.show(testId, isEditMode = true).url)
              ),
              nameRow(Some("ABC Limited"),
                editMode = true,
                changeHref = Some(routes.BusinessNameController.show(testId, isEditMode = true).url)
              ),
              startDateRow(Some("1 January 2018"),
                editMode = true,
                changeHref = Some(routes.BusinessStartDateController.show(testId, isEditMode = true).url)
              ),
              addressRow(Some("line 1 TF3 4NT"),
                editMode = true
              ),
              accountingMethodRow(Some("Cash basis accounting"), multipleBusinesses = true, editMode = true)
            )
          )
        }
        "the answers are not complete" in {
          document(emptySelfEmploymentsCYAModel).mainContent.mustHaveSummaryList(".govuk-summary-list")(
            rows = Seq(
              tradeRow(None, editMode = true, changeHref = Some(routes.BusinessTradeNameController.show(testId, isEditMode = true).url)),
              nameRow(None, editMode = true, changeHref = Some(routes.BusinessNameController.show(testId, isEditMode = true).url)),
              startDateRow(None,
                editMode = true,
                changeHref = Some(routes.BusinessStartDateController.show(testId, isEditMode = true).url)),
              addressRow(None, editMode = true),
              accountingMethodRow(None, multipleBusinesses = false, editMode = true)
            )
          )
        }
      }
      "in global mode" when {
        "the answers are complete" in {
          document(isGlobalEdit = true).mainContent.mustHaveSummaryList(".govuk-summary-list")(
            rows = Seq(
              tradeRow(Some("Plumbing"),
                editMode = true,
                changeHref = Some(routes.BusinessTradeNameController.show(testId, isEditMode = true, isGlobalEdit = true).url),
                globalEditMode = true
              ),
              nameRow(Some("ABC Limited"),
                editMode = true,
                changeHref = Some(routes.BusinessNameController.show(testId, isEditMode = true, isGlobalEdit = true).url),
                globalEditMode = true
              ),
              startDateRow(Some("1 January 2018"),
                editMode = true,
                changeHref = Some(routes.BusinessStartDateController.show(testId, isEditMode = true, isGlobalEdit = true).url),
                globalEditMode = true
              ),
              addressRow(Some("line 1 TF3 4NT"),
                editMode = true,
                globalEditMode = true
              ),
              accountingMethodRow(Some("Cash basis accounting"), multipleBusinesses = false, editMode = true, globalEditMode = true)
            )
          )
        }
        "there exists multiple businesses" in {
          document(multiBusinessCYAModel, isGlobalEdit = true).mainContent.mustHaveSummaryList(".govuk-summary-list")(
            rows = Seq(
              tradeRow(Some("Plumbing"),
                editMode = true,
                changeHref = Some(routes.BusinessTradeNameController.show(testId, isEditMode = true, isGlobalEdit = true).url),
                globalEditMode = true
              ),
              nameRow(Some("ABC Limited"),
                editMode = true,
                changeHref = Some(routes.BusinessNameController.show(testId, isEditMode = true, isGlobalEdit = true).url),
                globalEditMode = true
              ),
              startDateRow(Some("1 January 2018"),
                editMode = true,
                changeHref = Some(routes.BusinessStartDateController.show(testId, isEditMode = true, isGlobalEdit = true).url),
                globalEditMode = true
              ),
              addressRow(Some("line 1 TF3 4NT"),
                editMode = true,
                globalEditMode = true
              ),
              accountingMethodRow(Some("Cash basis accounting"), multipleBusinesses = true, editMode = true, globalEditMode = true)
            )
          )
        }
        "the answers are not complete" in {
          document(emptySelfEmploymentsCYAModel, isGlobalEdit = true).mainContent.mustHaveSummaryList(".govuk-summary-list")(
            rows = Seq(
              tradeRow(None,
                editMode = true,
                changeHref = Some(routes.BusinessTradeNameController.show(testId, isEditMode = true, isGlobalEdit = true).url),
                globalEditMode = true),
              nameRow(None,
                editMode = true,
                changeHref = Some(routes.BusinessNameController.show(testId, isEditMode = true, isGlobalEdit = true).url),
                globalEditMode = true),
              startDateRow(None,
                editMode = true,
                changeHref = Some(routes.BusinessStartDateController.show(testId, isEditMode = true, isGlobalEdit = true).url),
                globalEditMode = true),
              addressRow(None,
                editMode = true,
                globalEditMode = true),
              accountingMethodRow(None, multipleBusinesses = false, editMode = true, globalEditMode = true)
            )
          )
        }
      }
      "the start date before limit feature switch is enabled" when {
        "start date is before the limit" in {
          enable(StartDateBeforeLimit)
          document(fullSelfEmploymentsCYAModel.copy(businessStartDate = Some(olderThanLimitDate))).mainContent.mustHaveSummaryList(".govuk-summary-list")(
            rows = Seq(
              tradeRow(Some("Plumbing"),
                editMode = true,
                changeHref = Some(routes.BusinessTradeNameController.show(testId, isEditMode = true).url)
              ),
              nameRow(Some("ABC Limited"),
                editMode = true,
                changeHref = Some(routes.BusinessNameController.show(testId, isEditMode = true).url)
              ),
              startDateRow(value = Some(CheckYourAnswersMessages.startDateBeforeLimitLabel),
                editMode = true,
                changeHref = Some(routes.BusinessStartDateController.show(testId, isEditMode = true).url)
              ),
              addressRow(Some("line 1 TF3 4NT"),
                editMode = true
              ),
              accountingMethodRow(Some("Cash basis accounting"), multipleBusinesses = false, editMode = true)
            )
          )
        }
        "start date is after the limit" in {
          enable(StartDateBeforeLimit)
          document(fullSelfEmploymentsCYAModel.copy(businessStartDate = Some(limitDate))).mainContent.mustHaveSummaryList(".govuk-summary-list")(
            rows = Seq(
              tradeRow(Some("Plumbing"),
                editMode = true,
                changeHref = Some(routes.BusinessTradeNameController.show(testId, isEditMode = true).url)
              ),
              nameRow(Some("ABC Limited"),
                editMode = true,
                changeHref = Some(routes.BusinessNameController.show(testId, isEditMode = true).url)
              ),
              startDateRow(value = Some(limitDate.toLocalDate.format(DateTimeFormatter.ofPattern("d MMMM yyy"))),
                editMode = true,
                changeHref = Some(routes.BusinessStartDateController.show(testId, isEditMode = true).url)
              ),
              addressRow(Some("line 1 TF3 4NT"),
                editMode = true
              ),
              accountingMethodRow(Some("Cash basis accounting"), multipleBusinesses = false, editMode = true)
            )
          )
        }
      }
      "the start date before limit feature switch is disabled" when {
        "start date is before the limit" in {
          document(fullSelfEmploymentsCYAModel.copy(businessStartDate = Some(olderThanLimitDate))).mainContent.mustHaveSummaryList(".govuk-summary-list")(
            rows = Seq(
              tradeRow(Some("Plumbing"),
                editMode = true,
                changeHref = Some(routes.BusinessTradeNameController.show(testId, isEditMode = true).url)
              ),
              nameRow(Some("ABC Limited"),
                editMode = true,
                changeHref = Some(routes.BusinessNameController.show(testId, isEditMode = true).url)
              ),
              startDateRow(value = Some(olderThanLimitDate.toLocalDate.format(DateTimeFormatter.ofPattern("d MMMM yyy"))),
                editMode = true,
                changeHref = Some(routes.BusinessStartDateController.show(testId, isEditMode = true).url)
              ),
              addressRow(Some("line 1 TF3 4NT"),
                editMode = true
              ),
              accountingMethodRow(Some("Cash basis accounting"), multipleBusinesses = false, editMode = true)
            )
          )
        }
        "start date is after the limit" in {
          document(fullSelfEmploymentsCYAModel.copy(businessStartDate = Some(limitDate))).mainContent.mustHaveSummaryList(".govuk-summary-list")(
            rows = Seq(
              tradeRow(Some("Plumbing"),
                editMode = true,
                changeHref = Some(routes.BusinessTradeNameController.show(testId, isEditMode = true).url)
              ),
              nameRow(Some("ABC Limited"),
                editMode = true,
                changeHref = Some(routes.BusinessNameController.show(testId, isEditMode = true).url)
              ),
              startDateRow(value = Some(limitDate.toLocalDate.format(DateTimeFormatter.ofPattern("d MMMM yyy"))),
                editMode = true,
                changeHref = Some(routes.BusinessStartDateController.show(testId, isEditMode = true).url)
              ),
              addressRow(Some("line 1 TF3 4NT"),
                editMode = true
              ),
              accountingMethodRow(Some("Cash basis accounting"), multipleBusinesses = false, editMode = true)
            )
          )
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
        saveAndComeBackLater.attr("href") mustBe s"${appConfig.subscriptionFrontendProgressSavedUrl}?location=sole-trader-check-your-answers"
      }
    }

  }


  def simpleSummaryRow(key: String, changeHref: Option[String]): (Option[String], Boolean, Boolean) => SummaryListRowValues = {
    case (value, editMode, globalEditMode) =>
      SummaryListRowValues(
        key = key,
        value = value,
        actions = Seq(
          SummaryListActionValues(
            href = if (isEnabled(StartDateBeforeLimit)) {
              routes.FullIncomeSourceController.show(testId, isEditMode = editMode, isGlobalEdit = globalEditMode).url
            } else {
              changeHref.getOrElse("")
            },
            text = (if (value.isDefined) CheckYourAnswersMessages.change else CheckYourAnswersMessages.add) + " " + key,
            visuallyHidden = key
          )
        )
      )
  }

  private def tradeRow(value: Option[String], editMode: Boolean = false, globalEditMode: Boolean = false, changeHref: Option[String]) = {
    simpleSummaryRow(CheckYourAnswersMessages.businessTrade, changeHref)(value, editMode, globalEditMode)
  }

  private def nameRow(value: Option[String], editMode: Boolean = false, globalEditMode: Boolean = false, changeHref: Option[String]) = {
    simpleSummaryRow(CheckYourAnswersMessages.businessName, changeHref)(value, editMode, globalEditMode)
  }

  private def startDateRow(value: Option[String], editMode: Boolean = false, globalEditMode: Boolean = false, changeHref: Option[String]) = {
    simpleSummaryRow(CheckYourAnswersMessages.tradingStartDate, changeHref)(value, editMode, globalEditMode)
  }

  private def addressRow(value: Option[String], editMode: Boolean = false, globalEditMode: Boolean = false) = SummaryListRowValues(
    key = CheckYourAnswersMessages.businessAddress,
    value = value,
    actions = Seq(
      SummaryListActionValues(
        href = routes.AddressLookupRoutingController.initialiseAddressLookupJourney(testId, isEditMode = editMode, isGlobalEdit = globalEditMode).url,
        text = (if (value.isDefined) CheckYourAnswersMessages.change else CheckYourAnswersMessages.add) + " " + CheckYourAnswersMessages.businessAddress,
        visuallyHidden = CheckYourAnswersMessages.businessAddress
      )
    )
  )

  private def accountingMethodRow(value: Option[String], multipleBusinesses: Boolean,
                                  editMode: Boolean = false, globalEditMode: Boolean = false) = SummaryListRowValues(
    key = CheckYourAnswersMessages.accountingMethod,
    value = value,
    actions = Seq(
      SummaryListActionValues(
        href = if (multipleBusinesses) {
          routes.ChangeAccountingMethodController.show(testId, isGlobalEdit = globalEditMode).url
        } else {
          routes.BusinessAccountingMethodController.show(testId, isEditMode = editMode, isGlobalEdit = globalEditMode).url
        },
        text = (if (value.isDefined) CheckYourAnswersMessages.change else CheckYourAnswersMessages.add) + s" ${CheckYourAnswersMessages.accountingMethod}",
        visuallyHidden = CheckYourAnswersMessages.accountingMethod
      )
    )
  )

  object CheckYourAnswersMessages {
    val captionVisual = "Sole trader"
    val heading = "Check your answers"
    val title = "Check your answers - sole trader business"
    val confirmAndContinue = "Confirm and continue"
    val saveAndBack = "Save and come back later"
    val change = "Change"
    val add = "Add"
    val tradingStartDate = "Start date"
    val businessName = "Business name"
    val businessAddress = "Address"
    val businessTrade = "Trade"
    val accountingMethod = "Accounting method for sole trader income"
    val startDateBeforeLimitLabel = s"Before 6 April ${AccountingPeriodUtil.getStartDateLimit.getYear}"
  }
}
