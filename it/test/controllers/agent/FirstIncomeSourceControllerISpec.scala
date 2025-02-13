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

package controllers.agent

import connectors.stubs.IncomeTaxSubscriptionConnectorStub._
import connectors.stubs.SessionDataConnectorStub.stubGetSessionData
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.AuthStub._
import play.api.http.Status._
import play.api.libs.json.{JsString, Json}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.{incomeSourcesComplete, soleTraderBusinessesKey}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.{FirstIncomeSourceController, routes}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.StreamlineIncomeSourceForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.{AccountingPeriodUtil, ITSASessionKeys}

class FirstIncomeSourceControllerISpec extends ComponentSpecBase {

  val testNino = "test-nino"
  val clearedSoleTraderBusinesses: SoleTraderBusinesses = soleTraderBusinesses.copy(
    businesses = soleTraderBusinesses.businesses.map(_.copy(
      trade = None,
      name = None,
      startDate = None,
      startDateBeforeLimit = None
    )),
    accountingMethod = None
  )

  val date: DateModel = DateModel.dateConvert(AccountingPeriodUtil.getStartDateLimit)

  lazy val firstIncomeSourceController: FirstIncomeSourceController = app.injector.instanceOf[FirstIncomeSourceController]

  s"GET ${routes.FirstIncomeSourceController.show(id)}" when {
    "the connector returns an error from the backend" should {
      "display the technical difficulties page" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(INTERNAL_SERVER_ERROR)
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

        When(s"GET ${routes.FirstIncomeSourceController.show(id)} is called")
        val res = getFirstIncomeSource(id, isEditMode = false, isGlobalEdit = false)

        Then("should return an INTERNAL_SERVER_ERROR")
        res must have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }
    "the Connector receives no content" should {
      "return the page with no prepopulated fields" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(NO_CONTENT)
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

        When(s"GET ${routes.FirstIncomeSourceController.show(id)} is called")
        val res = getFirstIncomeSource(id, isEditMode = false, isGlobalEdit = false)

        Then("should return an OK with the first sole trader business page")
        res must have(
          httpStatus(OK),
          pageTitle(messages("agent.full-income-source.heading") + agentTitleSuffix),
          textField(StreamlineIncomeSourceForm.businessTradeName, ""),
          textField(StreamlineIncomeSourceForm.businessName, ""),
          radioButtonSet(StreamlineIncomeSourceForm.startDateBeforeLimit, None),
          radioButtonSet(StreamlineIncomeSourceForm.accountingMethodBusiness, None)
        )
      }
    }

    "the business requested is returned back with no fields filled" should {
      "return the page with no prepopulated fields" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(clearedSoleTraderBusinesses))
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

        When(s"GET ${routes.FirstIncomeSourceController.show(id)} is called")
        val res = getFirstIncomeSource(id, isEditMode = false, isGlobalEdit = false)

