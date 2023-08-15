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

import connectors.stubs.IncomeTaxSubscriptionConnectorStub.{stubGetSubscriptionData, stubSaveSubscriptionData}
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.AuthStub.stubAuthSuccess
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.businessesKey
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.individual.routes
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._

class BusinessAddressConfirmationControllerISpec extends ComponentSpecBase with FeatureSwitching {

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val crypto: ApplicationCrypto = app.injector.instanceOf[ApplicationCrypto]

  val id: String = "testId"
  val name: String = "FirstName LastName"
  val address: Address = Address(
    Seq(
      "1 Long Road",
      "Lonely town"
    ),
    Some("ZZ11ZZ")
  )
  val existingBusinesses: Seq[SelfEmploymentData] = {
    Seq(
      SelfEmploymentData(id = "old-id", businessAddress = Some(BusinessAddressModel("test-audit-ref", address).encrypt(crypto.QueryParameterCrypto)))
    )
  }

  s"GET ${routes.BusinessAddressConfirmationController.show(id).url}" should {
    "return INTERNAL_SERVER_ERROR" when {
      "there was an issue fetching the businesses data" in {
        Given("I setup the wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, businessesKey)(INTERNAL_SERVER_ERROR)

        When(s"GET ${routes.BusinessAddressConfirmationController.show(id).url} is called")
        val res = getBusinessAddressConfirmation(id)()

        res must have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }
    "redirect to the address lookup initialise route" when {
      "there is no existing business address" in {
        Given("I setup the wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, businessesKey)(NO_CONTENT)

        When(s"GET ${routes.BusinessAddressConfirmationController.show(id).url} is called")
        val res = getBusinessAddressConfirmation(id)()

        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(businessAddressInitialiseUri(id))
        )
      }
    }
    "return OK with page content" when {
      "there is an existing business address" in {
        Given("I setup the wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, businessesKey)(
          OK,
          Json.toJson(existingBusinesses)
        )

        When(s"GET ${routes.BusinessAddressConfirmationController.show(id).url} is called")
        val res = getBusinessAddressConfirmation(id)()

        res must have(
          httpStatus(OK),
          pageTitle("Confirm business address" + titleSuffix)
        )
      }
    }
  }

  s"POST ${routes.BusinessAddressConfirmationController.submit(id).url}" should {
    "return INTERNAL_SERVER_ERROR" when {
      "there was an issue fetching the businesses data" in {
        Given("I setup the wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, businessesKey)(INTERNAL_SERVER_ERROR)

        When(s"POST ${routes.BusinessAddressConfirmationController.show(id).url} is called")
        val res = submitBusinessAddressConfirmation(id, None)()

        res must have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }
    "redirect to the address lookup initialise route" when {
      "there is no existing business address" in {
        Given("I setup the wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, businessesKey)(NO_CONTENT)

        When(s"POST ${routes.BusinessAddressConfirmationController.show(id).url} is called")
        val res = submitBusinessAddressConfirmation(id, None)()

        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(businessAddressInitialiseUri(id))
        )
      }
      "the user selects their business address is not the same as presented" in {
        Given("I setup the wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, businessesKey)(
          OK,
          Json.toJson(existingBusinesses)
        )

        When(s"POST ${routes.BusinessAddressConfirmationController.show(id).url} is called")
        val res = submitBusinessAddressConfirmation(id, Some(No))()

        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(businessAddressInitialiseUri(id))
        )
      }
    }
    "save the address and redirect to the sole trader CYA page" when {
      "the user selects their address matches the one presented" in {
        Given("I setup the wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, businessesKey)(
          OK,
          Json.toJson(existingBusinesses)
        )
        stubSaveSubscriptionData(
          reference,
          businessesKey,
          Json.toJson(existingBusinesses :+ SelfEmploymentData(id, businessAddress = existingBusinesses.head.businessAddress))
        )(OK)

        When(s"POST ${routes.BusinessAddressConfirmationController.show(id).url} is called")
        val res = submitBusinessAddressConfirmation(id, Some(Yes))()

        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(BusinessCYAUri)
        )
      }
    }
  }
}