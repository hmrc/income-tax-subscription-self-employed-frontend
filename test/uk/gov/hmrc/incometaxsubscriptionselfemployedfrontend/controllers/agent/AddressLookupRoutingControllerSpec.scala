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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent

import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.generic.AddressLookupRoutingControllerGenericSpec

class AddressLookupRoutingControllerSpec extends AddressLookupRoutingControllerGenericSpec {

  val isAgent = true

  override val controllerName: String = "AddressLookupRoutingController"
  override def authorisedRoutes: Map[String, Action[AnyContent]] = Map(
    "initialiseAddressLookupJourney" -> testAddressLookupRoutingController.initialiseAddressLookupJourney(businessId, isEditMode = false),
    "addressLookupRedirect" -> testAddressLookupRoutingController.addressLookupRedirect(businessId, None, isEditMode = false)
  )

  def testAddressLookupRoutingController = new AddressLookupRoutingController(
    mockMessagesControllerComponents,
    mockAuthService,
    mockAddressLookupConnector,
    mockIncomeTaxSubscriptionConnector,
    mockMultipleSelfEmploymentsService
  )

  val continueUrl: String = s"http://localhost:9563/report-quarterly/income-and-expenses/sign-up/self-employments/client/details/address-lookup/$businessId"

  def redirectUrl: String = "http://testLocation?id=" + addressId

  def redirect5: String = routes.BusinessListCYAController.show.url

  def redirect4: String = routes.BusinessListCYAController.show.url

  def redirect3: String = routes.BusinessAccountingMethodController.show(Some(businessId)).url

  def redirect2: String = routes.SelfEmployedCYAController.show(businessId, isEditMode = true).url

  def redirect1: String = routes.SelfEmployedCYAController.show(businessId).url
}
