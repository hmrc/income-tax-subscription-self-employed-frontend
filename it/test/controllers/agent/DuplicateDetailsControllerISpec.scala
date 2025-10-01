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

import connectors.stubs.SessionDataConnectorStub.stubGetSessionData
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.AuthStub._
import play.api.http.Status._
import play.api.libs.json.{JsString, Json}
import uk.gov.hmrc.crypto.{ApplicationCrypto, Decrypter, Encrypter}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.routes
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.DuplicateDetails
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ITSASessionKeys

class DuplicateDetailsControllerISpec extends ComponentSpecBase {

  implicit val jsonCrypto: Encrypter with Decrypter = app.injector.instanceOf[ApplicationCrypto].JsonCrypto

  val testName: String = "test-name"
  val testTrade: String = "test-trade"
  val testStartDateBeforeLimit = false

  s"GET ${routes.DuplicateDetailsController.show(id).url}" must {
    "return OK with the page content" when {
      "the duplicate details were successfully retrieved from session" in {
        stubAuthSuccess()
        stubGetSessionData(ITSASessionKeys.REFERENCE)(OK, JsString(reference))
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))
        stubGetSessionData(ITSASessionKeys.DUPLICATE_DETAILS)(
          responseStatus = OK,
          responseBody = Json.toJsObject(DuplicateDetails(id, testName, testTrade, testStartDateBeforeLimit))(DuplicateDetails.encryptedFormat)
        )

        val res = getClientDuplicateDetails(
          id = id,
          isEditMode = false,
          isGlobalEdit = false
        )

        res must have(
          httpStatus(OK),
          pageTitle("There is a problem" + agentTitleSuffix)
        )
      }
    }

    "redirect to the full income source page" when {
      "no duplicate details were found in session" in {
        stubAuthSuccess()
        stubGetSessionData(ITSASessionKeys.REFERENCE)(OK, JsString(reference))
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))
        stubGetSessionData(ITSASessionKeys.DUPLICATE_DETAILS)(responseStatus = NO_CONTENT)

        val res = getClientDuplicateDetails(
          id = id,
          isEditMode = false,
          isGlobalEdit = false
        )

        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.FullIncomeSourceController.show(id).url)
        )
      }

      "duplicate details were found in session, but not for this id" in {
        stubAuthSuccess()
        stubGetSessionData(ITSASessionKeys.REFERENCE)(OK, JsString(reference))
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))
        stubGetSessionData(ITSASessionKeys.DUPLICATE_DETAILS)(
          responseStatus = OK,
          responseBody = Json.toJsObject(DuplicateDetails("other-id", testName, testTrade, testStartDateBeforeLimit))(DuplicateDetails.encryptedFormat)
        )

        val res = getClientDuplicateDetails(
          id = id,
          isEditMode = false,
          isGlobalEdit = false
        )

        res must have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.FullIncomeSourceController.show(id).url)
        )
      }
    }

    "redirect to the login page" when {
      "the user is not authenticated" in {
        stubUnauthorised()

        val res = getClientBusinessCheckYourAnswers(id, isEditMode = false)

        res must have(
          httpStatus(SEE_OTHER),
          redirectURI("/bas-gateway/sign-in")
        )
      }
    }

    "return INTERNAL_SERVER_ERROR" when {
      "there was a problem when attempting to fetch duplicate details" in {
        stubAuthSuccess()
        stubGetSessionData(ITSASessionKeys.REFERENCE)(OK, JsString(reference))
        stubGetSessionData(ITSASessionKeys.NINO)(OK, JsString(testNino))
        stubGetSessionData(ITSASessionKeys.DUPLICATE_DETAILS)(INTERNAL_SERVER_ERROR)

        val res = getClientBusinessCheckYourAnswers(id, isEditMode = false)

        res must have(
          httpStatus(INTERNAL_SERVER_ERROR)
        )
      }
    }
  }

}
