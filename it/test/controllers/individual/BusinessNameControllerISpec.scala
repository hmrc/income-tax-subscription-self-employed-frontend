/*
 * Copyright 2024 HM Revenue & Customs
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

import connectors.stubs.IncomeTaxSubscriptionConnectorStub._
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.AuthStub
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.{incomeSourcesComplete, soleTraderBusinessesKey}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.individual.routes
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{SoleTraderBusiness, SoleTraderBusinesses}

class BusinessNameControllerISpec extends ComponentSpecBase {

  s"GET ${routes.BusinessNameController.show(id)}" when {
    "the user is unauthorised" should {
      "redirect to the login page" in {
        AuthStub.stubUnauthorised()

        val result = getBusinessName(id)

        result must have(
          httpStatus(SEE_OTHER),
          redirectURI(ggSignInURI)
        )
      }
    }
    "the user has previously entered a business name" should {
      "display the page with the field filled" in {
        AuthStub.stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(
          responseStatus = OK,
          responseBody = Json.toJson(soleTraderBusinesses)
        )

        val result = getBusinessName(id)

        result must have(
          httpStatus(OK),
          pageTitle(messages("individual.business-name.title") + titleSuffix),
          textField("business-name", "test name")
        )
      }
    }
    "the user has not been on the page before for this business" should {
      "display the page with the field empty" in {
        AuthStub.stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(NO_CONTENT)

        val result = getBusinessName(id)

        result must have(
          httpStatus(OK),
          pageTitle(messages("individual.business-name.title") + titleSuffix),
          textField("business-name", "")
        )
      }
    }
  }

  s"POST ${routes.BusinessNameController.submit(id)}" when {
    "the user is unauthorised" should {
      "redirect to the login page" in {
        AuthStub.stubUnauthorised()

        val result = submitBusinessName(name = Some("test name"), id = id)

        result must have(
          httpStatus(SEE_OTHER),
          redirectURI(ggSignInURI)
        )
      }
    }
    "the user submits with no business name" should {
      "return a bad request with the page content" in {
        AuthStub.stubAuthSuccess()

        val result = submitBusinessName(name = None, id = id)

        result must have(
          httpStatus(BAD_REQUEST),
          pageTitle("Error: " + messages("individual.business-name.title") + titleSuffix)
        )
      }
    }
    "the user submits a valid business name" should {
      "save the name and redirect to the next page" in {
        AuthStub.stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(NO_CONTENT)
        stubSaveSubscriptionData(
          reference = reference,
          id = soleTraderBusinessesKey,
          body = Json.toJson(SoleTraderBusinesses(Seq(SoleTraderBusiness(
            id = id,
            name = Some("test name")
          ))))
        )(OK)
        stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)

        val result = submitBusinessName(name = Some("test name"), id = id)

        result must have(
          httpStatus(SEE_OTHER),
          // TODO: update redirect once BusinessTradeNameController is created
          redirectURI(routes.FullIncomeSourceController.show(id).url)
        )
      }
    }
    "the user submits a valid business name in edit mode" should {
      "save the name and redirect to the check your answers page" in {
        AuthStub.stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(
          responseStatus = OK,
          responseBody = Json.toJson(SoleTraderBusinesses(Seq(SoleTraderBusiness(
            id = id,
            name = Some("old name")
          ))))
        )
        stubSaveSubscriptionData(
          reference = reference,
          id = soleTraderBusinessesKey,
          body = Json.toJson(SoleTraderBusinesses(Seq(SoleTraderBusiness(
            id = id,
            name = Some("test name")
          ))))
        )(OK)
        stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)

        val result = submitBusinessName(name = Some("test name"), id = id, isEditMode = true)

        result must have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.SelfEmployedCYAController.show(id, isEditMode = true).url)
        )
      }
    }
    "the user submits a valid business name in global edit mode" should {
      "save the name and redirect to the check your answers page" in {
        AuthStub.stubAuthSuccess()
        stubGetSubscriptionData(reference, soleTraderBusinessesKey)(NO_CONTENT)
        stubSaveSubscriptionData(
          reference = reference,
          id = soleTraderBusinessesKey,
          body = Json.toJson(SoleTraderBusinesses(Seq(SoleTraderBusiness(
            id = id,
            name = Some("test name")
          ))))
        )(OK)
        stubDeleteSubscriptionData(reference, incomeSourcesComplete)(OK)

        val result = submitBusinessName(name = Some("test name"), id = id, isGlobalEdit = true)

        result must have(
          httpStatus(SEE_OTHER),
          redirectURI(routes.SelfEmployedCYAController.show(id, isGlobalEdit = true).url)
        )
      }
    }
  }
}