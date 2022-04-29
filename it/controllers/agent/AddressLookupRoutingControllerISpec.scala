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

import connectors.stubs.AddressLookupConnectorStub._
import connectors.stubs.IncomeTaxSubscriptionConnectorStub.{stubGetSubscriptionData, stubSaveSubscriptionData}
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.AuthStub._
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.{businessAccountingMethodKey, businessesKey}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitch.SaveAndRetrieve
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{Address, BusinessAddressModel}

import java.net.URLEncoder

class AddressLookupRoutingControllerISpec extends ComponentSpecBase with FeatureSwitching {

  private val addressId = "testId1"

  val testBusinessAddressModel: BusinessAddressModel = BusinessAddressModel(addressId, Address(Seq("line1", "line2", "line3"), Some("TF3 4NT")))

  override def beforeEach(): Unit = {
    disable(SaveAndRetrieve)
    super.beforeEach()
  }

  private val businessId = "12345"

  private def testConfig(contineUrl: String, referrerUrlMaybe: Option[String]): String = testAddressLookupConfigClient(contineUrl, referrerUrlMaybe)

  private val clientOrIndividual = "/client"

  def getAddressLookupInitialiseResponse(itsaId: String): WSResponse = getClientAddressLookupInitialise(itsaId): WSResponse

  def getAddressLookupResponse(itsaId: String, id: String, isEditMode: Boolean): WSResponse = getClientAddressLookup(itsaId, id, isEditMode)

  private val addressLookupInitialise = "/address-lookup-initialise"

  s"GET $baseUrl$clientOrIndividual$addressLookupInitialise/$businessId" when {

    "the Connector receives NO_CONTENT and location details in headers" should {
      "with location details in headers" in {
        Given("I setup the Wiremock stubs")
        val continueUrl = s"http://localhost:9563$baseUrl$clientOrIndividual/details/address-lookup/" + businessId
        stubAuthSuccess()
        val referrerUrl = URLEncoder.encode(s"$baseUrl$clientOrIndividual$addressLookupInitialise/$businessId", "UTF8")
        stubInitializeAddressLookup(Json.parse(
          testConfig(continueUrl, Some(referrerUrl))
        ))(s"$continueUrl?id=$businessId", ACCEPTED)

        When(s"GET $clientOrIndividual$addressLookupInitialise/$businessId is called")
        val res = getAddressLookupInitialiseResponse(businessId)

        Then("should return an SEE_OTHER with Address lookup location")
        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(s"$continueUrl?id=$businessId")
        )
      }
    }

    "the Connector receives OK" should {
      "with location details in headers" in {
        Given("I setup the Wiremock stubs")
        stubAuthSuccess()
        stubInitializeAddressLookup(Json.parse(
          testConfig("http://localhost/continueUrl", Some("not used"))
        ))("http://localhost/testLocation", OK)

        When(s"GET $clientOrIndividual$addressLookupInitialise/$businessId is called")
        val res = getAddressLookupInitialiseResponse(businessId)

        Then("should return an Internal server page")
        res must have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }
  }

