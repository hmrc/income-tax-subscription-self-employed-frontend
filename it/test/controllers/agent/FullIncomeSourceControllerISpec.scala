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
import connectors.stubs.SessionDataConnectorStub.{stubGetSessionData, stubSaveSessionData}
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.AuthStub
import play.api.http.Status._
import play.api.libs.json.{JsString, Json}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.{incomeSourcesComplete, soleTraderBusinessesKey}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.{FullIncomeSourceController, routes}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.StreamlineIncomeSourceForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{DuplicateDetails, SoleTraderBusiness, SoleTraderBusinesses}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ITSASessionKeys

class FullIncomeSourceControllerISpec extends ComponentSpecBase {

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  lazy val fullIncomeSourceController: FullIncomeSourceController = app.injector.instanceOf[FullIncomeSourceController]

  val duplicateDetails: DuplicateDetails = DuplicateDetails(
    id = id,
    name = "test duplicate name",
    trade = "test duplicate trade",
    startDateBeforeLimit = true
  )

  s"GET ${routes.FullIncomeSourceController.show(id)}" when {
    "the user is unauthorised" should {
      "redirect to the login page" in {
        AuthStub.stubUnauthorised()

        val result = getClientFullIncomeSource(id, isEditMode = false, isGlobalEdit = false)

        result must have(
          httpStatus(SEE_OTHER),
          redirectURI(ggSignInURI)
        )
      }
    }
    "the user has previously entered valid details" should {
      "display the page with the fields filled" in {
        AuthStub.stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(
          responseStatus = OK,
          responseBody = Json.toJson(soleTraderBusinesses)
        )
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

        val result = getClientFullIncomeSource(id, isEditMode = false, isGlobalEdit = false)

        result must have(
          httpStatus(OK),
          pageTitle(messages("agent.full-income-source.heading") + agentTitleSuffix),
          textField(StreamlineIncomeSourceForm.businessTradeName, "test trade"),
          textField(StreamlineIncomeSourceForm.businessName, "test name"),
          radioButtonSet(StreamlineIncomeSourceForm.startDateBeforeLimit, Some("No"))
        )
      }
    }
    "the user has previously entered duplicate details" should {
      "display the page with the fields filled" when {
        "the duplicate details were for the same business they are accessing" in {
          AuthStub.stubAuthSuccess()
          stubGetSubscriptionData(reference, soleTraderBusinessesKey)(NO_CONTENT)
          stubGetSessionData(ITSASessionKeys.DUPLICATE_DETAILS)(
            responseStatus = OK,
            responseBody = Json.toJson(duplicateDetails)(DuplicateDetails.encryptedFormat)
          )
          stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

          val result = getClientFullIncomeSource(id, isEditMode = false, isGlobalEdit = false)

          result must have(
            httpStatus(OK),
            pageTitle(messages("agent.full-income-source.heading") + agentTitleSuffix),
            textField(StreamlineIncomeSourceForm.businessTradeName, "test duplicate trade"),
            textField(StreamlineIncomeSourceForm.businessName, "test duplicate name"),
            radioButtonSet(StreamlineIncomeSourceForm.startDateBeforeLimit, Some("Yes"))
          )
        }
      }
      "display the page with the fields empty" when {
        "the duplicate details were for a different business" in {
          AuthStub.stubAuthSuccess()
          stubGetSubscriptionData(reference, soleTraderBusinessesKey)(NO_CONTENT)
          stubGetSessionData(ITSASessionKeys.DUPLICATE_DETAILS)(
            responseStatus = OK,
            responseBody = Json.toJson(duplicateDetails.copy(id = "id-two"))(DuplicateDetails.encryptedFormat)
          )
          stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

          val result = getClientFullIncomeSource(id, isEditMode = false, isGlobalEdit = false)

          result must have(
            httpStatus(OK),
            pageTitle(messages("agent.full-income-source.heading") + agentTitleSuffix),
            textField(StreamlineIncomeSourceForm.businessTradeName, ""),
            textField(StreamlineIncomeSourceForm.businessName, ""),
            radioButtonSet(StreamlineIncomeSourceForm.startDateBeforeLimit, None)
          )
        }
      }
    }
    "the user has not been on the page before for this business" should {
      "display the page with the fields empty" in {
        AuthStub.stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(NO_CONTENT)
        stubGetSessionData(ITSASessionKeys.DUPLICATE_DETAILS)(NO_CONTENT)
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

        val result = getClientFullIncomeSource(id, isEditMode = false, isGlobalEdit = false)

        result must have(
          httpStatus(OK),
          pageTitle(messages("agent.full-income-source.heading") + agentTitleSuffix),
          textField(StreamlineIncomeSourceForm.businessTradeName, ""),
          textField(StreamlineIncomeSourceForm.businessName, ""),
          radioButtonSet(StreamlineIncomeSourceForm.startDateBeforeLimit, None)
        )
      }
    }
  }

