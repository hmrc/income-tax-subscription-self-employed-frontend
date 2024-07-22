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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.routes
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.NextIncomeSourceForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ITSASessionKeys

class NextIncomeSourceControllerISpec extends ComponentSpecBase {

  val testNino = "test-nino"

  val fullSoleTraderBusinesses: SoleTraderBusinesses = soleTraderBusinesses.copy(
    businesses = soleTraderBusinesses.businesses ++ soleTraderBusinesses.businesses.map(_.copy(id = s"$id-2"))
  )

  val clearedSoleTraderBusinesses: SoleTraderBusinesses = soleTraderBusinesses.copy(
    businesses = soleTraderBusinesses.businesses ++ soleTraderBusinesses.businesses.map(_.copy(
      id = s"$id-2",
      trade = None,
      name = None,
      startDate = None
    ))
  )

  val clearedSingleBusiness: SoleTraderBusinesses = soleTraderBusinesses.copy(
    businesses = soleTraderBusinesses.businesses.map(_.copy(
      trade = None,
      name = None,
      startDate = None
    ))
  )

  s"GET ${routes.NextIncomeSourceController.show(s"$id-2")}" when {
    "the connector returns an error from the backend" should {
      "display the technical difficulties page" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(INTERNAL_SERVER_ERROR)
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

        When(s"GET ${routes.NextIncomeSourceController.show(s"$id-2")} is called")
        val res = getNextIncomeSource(s"$id-2", isEditMode = false)

        Then("should return an INTERNAL_SERVER_ERROR")
        res must have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }
    "the Connector receives no content for that business" should {
      "return the page with no prepopulated fields" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(clearedSoleTraderBusinesses.copy(
          businesses = clearedSoleTraderBusinesses.businesses.headOption.toSeq
        )))
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

        When(s"GET ${routes.NextIncomeSourceController.show(s"$id-2")} is called")
        val res = getNextIncomeSource(s"$id-2", isEditMode = false)

        Then("should return an OK with the next sole trader business page")
        res must have(
          httpStatus(OK),
          pageTitle(messages("agent.full-income-source.heading") + agentTitleSuffix),
          textField(NextIncomeSourceForm.businessTradeName, ""),
          textField(NextIncomeSourceForm.businessName, ""),
          dateField(NextIncomeSourceForm.startDate, DateModel("", "", ""))
        )
      }
    }

    "the business requested is returned back with no fields filled" should {
      "return the page with no prepopulated fields" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(clearedSoleTraderBusinesses))
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

        When(s"GET ${routes.NextIncomeSourceController.show(s"$id-2")} is called")
        val res = getNextIncomeSource(s"$id-2", isEditMode = false)

        Then("should return an OK with the next sole trader business page")
        res must have(
          httpStatus(OK),
          pageTitle(messages("agent.full-income-source.heading") + agentTitleSuffix),
          textField(NextIncomeSourceForm.businessTradeName, ""),
          textField(NextIncomeSourceForm.businessName, ""),
          dateField(NextIncomeSourceForm.startDate, DateModel("", "", ""))
        )
      }
    }

    "connector returns a previously filled in business" should {
      "show the current business with the fields" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(fullSoleTraderBusinesses))
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

        When(s"GET ${routes.NextIncomeSourceController.show(s"$id-2")} is called")
        val res = getNextIncomeSource(s"$id-2", isEditMode = true)

        Then("should return an OK with the next sole trader business page")
        res must have(
          httpStatus(OK),
          pageTitle(messages("agent.full-income-source.heading") + agentTitleSuffix),
          textField(NextIncomeSourceForm.businessTradeName, "test trade"),
          textField(NextIncomeSourceForm.businessName, "test name"),
          dateField(NextIncomeSourceForm.startDate, DateModel("1", "1", "1980"))
        )
      }
    }

    "connector returns a this business as the first business" should {
      "redirect the user to the next income source page" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinesses))
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

        When(s"GET ${routes.NextIncomeSourceController.show(id)} is called")
        val res = getNextIncomeSource(id, isEditMode = false)

        Then("should return a SEE_OTHER to the first income source page")
        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.FirstIncomeSourceController.show(id).url)
        )
      }
    }
  }

  s"POST ${routes.NextIncomeSourceController.show(id)}" when {
    "the connector returns an error when saving the streamline business" should {
      "display the technical difficulties page" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()

        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(clearedSingleBusiness))
        stubSaveSubscriptionData(reference, soleTraderBusinessesKey, Json.toJson(soleTraderBusinesses))(INTERNAL_SERVER_ERROR)
        stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)

        When(s"POST ${routes.NextIncomeSourceController.show(id)} is called")
        val res = submitNextIncomeSource(
          trade = Some("test trade"),
          name = Some("test name"),
          startDate = Some(DateModel("1", "1", "1980")),
          id = id,
          isEditMode = false
        )

        Then("should return an INTERNAL_SERVER_ERROR")
        res must have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }
    "not in edit mode" when {
      "the form data is valid and connector stores it successfully" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()

        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(clearedSingleBusiness))
        stubSaveSubscriptionData(reference, soleTraderBusinessesKey, Json.toJson(soleTraderBusinesses))(OK)
        stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)

        When(s"POST ${routes.NextIncomeSourceController.show(id)} is called")
        val res = submitNextIncomeSource(
          trade = Some("test trade"),
          name = Some("test name"),
          startDate = Some(DateModel("1", "1", "1980")),
          id = id,
          isEditMode = false
        )

        Then("Should return a SEE_OTHER with a redirect location of the check address route")
        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.AddressLookupRoutingController.checkAddressLookupJourney(id).url)
        )
      }

      "the form data is invalid" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()

        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(clearedSingleBusiness))
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

        When(s"POST ${routes.NextIncomeSourceController.show(id)} is called")
        val res = submitNextIncomeSource(
          trade = None,
          name = None,
          startDate = None,
          id = id,
          isEditMode = false
        )

        Then("Should return a BAD_REQUEST")
        res must have(
          httpStatus(BAD_REQUEST)
        )
      }
    }
    "in edit mode" when {
      "the form data is valid and connector stores it successfully" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()

        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(clearedSingleBusiness))
        stubSaveSubscriptionData(reference, soleTraderBusinessesKey, Json.toJson(soleTraderBusinesses))(OK)
        stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)

        When(s"POST ${routes.NextIncomeSourceController.show(id)} is called")
        val res = submitNextIncomeSource(
          trade = Some("test trade"),
          name = Some("test name"),
          startDate = Some(DateModel("1", "1", "1980")),
          id = id,
          isEditMode = true
        )

        Then("Should return a SEE_OTHER with a redirect location of the check address route")
        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.SelfEmployedCYAController.show(id, isEditMode = true).url)
        )
      }

      "the form data is invalid" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()

        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(clearedSingleBusiness))
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

        When(s"POST ${routes.NextIncomeSourceController.show(id)} is called")
        val res = submitNextIncomeSource(
          trade = None,
          name = None,
          startDate = None,
          id = id,
          isEditMode = true
        )

        Then("Should return a BAD_REQUEST")
        res must have(
          httpStatus(BAD_REQUEST)
        )
      }
    }
  }
}