        Then("should return an OK with the first sole trader business page")
        res must have(
          httpStatus(OK),
          pageTitle(messages("agent.full-income-source.heading") + agentTitleSuffix),
          textField(StreamlineIncomeSourceForm.businessTradeName, ""),
          textField(StreamlineIncomeSourceForm.businessName, ""),
          radioButtonSet(StreamlineIncomeSourceForm.startDateBeforeLimit, None),
          radioButtonSet(StreamlineIncomeSourceForm.accountingMethodBusiness, None)
        )
      }
    }

    "connector returns a previously filled in business which had a start date older than the limit" should {
      "show the current business with the fields" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(
          responseStatus = OK,
          responseBody = Json.toJson(
            soleTraderBusinesses.copy(
              businesses = Seq(
                soleTraderBusiness.copy(
                  startDateBeforeLimit = None,
                  startDate = Some(DateModel.dateConvert(AccountingPeriodUtil.getStartDateLimit.minusDays(1)))
                )
              )
            )
          )
        )
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

        When(s"GET ${routes.FirstIncomeSourceController.show(id)} is called")
        val res = getFirstIncomeSource(id, isEditMode = true, isGlobalEdit = false)

        Then("should return an OK with the first sole trader business page")
        res must have(
          httpStatus(OK),
          pageTitle(messages("agent.full-income-source.heading") + agentTitleSuffix),
          textField(StreamlineIncomeSourceForm.businessTradeName, "test trade"),
          textField(StreamlineIncomeSourceForm.businessName, "test name"),
          radioButtonSet(StreamlineIncomeSourceForm.startDateBeforeLimit, Some("Yes")),
          radioButtonSet(StreamlineIncomeSourceForm.accountingMethodBusiness, Some("Cash basis accounting"))
        )
      }
    }

    "connector returns a previously filled in business which had a start date sooner than the limit" should {
      "show the current business with the fields" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(
          responseStatus = OK,
          responseBody = Json.toJson(
            soleTraderBusinesses.copy(
              businesses = Seq(
                soleTraderBusiness.copy(
                  startDateBeforeLimit = None,
                  startDate = Some(DateModel.dateConvert(AccountingPeriodUtil.getStartDateLimit))
                )
              )
            )
          )
        )
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

        When(s"GET ${routes.FirstIncomeSourceController.show(id)} is called")
        val res = getFirstIncomeSource(id, isEditMode = true, isGlobalEdit = false)

        Then("should return an OK with the first sole trader business page")
        res must have(
          httpStatus(OK),
          pageTitle(messages("agent.full-income-source.heading") + agentTitleSuffix),
          textField(StreamlineIncomeSourceForm.businessTradeName, "test trade"),
          textField(StreamlineIncomeSourceForm.businessName, "test name"),
          radioButtonSet(StreamlineIncomeSourceForm.startDateBeforeLimit, Some("No")),
          radioButtonSet(StreamlineIncomeSourceForm.accountingMethodBusiness, Some("Cash basis accounting"))
        )
      }
    }

    "connector returns a previously filled in business which had selected their start date is before the limit" should {
      "show the current business with the fields" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(
          responseStatus = OK,
          responseBody = Json.toJson(
            soleTraderBusinesses.copy(
              businesses = Seq(
                soleTraderBusiness.copy(
                  startDateBeforeLimit = Some(true),
                  startDate = None
                )
              )
            )
          )
        )
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

        When(s"GET ${routes.FirstIncomeSourceController.show(id)} is called")
        val res = getFirstIncomeSource(id, isEditMode = true, isGlobalEdit = false)

        Then("should return an OK with the first sole trader business page")
        res must have(
          httpStatus(OK),
          pageTitle(messages("agent.full-income-source.heading") + agentTitleSuffix),
          textField(StreamlineIncomeSourceForm.businessTradeName, "test trade"),
          textField(StreamlineIncomeSourceForm.businessName, "test name"),
          radioButtonSet(StreamlineIncomeSourceForm.startDateBeforeLimit, Some("Yes")),
          radioButtonSet(StreamlineIncomeSourceForm.accountingMethodBusiness, Some("Cash basis accounting"))
        )
      }
    }

    "connector returns a previously filled in business which had selected their start date is after the limit but the stored date is before" should {
      "show the current business with the fields" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(
          responseStatus = OK,
          responseBody = Json.toJson(
            soleTraderBusinesses.copy(
              businesses = Seq(
                soleTraderBusiness.copy(
                  startDateBeforeLimit = Some(false),
                  startDate = Some(DateModel.dateConvert(AccountingPeriodUtil.getStartDateLimit.minusDays(1)))
                )
              )
            )
          )
        )
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

        When(s"GET ${routes.FirstIncomeSourceController.show(id)} is called")
        val res = getFirstIncomeSource(id, isEditMode = true, isGlobalEdit = false)

        Then("should return an OK with the first sole trader business page")
        res must have(
          httpStatus(OK),
          pageTitle(messages("agent.full-income-source.heading") + agentTitleSuffix),
          textField(StreamlineIncomeSourceForm.businessTradeName, "test trade"),
          textField(StreamlineIncomeSourceForm.businessName, "test name"),
          radioButtonSet(StreamlineIncomeSourceForm.startDateBeforeLimit, Some("Yes")),
          radioButtonSet(StreamlineIncomeSourceForm.accountingMethodBusiness, Some("Cash basis accounting"))
        )
      }
    }

    "connector returns a previously filled in business which had selected their start date is after the limit and it still is" should {
      "show the current business with the fields" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(
          responseStatus = OK,
          responseBody = Json.toJson(
            soleTraderBusinesses.copy(
              businesses = Seq(
                soleTraderBusiness.copy(
                  startDateBeforeLimit = Some(false),
                  startDate = Some(DateModel.dateConvert(AccountingPeriodUtil.getStartDateLimit))
                )
              )
            )
          )
        )
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

        When(s"GET ${routes.FirstIncomeSourceController.show(id)} is called")
        val res = getFirstIncomeSource(id, isEditMode = true, isGlobalEdit = false)

        Then("should return an OK with the first sole trader business page")
        res must have(
          httpStatus(OK),
          pageTitle(messages("agent.full-income-source.heading") + agentTitleSuffix),
          textField(StreamlineIncomeSourceForm.businessTradeName, "test trade"),
          textField(StreamlineIncomeSourceForm.businessName, "test name"),
          radioButtonSet(StreamlineIncomeSourceForm.startDateBeforeLimit, Some("No")),
          radioButtonSet(StreamlineIncomeSourceForm.accountingMethodBusiness, Some("Cash basis accounting"))
        )
      }
    }

    "connector returns a different business as the first business" should {
      "redirect the user to the next income source page" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinesses))
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

        When(s"GET ${routes.FirstIncomeSourceController.show(id)} is called")
        val res = getFirstIncomeSource(s"$id-2", isEditMode = false, isGlobalEdit = false)

        Then("should return a SEE_OTHER to the next income source page")
        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.NextIncomeSourceController.show(id).url)
        )
      }
    }
  }

  s"POST ${routes.FirstIncomeSourceController.show(id)}" when {
    "the connector returns an error when saving the streamline business" should {
      "display the technical difficulties page" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()

        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(clearedSoleTraderBusinesses))
        stubSaveSubscriptionData(
          reference = reference,
          id = soleTraderBusinessesKey,
          body = Json.toJson(soleTraderBusinesses.copy(businesses = Seq(soleTraderBusiness.copy(startDateBeforeLimit = None))))
        )(INTERNAL_SERVER_ERROR)
        stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)

        When(s"POST ${routes.FirstIncomeSourceController.submit(id)} is called")
        val res = submitFirstIncomeSource(
          trade = Some("test trade"),
          name = Some("test name"),
          startDate = Some(date),
          startDateBeforeLimit = None,
          accountingMethod = Some(Cash),
          id = id,
          isEditMode = false,
          isGlobalEdit = false
        )

        Then("should return an INTERNAL_SERVER_ERROR")
        res must have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }
    "the connector returns an error when fetching the streamline business" should {
      "display the technical difficulties page" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()

        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(INTERNAL_SERVER_ERROR, Json.toJson(clearedSoleTraderBusinesses))

        When(s"POST ${routes.FirstIncomeSourceController.submit(id)} is called")
        val res = submitFirstIncomeSource(
          trade = Some("test trade"),
          name = Some("test name"),
          startDate = Some(date),
          startDateBeforeLimit = None,
          accountingMethod = Some(Cash),
          id = id,
          isEditMode = false,
          isGlobalEdit = false
        )

        Then("should return an INTERNAL_SERVER_ERROR")
        res must have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }
    "not in edit mode" when {
      "the form data is valid they do not need to provide a start date" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()

        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(clearedSoleTraderBusinesses))
        stubSaveSubscriptionData(
          reference = reference,
          id = soleTraderBusinessesKey,
          body = Json.toJson(soleTraderBusinesses.copy(businesses = Seq(soleTraderBusiness.copy(startDate = None, startDateBeforeLimit = Some(true)))))
        )(OK)
        stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)

        When(s"POST ${routes.FirstIncomeSourceController.submit(id)} is called")
        val res = submitFirstIncomeSource(
          trade = Some("test trade"),
          name = Some("test name"),
          startDate = None,
          startDateBeforeLimit = Some(true),
          accountingMethod = Some(Cash),
          id = id,
          isEditMode = false,
          isGlobalEdit = false
        )

        Then("Should return a SEE_OTHER with a redirect location of the check address route")
        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.AddressLookupRoutingController.checkAddressLookupJourney(id).url)
        )
      }

      "the form data is valid they need to provide a start date" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()

        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(clearedSoleTraderBusinesses))
        stubSaveSubscriptionData(
          reference = reference,
          id = soleTraderBusinessesKey,
          body = Json.toJson(soleTraderBusinesses.copy(businesses = Seq(soleTraderBusiness.copy(startDate = None, startDateBeforeLimit = Some(false)))))
        )(OK)
        stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)

        When(s"POST ${routes.FirstIncomeSourceController.submit(id)} is called")
        val res = submitFirstIncomeSource(
          trade = Some("test trade"),
          name = Some("test name"),
          startDate = None,
          startDateBeforeLimit = Some(false),
          accountingMethod = Some(Cash),
          id = id,
          isEditMode = false,
          isGlobalEdit = false
        )

        Then("Should return a SEE_OTHER with a redirect location of the check address route")
        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.BusinessStartDateController.show(id).url)
        )
      }

      "the form data is invalid" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()

        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(clearedSoleTraderBusinesses))
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

        When(s"POST ${routes.FirstIncomeSourceController.submit(id)} is called")
        val res = submitFirstIncomeSource(
          trade = None,
          name = None,
          startDate = None,
          startDateBeforeLimit = None,
          accountingMethod = None,
          id = id,
          isEditMode = false,
          isGlobalEdit = false
        )

        Then("Should return a BAD_REQUEST")
        res must have(
          httpStatus(BAD_REQUEST)
        )
      }
    }

    "in edit mode" when {
      "the form data is valid and they do not need to provide a start date" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()

        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(clearedSoleTraderBusinesses))
        stubSaveSubscriptionData(
          reference = reference,
          id = soleTraderBusinessesKey,
          body = Json.toJson(soleTraderBusinesses.copy(businesses = Seq(soleTraderBusiness.copy(startDate = None, startDateBeforeLimit = Some(true)))))
        )(OK)
        stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)

        When(s"POST ${routes.FirstIncomeSourceController.submit(id)} is called")
        val res = submitFirstIncomeSource(
          trade = Some("test trade"),
          name = Some("test name"),
          startDate = None,
          startDateBeforeLimit = Some(true),
          accountingMethod = Some(Cash),
          id = id,
          isEditMode = true,
          isGlobalEdit = false
        )

        Then("Should return a SEE_OTHER with a redirect location of the check address route")
        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.SelfEmployedCYAController.show(id, isEditMode = true).url)
        )
      }

      "the form data is valid and their start date is not before the limit" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()

        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(clearedSoleTraderBusinesses.copy(businesses = Seq(soleTraderBusiness.copy(
          trade = None, name = None, startDate = None, startDateBeforeLimit = Some(true)
        )))))
        stubSaveSubscriptionData(
          reference = reference,
          id = soleTraderBusinessesKey,
          body = Json.toJson(soleTraderBusinesses.copy(businesses = Seq(soleTraderBusiness.copy(startDate = None, startDateBeforeLimit = Some(false)))))
        )(OK)
        stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)

        When(s"POST ${routes.FirstIncomeSourceController.submit(id)} is called")
        val res = submitFirstIncomeSource(
          trade = Some("test trade"),
          name = Some("test name"),
          startDate = Some(DateModel.dateConvert(AccountingPeriodUtil.getStartDateLimit)),
          startDateBeforeLimit = Some(false),
          accountingMethod = Some(Cash),
          id = id,
          isEditMode = true,
          isGlobalEdit = false
        )

        Then("Should return a SEE_OTHER with a redirect location of the check address route")
        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.BusinessStartDateController.show(id, isEditMode = true).url)
        )
      }

      "the form data is invalid" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()

        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(clearedSoleTraderBusinesses))
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

        When(s"POST ${routes.FirstIncomeSourceController.submit(id)} is called")
        val res = submitFirstIncomeSource(
          trade = None,
          name = None,
          startDate = None,
          startDateBeforeLimit = None,
          accountingMethod = None,
          id = id,
          isEditMode = true,
          isGlobalEdit = false
        )

        Then("Should return a BAD_REQUEST")
        res must have(
          httpStatus(BAD_REQUEST)
        )
      }
    }
    "in global edit mode" when {
      "the form data is valid they do not need to provide a start date" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()

        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(clearedSoleTraderBusinesses))
        stubSaveSubscriptionData(
          reference = reference,
          id = soleTraderBusinessesKey,
          body = Json.toJson(soleTraderBusinesses.copy(businesses = Seq(soleTraderBusiness.copy(startDate = None, startDateBeforeLimit = Some(true)))))
        )(OK)
        stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)

        When(s"POST ${routes.FirstIncomeSourceController.submit(id)} is called")
        val res = submitFirstIncomeSource(
          trade = Some("test trade"),
          name = Some("test name"),
          startDate = None,
          startDateBeforeLimit = Some(true),
          accountingMethod = Some(Cash),
          id = id,
          isEditMode = true,
          isGlobalEdit = true
        )

        Then("Should return a SEE_OTHER with a redirect location of the check address route")
        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.SelfEmployedCYAController.show(id, isEditMode = true, isGlobalEdit = true).url)
        )
      }

      "the form data is valid they need to provide a start date" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()

        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(clearedSoleTraderBusinesses))
        stubSaveSubscriptionData(
          reference = reference,
          id = soleTraderBusinessesKey,
          body = Json.toJson(soleTraderBusinesses.copy(businesses = Seq(soleTraderBusiness.copy(startDate = None, startDateBeforeLimit = Some(false)))))
        )(OK)
        stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)

        When(s"POST ${routes.FirstIncomeSourceController.submit(id)} is called")
        val res = submitFirstIncomeSource(
          trade = Some("test trade"),
          name = Some("test name"),
          startDate = None,
          startDateBeforeLimit = Some(false),
          accountingMethod = Some(Cash),
          id = id,
          isEditMode = true,
          isGlobalEdit = true
        )

        Then("Should return a SEE_OTHER with a redirect location of the check address route")
        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.BusinessStartDateController.show(id, isEditMode = true, isGlobalEdit = true).url)
        )
      }

      "the form data is invalid" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()

        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(clearedSoleTraderBusinesses))
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

        When(s"POST ${routes.FirstIncomeSourceController.submit(id)} is called")
        val res = submitFirstIncomeSource(
          trade = None,
          name = None,
          startDate = None,
          startDateBeforeLimit = None,
          accountingMethod = None,
          id = id,
          isEditMode = true,
          isGlobalEdit = true
        )

        Then("Should return a BAD_REQUEST")
        res must have(
          httpStatus(BAD_REQUEST)
        )
      }
    }
  }

  "backUrl" must {
    def backUrl(isEditMode: Boolean, isGlobalEdit: Boolean): String = firstIncomeSourceController.backUrl(id, isEditMode, isGlobalEdit)

    "redirect to Self Employment CYA" when {
      "in edit mode" in {
        backUrl(isEditMode = true, isGlobalEdit = false) mustBe routes.SelfEmployedCYAController.show(id, isEditMode = true).url
      }
      "in global edit mode" in {
        backUrl(isEditMode = true, isGlobalEdit = true) mustBe routes.SelfEmployedCYAController.show(id, isEditMode = true, isGlobalEdit = true).url
      }
    }

    "redirect to Your Income Sources" when {
      "not in edit mode or global edit mode" in {
        backUrl(isEditMode = false, isGlobalEdit = false) must include(clientYourIncomeSources)
      }
    }
  }
}