  s"POST ${routes.FullIncomeSourceController.submit(id)}" when {
    "the user is unauthorised" should {
      "redirect to the login page" in {
        AuthStub.stubUnauthorised()

        val result = submitClientFullIncomeSource(
          trade = Some("test trade"),
          name = Some("test name"),
          startDateBeforeLimit = Some(true),
          id = id,
          isEditMode = false,
          isGlobalEdit = false
        )

        result must have(
          httpStatus(SEE_OTHER),
          redirectURI(ggSignInURI)
        )
      }
    }
    "the user submits invalid data in one of the fields" should {
      "return a bad request with the page content" in {
        AuthStub.stubAuthSuccess()
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

        val result = submitClientFullIncomeSource(
          trade = None,
          name = None,
          startDateBeforeLimit = None,
          id = id,
          isEditMode = false,
          isGlobalEdit = false
        )

        result must have(
          httpStatus(BAD_REQUEST),
          pageTitle("Error: " + messages("agent.full-income-source.heading") + agentTitleSuffix)
        )
      }
    }
    "the user submits valid data with a start date before limit set to false" should {
      "save the result as a sole trader business and continue to the business start date page" in {
        AuthStub.stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(NO_CONTENT)
        stubSaveSubscriptionData(
          reference = reference,
          id = soleTraderBusinessesKey,
          body = Json.toJson(SoleTraderBusinesses(Seq(SoleTraderBusiness(
            id = id,
            startDateBeforeLimit = Some(false),
            name = Some("test name"),
            trade = Some("test trade")
          ))))
        )(OK)
        stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

        val result = submitClientFullIncomeSource(
          trade = Some("test trade"),
          name = Some("test name"),
          startDateBeforeLimit = Some(false),
          id = id,
          isEditMode = false,
          isGlobalEdit = false
        )

        result must have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.BusinessStartDateController.show(id).url)
        )
      }
    }
    "the user submits valid data with a start date before limit set to true" when {
      "not in edit mode" should {
        "save the result as a sole trader business and continue to the address lookup router" in {
          AuthStub.stubAuthSuccess()
          stubGetSubscriptionData(reference, soleTraderBusinessesKey)(NO_CONTENT)
          stubSaveSubscriptionData(
            reference = reference,
            id = soleTraderBusinessesKey,
            body = Json.toJson(SoleTraderBusinesses(Seq(SoleTraderBusiness(
              id = id,
              startDateBeforeLimit = Some(true),
              name = Some("test name"),
              trade = Some("test trade")
            ))))
          )(OK)
          stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)
          stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

          val result = submitClientFullIncomeSource(
            trade = Some("test trade"),
            name = Some("test name"),
            startDateBeforeLimit = Some(true),
            id = id,
            isEditMode = false,
            isGlobalEdit = false
          )

          result must have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.AddressLookupRoutingController.checkAddressLookupJourney(id).url)
          )
        }
      }
      "the user submits data which is partially the same as another business they have entered" should {
        "redirect as normal" when {
          "the name is the same but the trade is not" in {
            AuthStub.stubAuthSuccess()
            stubGetSubscriptionData(reference, soleTraderBusinessesKey)(
              responseStatus = OK,
              responseBody = Json.toJson(SoleTraderBusinesses(Seq(SoleTraderBusiness(
                id = id,
                startDateBeforeLimit = Some(true),
                name = Some("test duplicate name"),
                trade = Some("test other trade")
              ))))
            )
            stubSaveSubscriptionData(
              reference = reference,
              id = soleTraderBusinessesKey,
              body = Json.toJson(SoleTraderBusinesses(Seq(SoleTraderBusiness(
                id = id,
                startDateBeforeLimit = Some(true),
                name = Some("test duplicate name"),
                trade = Some("test new trade")
              ))))
            )(OK)
            stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)
            stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

            val result = submitClientFullIncomeSource(
              trade = Some("test new trade"),
              name = Some("test duplicate name"),
              startDateBeforeLimit = Some(true),
              id = id,
              isEditMode = false,
              isGlobalEdit = false
            )

            result must have(
              httpStatus(SEE_OTHER),
              redirectURI(routes.AddressLookupRoutingController.checkAddressLookupJourney(id).url)
            )
          }
          "the trade is the same but the name is not" in {
            AuthStub.stubAuthSuccess()
            stubGetSubscriptionData(reference, soleTraderBusinessesKey)(
              responseStatus = OK,
              responseBody = Json.toJson(SoleTraderBusinesses(Seq(SoleTraderBusiness(
                id = id,
                startDateBeforeLimit = Some(true),
                name = Some("test other name"),
                trade = Some("test duplicate trade")
              ))))
            )
            stubSaveSubscriptionData(
              reference = reference,
              id = soleTraderBusinessesKey,
              body = Json.toJson(SoleTraderBusinesses(Seq(SoleTraderBusiness(
                id = id,
                startDateBeforeLimit = Some(true),
                name = Some("test new name"),
                trade = Some("test duplicate trade")
              ))))
            )(OK)
            stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)
            stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

            val result = submitClientFullIncomeSource(
              trade = Some("test duplicate trade"),
              name = Some("test new name"),
              startDateBeforeLimit = Some(true),
              id = id,
              isEditMode = false,
              isGlobalEdit = false
            )

            result must have(
              httpStatus(SEE_OTHER),
              redirectURI(routes.AddressLookupRoutingController.checkAddressLookupJourney(id).url)
            )
          }
        }
      }
      "in edit mode" should {
        "save the result as a sole trader business and continue to the check your answers" in {
          AuthStub.stubAuthSuccess()
          stubGetSubscriptionData(reference, soleTraderBusinessesKey)(NO_CONTENT)
          stubSaveSubscriptionData(
            reference = reference,
            id = soleTraderBusinessesKey,
            body = Json.toJson(SoleTraderBusinesses(Seq(SoleTraderBusiness(
              id = id,
              startDateBeforeLimit = Some(true),
              name = Some("test name"),
              trade = Some("test trade")
            ))))
          )(OK)
          stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)
          stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

          val result = submitClientFullIncomeSource(
            trade = Some("test trade"),
            name = Some("test name"),
            startDateBeforeLimit = Some(true),
            id = id,
            isEditMode = true,
            isGlobalEdit = false
          )

          result must have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.SelfEmployedCYAController.show(id, isEditMode = true).url)
          )
        }
      }
      "in global edit mode" should {
        "save the result as a sole trader business and continue to the check your answers" in {
          AuthStub.stubAuthSuccess()
          stubGetSubscriptionData(reference, soleTraderBusinessesKey)(NO_CONTENT)
          stubSaveSubscriptionData(
            reference = reference,
            id = soleTraderBusinessesKey,
            body = Json.toJson(SoleTraderBusinesses(Seq(SoleTraderBusiness(
              id = id,
              startDateBeforeLimit = Some(true),
              name = Some("test name"),
              trade = Some("test trade")
            ))))
          )(OK)
          stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)
          stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

          val result = submitClientFullIncomeSource(
            trade = Some("test trade"),
            name = Some("test name"),
            startDateBeforeLimit = Some(true),
            id = id,
            isEditMode = false,
            isGlobalEdit = true
          )

          result must have(
            httpStatus(SEE_OTHER),
            redirectURI(routes.SelfEmployedCYAController.show(id, isGlobalEdit = true).url)
          )
        }
      }
    }
    "the user submits duplicate business details" should {
      "save the details to session and continue to the duplicate details page" in {
        AuthStub.stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(
          responseStatus = OK,
          responseBody = Json.toJson(SoleTraderBusinesses(Seq(SoleTraderBusiness(
            id = id,
            startDateBeforeLimit = Some(false),
            name = Some("test duplicate name"),
            trade = Some("test duplicate trade")
          ))))
        )
        stubSaveSessionData(ITSASessionKeys.DUPLICATE_DETAILS, duplicateDetails)(OK)(DuplicateDetails.encryptedFormat)
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

        val result = submitClientFullIncomeSource(
          trade = Some("test duplicate trade"),
          name = Some("test duplicate name"),
          startDateBeforeLimit = Some(true),
          id = id,
          isEditMode = false,
          isGlobalEdit = false
        )

        result must have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.DuplicateDetailsController.show(id).url)
        )
      }
    }
  }

  "backUrl" when {
    def backUrl(isEditMode: Boolean, isGlobalEdit: Boolean): String =
      fullIncomeSourceController.backUrl(id, isEditMode, isGlobalEdit)

    "not in edit mode" should {
      "redirect to your income sources page when it is not the first business" in {
        backUrl(isEditMode = false, isGlobalEdit = false) mustBe appConfig.clientYourIncomeSourcesUrl
      }
    }

    "in edit mode" should {
      "redirect to sole trader CYA" in {
        backUrl(isEditMode = true, isGlobalEdit = false) mustBe routes.SelfEmployedCYAController.show(id, isEditMode = true).url
      }
    }

    "in global edit mode" should {
      "redirect to sole trader CYA" in {
        backUrl(isEditMode = true, isGlobalEdit = true) mustBe routes.SelfEmployedCYAController.show(id, isEditMode = true, isGlobalEdit = true).url
      }
    }
  }
}

