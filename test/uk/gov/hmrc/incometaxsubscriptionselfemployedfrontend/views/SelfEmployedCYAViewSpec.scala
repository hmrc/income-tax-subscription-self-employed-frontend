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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.individual.routes
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.{ImplicitDateFormatter, ImplicitDateFormatterImpl, ViewSpec}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.SelfEmployedCYA

class SelfEmployedCYAViewSpec extends ViewSpec {

  val checkYourAnswers: SelfEmployedCYA = app.injector.instanceOf[SelfEmployedCYA]
  val implicitDateFormatter: ImplicitDateFormatter = app.injector.instanceOf[ImplicitDateFormatterImpl]
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

  val multiBusinessCYAModel: SelfEmploymentsCYAModel = fullSelfEmploymentsCYAModel.copy(totalSelfEmployments = 2)

  def page(answers: SelfEmploymentsCYAModel = fullSelfEmploymentsCYAModel): HtmlFormat.Appendable = {
    checkYourAnswers(
      answers,
      testCall,
      Some(testBackUrl),
      isGlobalEdit = true
    )(FakeRequest(), implicitly)
  }

  def document(answers: SelfEmploymentsCYAModel = fullSelfEmploymentsCYAModel): Document = {
    Jsoup.parse(page(answers).body)
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
        "the answers are complete" in {
          document().mainContent.mustHaveSummaryList(".govuk-summary-list")(
            rows = Seq(
              SummaryListRowValues(
                key = CheckYourAnswersMessages.businessName,
                value = Some("ABC Limited"),
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.BusinessNameController.show(testId, isEditMode = true, isGlobalEdit = true).url,
                    text = s"${CheckYourAnswersMessages.change} ${CheckYourAnswersMessages.businessName}",
                    visuallyHidden = CheckYourAnswersMessages.businessName
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.tradingStartDate,
                value = Some("1 January 2018"),
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.BusinessStartDateController.show(testId, isEditMode = true, isGlobalEdit = true).url,
                    text = s"${CheckYourAnswersMessages.change} ${CheckYourAnswersMessages.tradingStartDate}",
                    visuallyHidden = CheckYourAnswersMessages.tradingStartDate
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.businessTrade,
                value = Some("Plumbing"),
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.BusinessTradeNameController.show(testId, isEditMode = true, isGlobalEdit = true).url,
                    text = s"${CheckYourAnswersMessages.change} ${CheckYourAnswersMessages.businessTrade}",
                    visuallyHidden = CheckYourAnswersMessages.businessTrade
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.businessAddress,
                value = Some("line 1 TF3 4NT"),
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.AddressLookupRoutingController.initialiseAddressLookupJourney(testId, isEditMode = true, isGlobalEdit = true).url,
                    text = s"${CheckYourAnswersMessages.change} ${CheckYourAnswersMessages.businessAddress}",
                    visuallyHidden = CheckYourAnswersMessages.businessAddress
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.accountingMethod,
                value = Some("Cash basis accounting"),
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.BusinessAccountingMethodController.show(testId, isEditMode = true, isGlobalEdit = true).url,
                    text = s"${CheckYourAnswersMessages.change} ${CheckYourAnswersMessages.accountingMethod}",
                    visuallyHidden = CheckYourAnswersMessages.accountingMethod
                  )
                )
              )
            )
          )
        }
        "there exists multiple businesses" in {
          document(multiBusinessCYAModel).mainContent.mustHaveSummaryList(".govuk-summary-list")(
            rows = Seq(
              SummaryListRowValues(
                key = CheckYourAnswersMessages.businessName,
                value = Some("ABC Limited"),
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.BusinessNameController.show(testId, isEditMode = true, isGlobalEdit = true).url,
                    text = s"${CheckYourAnswersMessages.change} ${CheckYourAnswersMessages.businessName}",
                    visuallyHidden = CheckYourAnswersMessages.businessName
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.tradingStartDate,
                value = Some("1 January 2018"),
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.BusinessStartDateController.show(testId, isEditMode = true, isGlobalEdit = true).url,
                    text = s"${CheckYourAnswersMessages.change} ${CheckYourAnswersMessages.tradingStartDate}",
                    visuallyHidden = CheckYourAnswersMessages.tradingStartDate
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.businessTrade,
                value = Some("Plumbing"),
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.BusinessTradeNameController.show(testId, isEditMode = true, isGlobalEdit = true).url,
                    text = s"${CheckYourAnswersMessages.change} ${CheckYourAnswersMessages.businessTrade}",
                    visuallyHidden = CheckYourAnswersMessages.businessTrade
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.businessAddress,
                value = Some("line 1 TF3 4NT"),
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.AddressLookupRoutingController.initialiseAddressLookupJourney(testId, isEditMode = true, isGlobalEdit = true).url,
                    text = s"${CheckYourAnswersMessages.change} ${CheckYourAnswersMessages.businessAddress}",
                    visuallyHidden = CheckYourAnswersMessages.businessAddress
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.accountingMethod,
                value = Some("Cash basis accounting"),
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.ChangeAccountingMethodController.show(testId, isGlobalEdit = true).url,
                    text = s"${CheckYourAnswersMessages.change} ${CheckYourAnswersMessages.accountingMethod}",
                    visuallyHidden = CheckYourAnswersMessages.accountingMethod
                  )
                )
              )
            )
          )
        }
        "the answers are not complete" in {
          document(emptySelfEmploymentsCYAModel).mainContent.mustHaveSummaryList(".govuk-summary-list")(
            rows = Seq(
              SummaryListRowValues(
                key = CheckYourAnswersMessages.businessName,
                value = None,
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.BusinessNameController.show(testId, isEditMode = true, isGlobalEdit = true).url,
                    text = s"${CheckYourAnswersMessages.add} ${CheckYourAnswersMessages.businessName}",
                    visuallyHidden = CheckYourAnswersMessages.businessName
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.tradingStartDate,
                value = None,
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.BusinessStartDateController.show(testId, isEditMode = true, isGlobalEdit = true).url,
                    text = s"${CheckYourAnswersMessages.add} ${CheckYourAnswersMessages.tradingStartDate}",
                    visuallyHidden = CheckYourAnswersMessages.tradingStartDate
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.businessTrade,
                value = None,
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.BusinessTradeNameController.show(testId, isEditMode = true, isGlobalEdit = true).url,
                    text = s"${CheckYourAnswersMessages.add} ${CheckYourAnswersMessages.businessTrade}",
                    visuallyHidden = CheckYourAnswersMessages.businessTrade
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.businessAddress,
                value = None,
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.AddressLookupRoutingController.initialiseAddressLookupJourney(testId, isEditMode = true, isGlobalEdit = true).url,
                    text = s"${CheckYourAnswersMessages.add} ${CheckYourAnswersMessages.businessAddress}",
                    visuallyHidden = CheckYourAnswersMessages.businessAddress
                  )
                )
              ),
              SummaryListRowValues(
                key = CheckYourAnswersMessages.accountingMethod,
                value = None,
                actions = Seq(
                  SummaryListActionValues(
                    href = routes.BusinessAccountingMethodController.show(testId, isEditMode = true, isGlobalEdit = true).url,
                    text = s"${CheckYourAnswersMessages.add} ${CheckYourAnswersMessages.accountingMethod}",
                    visuallyHidden = CheckYourAnswersMessages.accountingMethod
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
          saveAndComeBackLater.attr("href") mustBe s"${appConfig.subscriptionFrontendProgressSavedUrl}?location=sole-trader-check-your-answers"
        }
      }

    }

    object CheckYourAnswersMessages {
      val captionHidden = "This section is"
      val captionVisual = "Sole trader"
      val heading = "Check your answers"
      val title = "Check your answers - sole trader business"
      val confirmAndContinue = "Confirm and continue"
      val saveAndBack = "Save and come back later"
      val change = "Change"
      val add = "Add"
      val tradingStartDate = "Trading start date"
      val businessName = "Business name"
      val businessAddress = "Address"
      val businessTrade = "Type of trade"
      val accountingMethod = "Accounting method for sole trader income"
    }
}
