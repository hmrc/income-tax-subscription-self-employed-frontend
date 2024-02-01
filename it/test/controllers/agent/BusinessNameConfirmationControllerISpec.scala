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

import connectors.stubs.IncomeTaxSubscriptionConnectorStub.{stubGetSubscriptionData, stubSaveSubscriptionData}
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.AuthStub.stubAuthSuccess
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.soleTraderBusinessesKey
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.routes
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{No, SoleTraderBusinesses, Yes}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ITSASessionKeys

class BusinessNameConfirmationControllerISpec extends ComponentSpecBase with FeatureSwitching {

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  val soleTraderBusinessesWithoutName: SoleTraderBusinesses = soleTraderBusinesses.copy(
    businesses = soleTraderBusinesses.businesses.map(_.copy(name = None))
  )

  s"GET ${routes.BusinessNameConfirmationController.show(id).url}" should {
    "return the page" when {
      "there is an existing business name" in {
        Given("I setup the wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinesses))

        When(s"GET ${routes.BusinessNameConfirmationController.show(id).url} is called")
        val res = getClientBusinessNameConfirmation(id)(Map(
          ITSASessionKeys.FirstName -> "FirstName",
          ITSASessionKeys.LastName -> "LastName",
          ITSASessionKeys.NINO -> "NINO"
        ))

        res must have(
          httpStatus(OK),
          pageTitle("Is your client’s business trading name the same as the first one you added?" + agentTitleSuffix)
        )
      }
      "there is no existing business name, taking the name from session" in {
        Given("I setup the wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinessesWithoutName))

        When(s"GET ${routes.BusinessNameConfirmationController.show(id).url} is called")
        val res = getClientBusinessNameConfirmation(id)(Map(
          ITSASessionKeys.FirstName -> "FirstName",
          ITSASessionKeys.LastName -> "LastName",
          ITSASessionKeys.NINO -> "NINO"
        ))

        res must have(
          httpStatus(OK),
          pageTitle("Is your client’s business name the same as their own name?" + agentTitleSuffix)
        )
      }
    }
  }

  s"POST ${routes.BusinessNameConfirmationController.submit(id).url}" when {
    "there is a name in the users session" when {
      "the user submits no answer" must {
        "return BAD_REQUEST with the page" in {
          Given("I setup the wiremock stubs")
          stubAuthSuccess()
          stubGetSubscriptionData(reference, soleTraderBusinessesKey)(NO_CONTENT)

          When(s"POST ${routes.BusinessNameConfirmationController.show(id).url} is called")
          val res = submitClientBusinessNameConfirmation(id, None)(Map(
            ITSASessionKeys.FirstName -> "FirstName",
            ITSASessionKeys.LastName -> "LastName",
            ITSASessionKeys.NINO -> "NINO"
          ))

          res must have(
            httpStatus(BAD_REQUEST),
            pageTitle("Error: Is your client’s business name the same as their own name?" + agentTitleSuffix)
          )
        }
      }
      "the user submits Yes" must {
        "save the name and redirect to the business start date page" in {
          Given("I setup the wiremock stubs")
          stubAuthSuccess()
          stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinessesWithoutName))
          stubSaveSubscriptionData(reference, soleTraderBusinessesKey, Json.toJson(soleTraderBusinesses))(OK)

          When(s"POST ${routes.BusinessNameConfirmationController.show(id).url} is called")
          val res = submitClientBusinessNameConfirmation(id, Some(Yes))(Map(
            ITSASessionKeys.FirstName -> "test",
            ITSASessionKeys.LastName -> "name",
            ITSASessionKeys.NINO -> "NINO"
          ))

          res must have(
            httpStatus(SEE_OTHER),
            redirectURI(DateOfCommencementUri)
          )
        }
      }
      "the user submits No" must {
        "redirect to the business name page" in {
          Given("I setup the wiremock stubs")
          stubAuthSuccess()
          stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinesses))

          When(s"POST ${routes.BusinessNameConfirmationController.show(id).url} is called")
          val res = submitClientBusinessNameConfirmation(id, Some(No))(Map(
            ITSASessionKeys.FirstName -> "FirstName",
            ITSASessionKeys.LastName -> "LastName",
            ITSASessionKeys.NINO -> "NINO"
          ))

          res must have(
            httpStatus(SEE_OTHER),
            redirectURI(ClientBusinessNameUri)
          )
        }
      }
    }
  }
}