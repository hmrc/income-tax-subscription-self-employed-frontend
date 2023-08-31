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
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.businessesKey
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.routes
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{Address, BusinessAddressModel, BusinessNameModel, No, SelfEmploymentData, Yes}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ITSASessionKeys

class BusinessAddressConfirmationControllerISpec extends ComponentSpecBase with FeatureSwitching {

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val crypto : ApplicationCrypto = app.injector.instanceOf[ApplicationCrypto]

  val id: String = "testId"
  val address: Address = Address(
    Seq(
      "1 Long Road",
      "Lonely town"
    ),
    Some("ZZ11ZZ")
  )

  s"GET ${routes.BusinessAddressConfirmationController.show(id).url}" should {
    "return the page" when {
      "there is an existing business address" in {
        Given("I setup the wiremock stubs")
        stubAuthSuccess()
        stubGetSubscriptionData(reference, businessesKey)(
          OK,
          Json.toJson(Seq(SelfEmploymentData(id, businessAddress = Some(BusinessAddressModel(address).encrypt(crypto.QueryParameterCrypto)))))
        )

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
        stubGetSubscriptionData(reference, businessesKey)(NO_CONTENT)

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
          stubGetSubscriptionData(reference, businessesKey)(
            OK,
            Json.toJson(Seq(SelfEmploymentData(id, businessAddress = Some(BusinessAddressModel(address).encrypt(crypto.QueryParameterCrypto)))))
          )

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
          Given("I setup the wiremock stubs")
          stubAuthSuccess()
          stubGetSubscriptionData(reference, businessesKey)(
            OK,
            Json.toJson(Seq(SelfEmploymentData("id2", businessAddress = Some(BusinessAddressModel(address).encrypt(crypto.QueryParameterCrypto)))))
          )
          stubSaveSubscriptionData(reference, businessesKey, Json.toJson(
            Seq(
              SelfEmploymentData("id2", businessAddress = Some(BusinessAddressModel(address).encrypt(crypto.QueryParameterCrypto))),
              SelfEmploymentData(id, businessAddress = Some(BusinessAddressModel(address).encrypt(crypto.QueryParameterCrypto)))
            )
          ))(OK)

          When(s"POST ${routes.BusinessAddressConfirmationController.show(id).url} is called")
          val res = submitClientBusinessAddressConfirmation(id, Some(Yes))(Map(
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
          stubGetSubscriptionData(reference, businessesKey)(
            OK,
            Json.toJson(Seq(SelfEmploymentData(id, businessAddress = Some(BusinessAddressModel(address).encrypt(crypto.QueryParameterCrypto)))))
          )

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