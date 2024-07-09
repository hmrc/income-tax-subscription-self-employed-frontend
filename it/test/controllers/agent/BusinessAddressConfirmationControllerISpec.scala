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

import connectors.stubs.IncomeTaxSubscriptionConnectorStub.{stubDeleteSubscriptionData, stubGetSubscriptionData, stubSaveSubscriptionData}
import connectors.stubs.SessionDataConnectorStub.stubGetSessionData
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.AuthStub.stubAuthSuccess
import play.api.http.Status._
import play.api.libs.json.{JsString, Json}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.{incomeSourcesComplete, soleTraderBusinessesKey}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.routes
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{Address, No, SoleTraderBusiness, Yes}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ITSASessionKeys

class BusinessAddressConfirmationControllerISpec extends ComponentSpecBase with FeatureSwitching {

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val address: Address = Address(
    Seq(
      "1 Long Road",
      "Lonely Town"
    ),
    Some("ZZ1 1ZZ")
  )
  val testNino: String = "test-nino"

  s"GET ${routes.BusinessAddressConfirmationController.show(id).url}" should {
    "return the page" when {
      "there is an existing business address" in {
        Given("I setup the wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinesses))
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

        When(s"GET ${routes.BusinessAddressConfirmationController.show(id).url} is called")
        val res = getClientBusinessAddressConfirmation(id)(Map(
          ITSASessionKeys.FirstName -> "FirstName",
          ITSASessionKeys.LastName -> "LastName",
          ITSASessionKeys.NINO -> "NINO"
        ))

        res must have(
          httpStatus(OK),
          pageTitle("Confirm business address" + agentTitleSuffix)
        )
      }
      "there is no existing business address" in {
        Given("I setup the wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(NO_CONTENT)

        When(s"GET ${routes.BusinessAddressConfirmationController.show(id).url} is called")
        val res = getClientBusinessAddressConfirmation(id)(Map(
          ITSASessionKeys.FirstName -> "FirstName",
          ITSASessionKeys.LastName -> "LastName",
          ITSASessionKeys.NINO -> "NINO"
        ))

        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(ClientBusinessAddressInitialiseUri)
        )
      }
    }
  }

  s"POST ${routes.BusinessAddressConfirmationController.submit(id).url}" when {
    "there is a address in the users session" when {
      "the user submits no answer" must {
        "return BAD_REQUEST with the page" in {
          Given("I setup the wiremock stubs")
          stubAuthSuccess()
          stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinesses))
          stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))

          When(s"POST ${routes.BusinessAddressConfirmationController.show(id).url} is called")
          val res = submitClientBusinessAddressConfirmation(id, None)(Map(
            ITSASessionKeys.FirstName -> "FirstName",
            ITSASessionKeys.LastName -> "LastName",
            ITSASessionKeys.NINO -> "NINO"
          ))

          res must have(
            httpStatus(BAD_REQUEST),
            pageTitle("Error: Confirm business address" + agentTitleSuffix)
          )
        }
      }
      "the user submits Yes" must {
        "save the address and redirect to the Check your answers page" in {
          val expectedSave = soleTraderBusinesses.copy(
            businesses = soleTraderBusinesses.businesses :+ SoleTraderBusiness("id2", address = Some(address))
          )

          Given("I setup the wiremock stubs")
          stubAuthSuccess()
          stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinesses))
          stubSaveSubscriptionData(reference, soleTraderBusinessesKey, Json.toJson(expectedSave))(OK)
          stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)

          When(s"POST ${routes.BusinessAddressConfirmationController.show("id2").url} is called")
          val res = submitClientBusinessAddressConfirmation("id2", Some(Yes))(Map(
            ITSASessionKeys.FirstName -> "FirstName",
            ITSASessionKeys.LastName -> "LastName",
            ITSASessionKeys.NINO -> "NINO"
          ))

          res must have(
            httpStatus(SEE_OTHER),
            redirectURI(ClientBusinessCYAUri)
          )
        }
      }
      "the user submits No" must {
        "redirect to the business address look up page" in {
          Given("I setup the wiremock stubs")
          stubAuthSuccess()
          stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinesses))

          When(s"POST ${routes.BusinessAddressConfirmationController.show(id).url} is called")
          val res = submitClientBusinessAddressConfirmation(id, Some(No))(Map(
            ITSASessionKeys.FirstName -> "FirstName",
            ITSASessionKeys.LastName -> "LastName",
            ITSASessionKeys.NINO -> "NINO"
          ))

          res must have(
            httpStatus(SEE_OTHER),
            redirectURI(ClientBusinessAddressInitialiseUri)
          )
        }
      }
    }
  }
}