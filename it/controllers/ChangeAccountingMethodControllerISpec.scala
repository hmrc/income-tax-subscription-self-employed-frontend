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

package controllers

import connectors.stubs.IncomeTaxSubscriptionConnectorStub._
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.AuthStub._
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.businessAccountingMethodKey
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes

class ChangeAccountingMethodControllerISpec extends ComponentSpecBase with FeatureSwitching {

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val id: String = "test-id"

  s"GET ${routes.ChangeAccountingMethodController.show(id)}" should {
    s"return $OK with content" in {
      Given("I setup the Wiremock stubs")
      stubAuthSuccess()

      When(s"GET ${routes.ChangeAccountingMethodController.show(id)}")
      val res = getChangeAccountingMethod(id)

      Then("Should return OK with the change accounting method page")
      res must have(
        httpStatus(OK),
        pageTitle("Changing accounting method" + titleSuffix)
      )
    }
  }

  s"POST ${routes.ChangeAccountingMethodController.submit(id)}" should {
    s"return $OK with content" in {
      Given("I setup the Wiremock stubs")
      stubAuthSuccess()

      When(s"POST ${routes.ChangeAccountingMethodController.submit(id)}")
      val res = submitChangeAccountingMethod(id)

      Then("Should return SEE_OTHER going to the accounting method page in edit mode")
      res must have(
        httpStatus(SEE_OTHER),
        redirectURI(s"$BusinessAccountingMethodUri?id=$id&isEditMode=true")
      )
    }
  }
}
