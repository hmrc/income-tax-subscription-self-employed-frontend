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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitch.StartDateBeforeLimit
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.routes
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.{AccountingPeriodUtil, ImplicitDateFormatter, ImplicitDateFormatterImpl, ViewSpec}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.SelfEmployedCYA

import java.time.format.DateTimeFormatter

class SelfEmployedCYAViewSpec extends ViewSpec with FeatureSwitching {

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(StartDateBeforeLimit)
  }

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

    "have a summary of the users answers" when {
      "the start date before limit feature switch is enabled" when {
        "start date is a date older than the limit" in {
          enable(StartDateBeforeLimit)

          document(emptySelfEmploymentsCYAModel.copy(businessStartDate = Some(olderThanLimitDate))).mainContent.mustHaveSummaryList(".govuk-summary-list")(Seq(
            tradeRow(value = None, editMode = true),
            nameRow(value = None, editMode = true),
            startDateRow(value = Some(CheckYourAnswersMessages.beforeLimit), editMode = true),
            accountingMethodRow(value = None, editMode = true),
            addressRow(value = None, editMode = true)
          ))
        }
        "start date is not older than the limit" in {
          enable(StartDateBeforeLimit)

          document(emptySelfEmploymentsCYAModel.copy(businessStartDate = Some(limitDate))).mainContent.mustHaveSummaryList(".govuk-summary-list")(Seq(
            tradeRow(value = None, editMode = true),
            nameRow(value = None, editMode = true),
            startDateRow(value = Some(limitDate.toLocalDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))), editMode = true),
            accountingMethodRow(value = None, editMode = true),
            addressRow(value = None, editMode = true)
          ))
        }
      }
      "the start date before limit feature switch is disabled" when {
        "start date is a date older than the limit" in {
          document(emptySelfEmploymentsCYAModel.copy(businessStartDate = Some(olderThanLimitDate))).mainContent.mustHaveSummaryList(".govuk-summary-list")(Seq(
            tradeRow(value = None, editMode = true),
            nameRow(value = None, editMode = true),
            startDateRow(value = Some(olderThanLimitDate.toLocalDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))), editMode = true),
            accountingMethodRow(value = None, editMode = true),
            addressRow(value = None, editMode = true)
          ))
        }
        "start date is not older than the limit" in {
          document(emptySelfEmploymentsCYAModel.copy(businessStartDate = Some(limitDate))).mainContent.mustHaveSummaryList(".govuk-summary-list")(Seq(
            tradeRow(value = None, editMode = true),
            nameRow(value = None, editMode = true),
            startDateRow(value = Some(limitDate.toLocalDate.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))), editMode = true),
            accountingMethodRow(value = None, editMode = true),
            addressRow(value = None, editMode = true)
          ))
        }
      }

      "in edit mode" which {
        "as the initial business" when {
          "all data is complete" in {
            document().mainContent.mustHaveSummaryList(".govuk-summary-list")(Seq(
              tradeRow(value = Some("Plumbing"), editMode = true),
              nameRow(value = Some("ABC Limited"), editMode = true),
              startDateRow(value = Some("1 January 2018"), editMode = true),
              accountingMethodRow(value = Some("Cash basis accounting"), editMode = true),
              addressRow(value = Some("line 1 TF3 4NT"), editMode = true)
            ))
          }
          "all data is missing" in {
            document(emptySelfEmploymentsCYAModel).mainContent.mustHaveSummaryList(".govuk-summary-list")(Seq(
              tradeRow(value = None, editMode = true),
              nameRow(value = None, editMode = true),
              startDateRow(value = None, editMode = true),
              accountingMethodRow(value = None, editMode = true),
              addressRow(value = None, editMode = true)
            ))
          }
          "start date is before limit field is present" which {
            "is true" in {
              document(fullSelfEmploymentsCYAModel.copy(startDateBeforeLimit = Some(true))).mainContent.mustHaveSummaryList(".govuk-summary-list")(Seq(
                tradeRow(value = Some("Plumbing"), editMode = true),
                nameRow(value = Some("ABC Limited"), editMode = true),
                startDateRow(value = Some(CheckYourAnswersMessages.beforeLimit), editMode = true),
                accountingMethodRow(value = Some("Cash basis accounting"), editMode = true),
                addressRow(value = Some("line 1 TF3 4NT"), editMode = true)
              ))
            }
            "is false" in {
              document(fullSelfEmploymentsCYAModel.copy(startDateBeforeLimit = Some(false))).mainContent.mustHaveSummaryList(".govuk-summary-list")(Seq(
                tradeRow(value = Some("Plumbing"), editMode = true),
                nameRow(value = Some("ABC Limited"), editMode = true),
                startDateRow(value = Some("1 January 2018"), editMode = true),
                accountingMethodRow(value = Some("Cash basis accounting"), editMode = true),
                addressRow(value = Some("line 1 TF3 4NT"), editMode = true)
              ))
            }
          }
        }
        "as a subsequent business" when {
          "all data is complete" in {
            document(fullSelfEmploymentsCYAModel.copy(isFirstBusiness = false)).mainContent.mustHaveSummaryList(".govuk-summary-list")(Seq(
              tradeRow(value = Some("Plumbing"), editMode = true, isFirstBusiness = false),
              nameRow(value = Some("ABC Limited"), editMode = true, isFirstBusiness = false),
              startDateRow(value = Some("1 January 2018"), editMode = true, isFirstBusiness = false),
              addressRow(value = Some("line 1 TF3 4NT"), editMode = true)
            ))
          }
          "all data is missing" in {
            document(emptySelfEmploymentsCYAModel.copy(isFirstBusiness = false)).mainContent.mustHaveSummaryList(".govuk-summary-list")(Seq(
              tradeRow(value = None, editMode = true, isFirstBusiness = false),
              nameRow(value = None, editMode = true, isFirstBusiness = false),
              startDateRow(value = None, editMode = true, isFirstBusiness = false),
              addressRow(value = None, editMode = true)
            ))
          }
          "start date is before limit field is present" which {
            "is true" in {
              document(fullSelfEmploymentsCYAModel.copy(isFirstBusiness = false, startDateBeforeLimit = Some(true)))
                .mainContent
                .mustHaveSummaryList(".govuk-summary-list")(Seq(
                  tradeRow(value = Some("Plumbing"), editMode = true, isFirstBusiness = false),
                  nameRow(value = Some("ABC Limited"), editMode = true, isFirstBusiness = false),
                  startDateRow(value = Some(CheckYourAnswersMessages.beforeLimit), editMode = true, isFirstBusiness = false),
                  addressRow(value = Some("line 1 TF3 4NT"), editMode = true)
                ))
            }
            "is false" in {
              document(fullSelfEmploymentsCYAModel.copy(isFirstBusiness = false, startDateBeforeLimit = Some(false)))
                .mainContent.mustHaveSummaryList(".govuk-summary-list")(Seq(
                  tradeRow(value = Some("Plumbing"), editMode = true, isFirstBusiness = false),
                  nameRow(value = Some("ABC Limited"), editMode = true, isFirstBusiness = false),
                  startDateRow(value = Some("1 January 2018"), editMode = true, isFirstBusiness = false),
                  addressRow(value = Some("line 1 TF3 4NT"), editMode = true)
                ))
            }
          }
        }
      }

      "in global edit mode" which {
        "as the initial business" when {
          "all data is complete" in {
            document(isGlobalEdit = true).mainContent.mustHaveSummaryList(".govuk-summary-list")(Seq(
              tradeRow(value = Some("Plumbing"), editMode = true, globalEditMode = true),
              nameRow(value = Some("ABC Limited"), editMode = true, globalEditMode = true),
              startDateRow(value = Some("1 January 2018"), editMode = true, globalEditMode = true),
              accountingMethodRow(value = Some("Cash basis accounting"), editMode = true, globalEditMode = true),
              addressRow(value = Some("line 1 TF3 4NT"), editMode = true, globalEditMode = true)
            ))
          }
        }
        "as a subsequent business" when {
          "all data is complete" in {
            document(fullSelfEmploymentsCYAModel.copy(isFirstBusiness = false), isGlobalEdit = true).mainContent.mustHaveSummaryList(".govuk-summary-list")(Seq(
              tradeRow(value = Some("Plumbing"), editMode = true, globalEditMode = true, isFirstBusiness = false),
              nameRow(value = Some("ABC Limited"), editMode = true, globalEditMode = true, isFirstBusiness = false),
              startDateRow(value = Some("1 January 2018"), editMode = true, globalEditMode = true, isFirstBusiness = false),
              addressRow(value = Some("line 1 TF3 4NT"), editMode = true, globalEditMode = true)
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
    businessAddress = Some(Address(Seq(s"line 1"), Some("TF3 4NT"))),
    accountingMethod = Some(Cash),
    totalSelfEmployments = 1,
    isFirstBusiness = true
  )

  lazy val emptySelfEmploymentsCYAModel: SelfEmploymentsCYAModel = SelfEmploymentsCYAModel(
    id = testId,
    totalSelfEmployments = 1,
    isFirstBusiness = true
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

  def simpleSummaryRow(key: String): (Option[String], Boolean, Boolean, Boolean) => SummaryListRowValues = {
    case (value, editMode, globalEditMode, isFirstBusiness) =>
      SummaryListRowValues(
        key = key,
        value = value,
        actions = Seq(
          SummaryListActionValues(
            href = if (isFirstBusiness) {
              routes.FirstIncomeSourceController.show(testId, isEditMode = editMode, isGlobalEdit = globalEditMode).url
            } else {
              routes.NextIncomeSourceController.show(testId, isEditMode = editMode, isGlobalEdit = globalEditMode).url
            },
            text = (if (value.isDefined) CheckYourAnswersMessages.change else CheckYourAnswersMessages.add) + " " + key,
            visuallyHidden = key
          )
        )
      )
  }

  private def tradeRow(value: Option[String], editMode: Boolean = false, globalEditMode: Boolean = false, isFirstBusiness: Boolean = true) = {
    simpleSummaryRow(CheckYourAnswersMessages.trade)(value, editMode, globalEditMode, isFirstBusiness)
  }

  private def nameRow(value: Option[String], editMode: Boolean = false, globalEditMode: Boolean = false, isFirstBusiness: Boolean = true) = {
    simpleSummaryRow(CheckYourAnswersMessages.name)(value, editMode, globalEditMode, isFirstBusiness)
  }

  private def startDateRow(value: Option[String], editMode: Boolean = false, globalEditMode: Boolean = false, isFirstBusiness: Boolean = true) = {
    simpleSummaryRow(CheckYourAnswersMessages.startDate)(value, editMode, globalEditMode, isFirstBusiness)
  }

  private def accountingMethodRow(value: Option[String], editMode: Boolean = false, globalEditMode: Boolean = false, isFirstBusiness: Boolean = true) = {
    simpleSummaryRow(CheckYourAnswersMessages.accountingMethod)(value, editMode, globalEditMode, isFirstBusiness)
  }

  private def addressRow(value: Option[String], editMode: Boolean = false, globalEditMode: Boolean = false) = SummaryListRowValues(
    key = CheckYourAnswersMessages.address,
    value = value,
    actions = Seq(
      SummaryListActionValues(
        href = routes.AddressLookupRoutingController.initialiseAddressLookupJourney(testId, isEditMode = editMode, isGlobalEdit = globalEditMode).url,
        text = (if (value.isDefined) CheckYourAnswersMessages.change else CheckYourAnswersMessages.add) + " " + CheckYourAnswersMessages.address,
        visuallyHidden = CheckYourAnswersMessages.address
      )
    )
  )


}