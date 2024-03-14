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

package controllers.individual

import connectors.stubs.IncomeTaxSubscriptionConnectorStub.{stubDeleteSubscriptionData, stubGetSubscriptionData, stubSaveSubscriptionData}
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.AuthStub.stubAuthSuccess
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.{incomeSourcesComplete, soleTraderBusinessesKey}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.individual.routes
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
        val res = getBusinessNameConfirmation(id)(Map(ITSASessionKeys.FullNameSessionKey -> "user name"))

        res must have(
          httpStatus(OK),
          pageTitle("Is your business trading name the same as the first one you added?" + titleSuffix)
        )
      }

      "there is no existing business name, taking the name from session" in {
        Given("I setup the wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(NO_CONTENT)

        When(s"GET ${routes.BusinessNameConfirmationController.show(id).url} is called")
        val res = getBusinessNameConfirmation(id)(Map(
          ITSASessionKeys.FullNameSessionKey -> "user name"
        ))
        res must have(
          httpStatus(OK),
          pageTitle("Is your business trading name the same as your own name?" + titleSuffix)
        )
      }

      "No business name and no session name" must {
        "return the page" in {
          Given("I setup the wiremock stubs")
          stubAuthSuccess()
          stubGetSubscriptionData(reference, soleTraderBusinessesKey)(NO_CONTENT)

          When(s"GET ${routes.BusinessNameConfirmationController.show(id).url} is called")
          val res = submitBusinessNameConfirmation(id, None)()

          res must have(
            httpStatus(SEE_OTHER),
            redirectURI(BusinessNameUri)
          )
        }
      }
    }

  }

  s"POST ${routes.BusinessNameConfirmationController.submit(id).url}" when {
    "there is no previous business name and no name in session" must {
      "redirect to the business name page" in {
        Given("I setup the wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(NO_CONTENT)

        When(s"POST ${routes.BusinessNameConfirmationController.show(id).url} is called")
        val res = submitBusinessNameConfirmation(id, None)()

        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(BusinessNameUri)
        )
      }
    }

    "there is a name in the users session" when {
      "the user submits no answer" must {
        "return BAD_REQUEST with the page" in {
          Given("I setup the wiremock stubs")
          stubAuthSuccess()
          stubGetSubscriptionData(reference, soleTraderBusinessesKey)(NO_CONTENT)

          When(s"POST ${routes.BusinessNameConfirmationController.show(id).url} is called")
          val res = submitBusinessNameConfirmation(id, None)(Map(ITSASessionKeys.FullNameSessionKey -> "test name"))

          res must have(
            httpStatus(BAD_REQUEST),
            pageTitle("Error: Is your business trading name the same as your own name?" + titleSuffix)
          )
        }
      }
      "the user submits Yes" must {
        "save the name and redirect to the business start date page" in {
          Given("I setup the wiremock stubs")
          stubAuthSuccess()
          stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinessesWithoutName))
          stubSaveSubscriptionData(reference, soleTraderBusinessesKey, Json.toJson(soleTraderBusinesses))(OK)
          stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)

          When(s"POST ${routes.BusinessNameConfirmationController.show(id).url} is called")
          val res = submitBusinessNameConfirmation(id, Some(Yes))(Map(ITSASessionKeys.FullNameSessionKey -> "test name"))

          res must have(
            httpStatus(SEE_OTHER),
            redirectURI(BusinessStartDateUri)
          )
        }
        "save the business name and redirect to the business start date page" in {
          Given("I setup the wiremock stubs")
          stubAuthSuccess()
          stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinesses))
          stubSaveSubscriptionData(reference, soleTraderBusinessesKey, Json.toJson(soleTraderBusinesses))(OK)
          stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)

          When(s"POST ${routes.BusinessNameConfirmationController.show(id).url} is called")
          val res = submitBusinessNameConfirmation(id, Some(Yes))(Map(ITSASessionKeys.FullNameSessionKey -> "test name"))

          res must have(
            httpStatus(SEE_OTHER),
            redirectURI(BusinessStartDateUri)
          )
        }
      }
      "the user submits No" must {
        "redirect to the business name page" in {
          Given("I setup the wiremock stubs")
          stubAuthSuccess()
          stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinesses))

          When(s"POST ${routes.BusinessNameConfirmationController.show(id).url} is called")
          val res = submitBusinessNameConfirmation(id, Some(No))(Map(ITSASessionKeys.FullNameSessionKey -> "test name"))

          res must have(
            httpStatus(SEE_OTHER),
            redirectURI(BusinessNameUri)
          )
        }
      }
    }
  }
}