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

import connectors.stubs.AddressLookupConnectorStub._
import connectors.stubs.IncomeTaxSubscriptionConnectorStub.{stubDeleteSubscriptionData, stubGetSubscriptionData, stubSaveSubscriptionData}
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.AuthStub._
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.{incomeSourcesComplete, soleTraderBusinessesKey}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitch.EnableUseRealAddressLookup
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{Address, SoleTraderBusinesses}

import java.net.URLEncoder

class AddressLookupRoutingControllerISpec extends ComponentSpecBase with FeatureSwitching {

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]

  private val addressId = "testId1"

  override def beforeEach(): Unit = {
    disable(EnableUseRealAddressLookup)
    super.beforeEach()
  }

  private def testConfig(continueUrl: String, referrerUrlMaybe: Option[String]): String = testAddressLookupConfig(continueUrl, referrerUrlMaybe)

  private val clientOrIndividual = "" // if this were the client version, we would have /client here, but individual gets nothing.

  def getAddressLookupInitialiseResponse(itsaId: String): WSResponse = getAddressLookupInitialise(itsaId): WSResponse

  def getAddressLookupResponse(itsaId: String, id: String, isEditMode: Boolean): WSResponse = getAddressLookup(itsaId, id, isEditMode)

  private val addressLookupInitialise = "/address-lookup-initialise"

  val soleTraderBusinessesWithoutAddress: SoleTraderBusinesses = soleTraderBusinesses
    .copy(businesses = soleTraderBusinesses.businesses.map(_.copy(address = None)))

  "when the enableAddressLookup is disabled" when {
    s"Post $baseUrl$clientOrIndividual$addressLookupInitialise/$id" when {
      "the Connector receives Accepted and location details in headers" should {
        "redirect to the location" in {
          disable(EnableUseRealAddressLookup)
          Given("I setup the Wiremock stubs")
          val continueUrl = s"http://localhost:9563$baseUrl$clientOrIndividual/details/address-lookup/" + id
          stubAuthSuccess()
          val referrerUrl = URLEncoder.encode(s"$baseUrl$clientOrIndividual$addressLookupInitialise/$id", "UTF8")
          stubInitializeAddressLookup(Json.parse(
            testConfig(continueUrl, Some(referrerUrl))
          ))(s"$continueUrl?id=$id", ACCEPTED)

          When(s"GET $clientOrIndividual$addressLookupInitialise/$id is called")
          val res = getAddressLookupInitialiseResponse(id)

          Then("should return an SEE_OTHER with Address lookup location")
          res must have(
            httpStatus(SEE_OTHER),
            redirectURI(s"$continueUrl?id=$id")
          )
        }
      }
    }

    s"GET $baseUrl$clientOrIndividual/details/address-lookup/$id" when {
      "the address lookup service return successful JSON details" should {
        "redirect to sole trader check your answers" in {
          Given("I setup the Wiremock stubs")
          stubAuthSuccess()
          disable(EnableUseRealAddressLookup)

          stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinessesWithoutAddress))
          stubGetAddressLookupDetails(addressId)(OK, Json.obj("address" -> Json.toJson(address)(Address.format)))
          stubSaveSubscriptionData(reference, soleTraderBusinessesKey, Json.toJson(soleTraderBusinesses))(OK)
          stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)

          When(s"GET $clientOrIndividual/details/address-lookup/" + id + " is called")
          val res = getAddressLookupResponse(id, addressId, isEditMode = false)

          Then("Should return a SEE_OTHER with a redirect location of business check your answers")
          res must have(
            httpStatus(SEE_OTHER),
            redirectURI(BusinessCYAUri)
          )
        }
      }
    }
  }

  "when the enableAddressLookup is enabled" when {
    s"Post $baseUrl$clientOrIndividual$addressLookupInitialise/$id" when {

      "the Connector receives Accepted and location details in headers" should {
        "redirect to the location" in {
          enable(EnableUseRealAddressLookup)
          Given("I setup the Wiremock stubs")
          val continueUrl = s"http://localhost:9563$baseUrl$clientOrIndividual/details/address-lookup/" + id
          stubAuthSuccess()
          val referrerUrl = URLEncoder.encode(s"$baseUrl$clientOrIndividual$addressLookupInitialise/$id", "UTF8")
          stubInitializeAddressLookup(Json.parse(
            testConfig(continueUrl, Some(referrerUrl))
          ))(s"$continueUrl?id=$id", ACCEPTED)

          When(s"Post $clientOrIndividual$addressLookupInitialise/$id is called")
          val res = getAddressLookupInitialiseResponse(id)

          Then("should return an SEE_OTHER with Address lookup location")
          res must have(
            httpStatus(SEE_OTHER),
            redirectURI(s"$continueUrl?id=$id")
          )
        }
      }

      "the Connector receives Accepted and no location details in headers" should {
        "return INTERNAL_SERVER_ERROR" in {
          enable(EnableUseRealAddressLookup)
          Given("I setup the Wiremock stubs")
          stubAuthSuccess()
          stubInitializeAddressLookup(Json.parse(
            testConfig("http://localhost/continueUrl", None)
          ))("http://localhost/testLocation", ACCEPTED)

          When(s"Post $clientOrIndividual$addressLookupInitialise/$id is called")
          val res = getAddressLookupInitialiseResponse(id)

          Then("should return an Internal server page")
          res must have(
            httpStatus(INTERNAL_SERVER_ERROR)
          )
        }
      }
    }

    s"GET $baseUrl$clientOrIndividual/details/address-lookup/$id" when {
      "it is not in edit mode" when {
        "the address lookup service return successful JSON details" should {
          "redirect to sole trader check your answers page" in {
            Given("I setup the Wiremock stubs")
            stubAuthSuccess()
            enable(EnableUseRealAddressLookup)
            stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinessesWithoutAddress))
            stubGetAddressLookupDetails(addressId)(OK, Json.obj("address" -> Json.toJson(address)(Address.format)))
            stubSaveSubscriptionData(reference, soleTraderBusinessesKey, Json.toJson(soleTraderBusinesses))(OK)
            stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)

            When(s"GET $clientOrIndividual/details/address-lookup/" + id + " is called")
            val res = getAddressLookupResponse(id, addressId, isEditMode = false)

            Then("Should return a SEE_OTHER with a redirect location of business check your answers")
            res must have(
              httpStatus(SEE_OTHER),
              redirectURI(BusinessCYAUri)
            )
          }
        }

        "the address id does not return from address service" should {
          "return InternalServerException" in {
            Given("I setup the Wiremock stubs")
            stubAuthSuccess()
            enable(EnableUseRealAddressLookup)

            stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinessesWithoutAddress))
            getAddressDetailsUrlNoId(OK, Json.toJson(address)(Address.format))

            When(s"GET $clientOrIndividual/details/address-lookup/" + id + " is called")
            val res = getAddressLookupResponse(id, addressId, isEditMode = false)

            Then("Should return an internal server error")
            res must have(
              httpStatus(INTERNAL_SERVER_ERROR)
            )
          }
        }
      }

      "it is in edit mode" when {
        "the address lookup service return successful JSON details" should {
          "redirect to sole trader check your answers page" in {
            Given("I setup the Wiremock stubs")
            stubAuthSuccess()
            enable(EnableUseRealAddressLookup)

            stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinessesWithoutAddress))
            stubGetAddressLookupDetails(addressId)(OK, Json.obj("address" -> Json.toJson(address)(Address.format)))
            stubSaveSubscriptionData(reference, soleTraderBusinessesKey, Json.toJson(soleTraderBusinesses))(OK)
            stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)

            When("GET /details/address-lookup/" + id + " is called")
            val res = getAddressLookupResponse(id, addressId, isEditMode = true)

            Then("Should return a SEE_OTHER with a redirect location of sole trader check your answers")
            res must have(
              httpStatus(SEE_OTHER),
              redirectURI(BusinessCYAUri)
            )
          }
        }

        "the address lookup service return NOT_FOUND" in {
          Given("I setup the Wiremock stubs")
          stubAuthSuccess()
          enable(EnableUseRealAddressLookup)
          stubGetAddressLookupDetails(addressId)(NOT_FOUND)
          stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinessesWithoutAddress))

          When(s"GET $clientOrIndividual/details/address-lookup/$id is called")
          val res = getAddressLookupResponse(id, addressId, isEditMode = true)


          Then("Should return a INTERNAL_SERVER_ERROR")
          res must have(
            httpStatus(INTERNAL_SERVER_ERROR)
          )
        }

        "the address lookup service return invalid Json" in {
          enable(EnableUseRealAddressLookup)

          Given("I setup the Wiremock stubs")
          stubAuthSuccess()
          stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinessesWithoutAddress))
          stubGetAddressLookupDetails(addressId)(OK, Json.obj("abc" -> "def"))

          When(s"POST $clientOrIndividual/details/address-lookup/$id is called")
          val res = getAddressLookupResponse(id, addressId, isEditMode = true)

          Then("Should return a INTERNAL_SERVER_ERROR")
          res must have(
            httpStatus(INTERNAL_SERVER_ERROR)
          )
        }

        "the address lookup service return 400" in {
          enable(EnableUseRealAddressLookup)

          Given("I setup the Wiremock stubs")
          stubAuthSuccess()
          stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, Json.toJson(soleTraderBusinessesWithoutAddress))
          stubGetAddressLookupDetails(addressId)(BAD_REQUEST)

          When(s"POST $clientOrIndividual/details/address-lookup/$id is called")
          val res = getAddressLookupResponse(id, addressId, isEditMode = true)

          Then("Should return a INTERNAL_SERVER_ERROR")
          res must have(
            httpStatus(INTERNAL_SERVER_ERROR)
          )
        }
      }
    }
  }
}
