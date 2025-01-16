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

  val implicitDateFormatter: ImplicitDateFormatter = app.injector.instanceOf[ImplicitDateFormatterImpl]
  val checkYourAnswers: SelfEmployedCYA = app.injector.instanceOf[SelfEmployedCYA]

  val testId: String = "testId"

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
      "in edit mode" which {
        "as the initial business" when {
          "all data is complete" in {
            document().mainContent.mustHaveSummaryList(".govuk-summary-list")(Seq(
              SummaryListRowValues(
                key = CheckYourAnswersMessages.businessTradeStreamline,
                value = Some("Plumbing"),
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.FirstIncomeSourceController.show(testId, isEditMode = true).url,
                    text = s"${CheckYourAnswersMessages.change} ${CheckYourAnswersMessages.businessTradeStreamline}",
                    visuallyHidden = CheckYourAnswersMessages.businessTradeStreamline
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.businessName,
                value = Some("ABC Limited"),
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.FirstIncomeSourceController.show(testId, isEditMode = true).url,
                    text = s"${CheckYourAnswersMessages.change} ${CheckYourAnswersMessages.businessName}",
                    visuallyHidden = CheckYourAnswersMessages.businessName
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.tradingStartDateStreamline,
                value = Some("1 January 2018"),
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.FirstIncomeSourceController.show(testId, isEditMode = true).url,
                    text = s"${CheckYourAnswersMessages.change} ${CheckYourAnswersMessages.tradingStartDateStreamline}",
                    visuallyHidden = CheckYourAnswersMessages.tradingStartDateStreamline
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.accountingMethodStreamline,
                value = Some("Cash basis accounting"),
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.FirstIncomeSourceController.show(testId, isEditMode = true).url,
                    text = s"${CheckYourAnswersMessages.change} ${CheckYourAnswersMessages.accountingMethodStreamline}",
                    visuallyHidden = CheckYourAnswersMessages.accountingMethodStreamline
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.businessAddressStreamline,
                value = Some("line 1 TF3 4NT"),
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.AddressLookupRoutingController.initialiseAddressLookupJourney(testId, isEditMode = true).url,
                    text = s"${CheckYourAnswersMessages.change} ${CheckYourAnswersMessages.businessAddressStreamline}",
                    visuallyHidden = CheckYourAnswersMessages.businessAddressStreamline
                  )
                )
              )
            ))
          }
          "all data is missing" in {
            document(emptySelfEmploymentsCYAModel).mainContent.mustHaveSummaryList(".govuk-summary-list")(Seq(
              SummaryListRowValues(
                key = CheckYourAnswersMessages.businessTradeStreamline,
                value = None,
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.FirstIncomeSourceController.show(testId, isEditMode = true).url,
                    text = s"${CheckYourAnswersMessages.add} ${CheckYourAnswersMessages.businessTradeStreamline}",
                    visuallyHidden = CheckYourAnswersMessages.businessTradeStreamline
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.businessName,
                value = None,
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.FirstIncomeSourceController.show(testId, isEditMode = true).url,
                    text = s"${CheckYourAnswersMessages.add} ${CheckYourAnswersMessages.businessName}",
                    visuallyHidden = CheckYourAnswersMessages.businessName
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.tradingStartDateStreamline,
                value = None,
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.FirstIncomeSourceController.show(testId, isEditMode = true).url,
                    text = s"${CheckYourAnswersMessages.add} ${CheckYourAnswersMessages.tradingStartDateStreamline}",
                    visuallyHidden = CheckYourAnswersMessages.tradingStartDateStreamline
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.accountingMethodStreamline,
                value = None,
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.FirstIncomeSourceController.show(testId, isEditMode = true).url,
                    text = s"${CheckYourAnswersMessages.add} ${CheckYourAnswersMessages.accountingMethodStreamline}",
                    visuallyHidden = CheckYourAnswersMessages.accountingMethodStreamline
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.businessAddressStreamline,
                value = None,
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.AddressLookupRoutingController.initialiseAddressLookupJourney(testId, isEditMode = true).url,
                    text = s"${CheckYourAnswersMessages.add} ${CheckYourAnswersMessages.businessAddressStreamline}",
                    visuallyHidden = CheckYourAnswersMessages.businessAddressStreamline
                  )
                )
              )
            ))
          }
        }
        "as a subsequent business" when {
          "all data is complete" in {
            document(fullSelfEmploymentsCYAModel.copy(isFirstBusiness = false)).mainContent.mustHaveSummaryList(".govuk-summary-list")(Seq(
              SummaryListRowValues(
                key = CheckYourAnswersMessages.businessTradeStreamline,
                value = Some("Plumbing"),
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.NextIncomeSourceController.show(testId, isEditMode = true).url,
                    text = s"${CheckYourAnswersMessages.change} ${CheckYourAnswersMessages.businessTradeStreamline}",
                    visuallyHidden = CheckYourAnswersMessages.businessTradeStreamline
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.businessName,
                value = Some("ABC Limited"),
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.NextIncomeSourceController.show(testId, isEditMode = true).url,
                    text = s"${CheckYourAnswersMessages.change} ${CheckYourAnswersMessages.businessName}",
                    visuallyHidden = CheckYourAnswersMessages.businessName
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.tradingStartDateStreamline,
                value = Some("1 January 2018"),
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.NextIncomeSourceController.show(testId, isEditMode = true).url,
                    text = s"${CheckYourAnswersMessages.change} ${CheckYourAnswersMessages.tradingStartDateStreamline}",
                    visuallyHidden = CheckYourAnswersMessages.tradingStartDateStreamline
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.businessAddressStreamline,
                value = Some("line 1 TF3 4NT"),
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.AddressLookupRoutingController.initialiseAddressLookupJourney(testId, isEditMode = true).url,
                    text = s"${CheckYourAnswersMessages.change} ${CheckYourAnswersMessages.businessAddressStreamline}",
                    visuallyHidden = CheckYourAnswersMessages.businessAddressStreamline
                  )
                )
              )
            ))
          }
          "all data is missing" in {
            document(emptySelfEmploymentsCYAModel.copy(isFirstBusiness = false)).mainContent.mustHaveSummaryList(".govuk-summary-list")(Seq(
              SummaryListRowValues(
                key = CheckYourAnswersMessages.businessTradeStreamline,
                value = None,
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.NextIncomeSourceController.show(testId, isEditMode = true).url,
                    text = s"${CheckYourAnswersMessages.add} ${CheckYourAnswersMessages.businessTradeStreamline}",
                    visuallyHidden = CheckYourAnswersMessages.businessTradeStreamline
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.businessName,
                value = None,
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.NextIncomeSourceController.show(testId, isEditMode = true).url,
                    text = s"${CheckYourAnswersMessages.add} ${CheckYourAnswersMessages.businessName}",
                    visuallyHidden = CheckYourAnswersMessages.businessName
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.tradingStartDateStreamline,
                value = None,
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.NextIncomeSourceController.show(testId, isEditMode = true).url,
                    text = s"${CheckYourAnswersMessages.add} ${CheckYourAnswersMessages.tradingStartDateStreamline}",
                    visuallyHidden = CheckYourAnswersMessages.tradingStartDateStreamline
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.businessAddressStreamline,
                value = None,
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.AddressLookupRoutingController.initialiseAddressLookupJourney(testId, isEditMode = true).url,
                    text = s"${CheckYourAnswersMessages.add} ${CheckYourAnswersMessages.businessAddressStreamline}",
                    visuallyHidden = CheckYourAnswersMessages.businessAddressStreamline
                  )
                )
              )
            ))
          }
        }
      }

      "in global edit mode" which {
        "as the initial business" when {
          "all data is complete" in {
            document(isGlobalEdit = true).mainContent.mustHaveSummaryList(".govuk-summary-list")(Seq(
              SummaryListRowValues(
                key = CheckYourAnswersMessages.businessTradeStreamline,
                value = Some("Plumbing"),
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.FirstIncomeSourceController.show(testId, isEditMode = true, isGlobalEdit = true).url,
                    text = s"${CheckYourAnswersMessages.change} ${CheckYourAnswersMessages.businessTradeStreamline}",
                    visuallyHidden = CheckYourAnswersMessages.businessTradeStreamline
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.businessName,
                value = Some("ABC Limited"),
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.FirstIncomeSourceController.show(testId, isEditMode = true, isGlobalEdit = true).url,
                    text = s"${CheckYourAnswersMessages.change} ${CheckYourAnswersMessages.businessName}",
                    visuallyHidden = CheckYourAnswersMessages.businessName
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.tradingStartDateStreamline,
                value = Some("1 January 2018"),
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.FirstIncomeSourceController.show(testId, isEditMode = true, isGlobalEdit = true).url,
                    text = s"${CheckYourAnswersMessages.change} ${CheckYourAnswersMessages.tradingStartDateStreamline}",
                    visuallyHidden = CheckYourAnswersMessages.tradingStartDateStreamline
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.accountingMethodStreamline,
                value = Some("Cash basis accounting"),
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.FirstIncomeSourceController.show(testId, isEditMode = true, isGlobalEdit = true).url,
                    text = s"${CheckYourAnswersMessages.change} ${CheckYourAnswersMessages.accountingMethodStreamline}",
                    visuallyHidden = CheckYourAnswersMessages.accountingMethodStreamline
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.businessAddressStreamline,
                value = Some("line 1 TF3 4NT"),
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.AddressLookupRoutingController.initialiseAddressLookupJourney(testId, isEditMode = true, isGlobalEdit = true).url,
                    text = s"${CheckYourAnswersMessages.change} ${CheckYourAnswersMessages.businessAddressStreamline}",
                    visuallyHidden = CheckYourAnswersMessages.businessAddressStreamline
                  )
                )
              )
            ))
          }
        }
        "as a subsequent business" when {
          "all data is complete" in {
            document(fullSelfEmploymentsCYAModel.copy(isFirstBusiness = false), isGlobalEdit = true).mainContent.mustHaveSummaryList(".govuk-summary-list")(Seq(
              SummaryListRowValues(
                key = CheckYourAnswersMessages.businessTradeStreamline,
                value = Some("Plumbing"),
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.NextIncomeSourceController.show(testId, isEditMode = true, isGlobalEdit = true).url,
                    text = s"${CheckYourAnswersMessages.change} ${CheckYourAnswersMessages.businessTradeStreamline}",
                    visuallyHidden = CheckYourAnswersMessages.businessTradeStreamline
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.businessName,
                value = Some("ABC Limited"),
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.NextIncomeSourceController.show(testId, isEditMode = true, isGlobalEdit = true).url,
                    text = s"${CheckYourAnswersMessages.change} ${CheckYourAnswersMessages.businessName}",
                    visuallyHidden = CheckYourAnswersMessages.businessName
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.tradingStartDateStreamline,
                value = Some("1 January 2018"),
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.NextIncomeSourceController.show(testId, isEditMode = true, isGlobalEdit = true).url,
                    text = s"${CheckYourAnswersMessages.change} ${CheckYourAnswersMessages.tradingStartDateStreamline}",
                    visuallyHidden = CheckYourAnswersMessages.tradingStartDateStreamline
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.businessAddressStreamline,
                value = Some("line 1 TF3 4NT"),
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.AddressLookupRoutingController.initialiseAddressLookupJourney(testId, isEditMode = true, isGlobalEdit = true).url,
                    text = s"${CheckYourAnswersMessages.change} ${CheckYourAnswersMessages.businessAddressStreamline}",
                    visuallyHidden = CheckYourAnswersMessages.businessAddressStreamline
                  )
                )
              )
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
    val continue = "Continue"
    val saveAndBack = "Save and come back later"
    val change = "Change"
    val add = "Add"
    val tradingStartDate = "Trading start date"
    val businessName = "Business name"
    val businessAddress = "Address"
    val businessTrade = "Type of trade"
    val accountingMethod = "Accounting method for sole trader income"
    val yes = "Yes"
    val no = "No"
    val businessTradeStreamline = "Trade"
    val tradingStartDateStreamline = "Start date"
    val accountingMethodStreamline = "Accounting method"
    val businessAddressStreamline = "Address"
  }
}