  s"GET $baseUrl$clientOrIndividual/details/address-lookup/$businessId" when {
    "save and retrieve is enabled" when {
      "it is not in edit mode" when {
        "business accounting method is not defined" when {
          "the address lookup service return successful JSON details" should {
            "redirect to sole trader accounting method page" in {
              Given("I setup the Wiremock stubs")
              stubAuthSuccess()

              enable(SaveAndRetrieve)
              stubGetSubscriptionData(reference, businessAccountingMethodKey)(NO_CONTENT)
              stubGetAddressLookupDetails(addressId)(OK, Json.toJson(testBusinessAddressModel))
              stubSaveSubscriptionData(reference, businessesKey, Json.toJson(testBusinessAddressModel))(OK)

              When("GET /details/address-lookup/" + businessId + " is called")
              val res = getAddressLookupResponse(businessId, addressId, isEditMode = false)

              Then("Should return a SEE_OTHER with a redirect location of business accounting method")
              res must have(
                httpStatus(SEE_OTHER),
                redirectURI(ClientBusinessAccountingMethodUri)
              )
            }
          }
        }

        "business accounting method is defined" when {
          "the address lookup service return successful JSON details" should {
            "redirect to sole trader check your answers page" in {
              Given("I setup the Wiremock stubs")
              stubAuthSuccess()
              enable(SaveAndRetrieve)
              stubGetAddressLookupDetails(addressId)(OK, Json.toJson(testBusinessAddressModel))
              stubSaveSubscriptionData(reference, businessesKey, Json.toJson(testBusinessAddressModel))(OK)
              stubGetSubscriptionData(reference, businessAccountingMethodKey)(OK, Json.toJson(testAccountingMethodModel))

              When("GET /details/address-lookup/" + businessId + " is called")
              val res = getAddressLookupResponse(businessId, addressId, isEditMode = false)

              Then("Should return a SEE_OTHER with a redirect location of sole trader check your answers pagee")
              res must have(
                httpStatus(SEE_OTHER),
                redirectURI(ClientBusinessCYAUri)
              )
            }
          }
        }
      }
      "it is in edit mode" when {
        "the address lookup service return successful JSON details" should {
          "redirect to sole trader check your answers page" in {
            Given("I setup the Wiremock stubs")
            stubAuthSuccess()
            enable(SaveAndRetrieve)
            stubGetAddressLookupDetails(addressId)(OK, Json.toJson(testBusinessAddressModel))
            stubSaveSubscriptionData(reference, businessesKey, Json.toJson(testBusinessAddressModel))(OK)
            stubGetSubscriptionData(reference, businessAccountingMethodKey)(OK, Json.toJson(testAccountingMethodModel))

            When("GET /details/address-lookup/" + businessId + " is called")
            val res = getAddressLookupResponse(businessId, addressId, isEditMode = true)

            Then("Should return a SEE_OTHER with a redirect location of sole trader check your answers")
            res must have(
              httpStatus(SEE_OTHER),
              redirectURI(ClientBusinessCYAUri)
            )
          }
        }
      }
    }

    "save and retrieve is disabled" when {
      "it is not in edit mode" when {
        "the address lookup service return successful JSON details" should {
          "redirect to business list check your answers page" in {
            Given("I setup the Wiremock stubs")
            stubAuthSuccess()
            stubGetAddressLookupDetails(addressId)(OK, Json.toJson(testBusinessAddressModel))
            stubSaveSubscriptionData(reference, businessesKey, Json.toJson(testBusinessAddressModel))(OK)
            stubGetSubscriptionData(reference, businessAccountingMethodKey)(NO_CONTENT)

            When(s"GET $clientOrIndividual/details/address-lookup/" + businessId + " is called")
            val res = getAddressLookupResponse(businessId, addressId, isEditMode = false)

            Then("Should return a SEE_OTHER with a redirect location of business list check your answers")
            res must have(
              httpStatus(SEE_OTHER),
              redirectURI(ClientBusinessListCYAUri)
            )
          }
        }
      }

      "it is in edit mode" when {
        "the address lookup service return successful JSON details" should {
          "redirect to business list check your answers page" in {
            Given("I setup the Wiremock stubs")
            stubAuthSuccess()
            stubGetAddressLookupDetails(addressId)(OK, Json.toJson(testBusinessAddressModel))
            stubSaveSubscriptionData(reference, businessesKey, Json.toJson(testBusinessAddressModel))(OK)
            stubGetSubscriptionData(reference, businessAccountingMethodKey)(NO_CONTENT)

            When("GET /details/address-lookup/" + businessId + " is called")
            val res = getAddressLookupResponse(businessId, addressId, isEditMode = true)

            Then("Should return a SEE_OTHER with a redirect location of business list check your answers page")
            res must have(
              httpStatus(SEE_OTHER),
              redirectURI(ClientBusinessListCYAUri)
            )
          }
        }

        "the address lookup service return NOT_FOUND" in {
          Given("I setup the Wiremock stubs")
          stubAuthSuccess()
          stubGetAddressLookupDetails(addressId)(NOT_FOUND)
          stubGetSubscriptionData(reference, businessAccountingMethodKey)(NO_CONTENT)

          When(s"GET $clientOrIndividual/details/address-lookup/$businessId is called")
          val res = getAddressLookupResponse(businessId, addressId, isEditMode = true)

          Then("Should return a INTERNAL_SERVER_ERROR")
          res must have(
            httpStatus(INTERNAL_SERVER_ERROR)
          )
        }

        "the address lookup service return invalid Json" in {
          Given("I setup the Wiremock stubs")
          stubAuthSuccess()
          stubGetAddressLookupDetails(addressId)(OK, Json.obj("abc" -> "def"))
          stubGetSubscriptionData(reference, businessAccountingMethodKey)(NO_CONTENT)

          When(s"POST $clientOrIndividual/details/address-lookup/$businessId is called")
          val res = getAddressLookupResponse(businessId, addressId, isEditMode = true)


          Then("Should return a INTERNAL_SERVER_ERROR")
          res must have(
            httpStatus(INTERNAL_SERVER_ERROR)
          )
        }

        "the address lookup service return 400" in {
          Given("I setup the Wiremock stubs")
          stubAuthSuccess()
          stubGetAddressLookupDetails(addressId)(BAD_REQUEST)
          stubGetSubscriptionData(reference, businessAccountingMethodKey)(NO_CONTENT)

          When(s"POST $clientOrIndividual/details/address-lookup/$businessId is called")
          val res = getAddressLookupResponse(businessId, addressId, isEditMode = true)

          Then("Should return a INTERNAL_SERVER_ERROR")
          res must have(
            httpStatus(INTERNAL_SERVER_ERROR)
          )
        }
      }
    }
  }
}
