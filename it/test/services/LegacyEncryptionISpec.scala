/*
 * Copyright 2026 HM Revenue & Customs
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

package services

import connectors.stubs.IncomeTaxSubscriptionConnectorStub.stubGetSubscriptionData
import connectors.stubs.SessionDataConnectorStub.stubGetSessionData
import helpers.{ComponentSpecBase, WiremockHelper}
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status.OK
import play.api.libs.json.Json
import uk.gov.hmrc.crypto.{ApplicationCrypto, Decrypter, Encrypter}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.soleTraderBusinessesKey
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.*
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{MultipleSelfEmploymentsService, SessionDataService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ITSASessionKeys

// TODO: Remove these temporary legacy compatibility tests once ApplicationCrypto is fully removed.
class LegacyEncryptionISpec extends ComponentSpecBase with ScalaFutures {

  private lazy val multipleSelfEmploymentsService = app.injector.instanceOf[MultipleSelfEmploymentsService]
  private lazy val sessionDataService = app.injector.instanceOf[SessionDataService]
  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  private lazy val legacyCrypto: Encrypter with Decrypter = app.injector.instanceOf[ApplicationCrypto].JsonCrypto

  private val business = SoleTraderBusiness(
    id = "legacy-business",
    confirmed = true,
    startDateBeforeLimit = Some(true),
    startDate = Some(DateModel("1", "2", "2020")),
    name = Some("Legacy business name"),
    displayName = Some("Legacy business display name"),
    trade = Some("Plumbing"),
    address = Some(Address(Seq("1 Long Road", "Line two"), Some("ZZ1 1ZZ"), Country.UK, Some("123456789")))
  )
  private val businesses = SoleTraderBusinesses(Seq(business))

  private def legacyEncryptedBusinesses = Json.toJson(businesses)(SoleTraderBusinesses.encryptedFormat(legacyCrypto))

  "MultipleSelfEmploymentsService" must {
    "read legacy AES-encrypted sole trader businesses through the real connector" in {
      stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, legacyEncryptedBusinesses)

      multipleSelfEmploymentsService.fetchSoleTraderBusinesses(reference).futureValue mustBe Right(Some(businesses))
      WiremockHelper.verifyGet(s"/income-tax-subscription/subscription-data/$reference/id/$soleTraderBusinessesKey")
    }

    "read a single legacy AES-encrypted business through the real connector" in {
      stubGetSubscriptionData(reference, soleTraderBusinessesKey)(OK, legacyEncryptedBusinesses)

      multipleSelfEmploymentsService.fetchBusiness(reference, business.id).futureValue mustBe Right(Some(business))
    }
  }

  "SessionDataService" must {
    "read legacy AES-encrypted duplicate details through the real connector" in {
      val duplicateDetails = DuplicateDetails("legacy-business", "Legacy business name", "Plumbing", startDateBeforeLimit = true)
      stubGetSessionData(ITSASessionKeys.DUPLICATE_DETAILS)(
        OK, Json.toJson(duplicateDetails)(DuplicateDetails.encryptedFormat(legacyCrypto))
      )

      sessionDataService.getDuplicateDetails(duplicateDetails.id).futureValue mustBe Some(duplicateDetails)
    }
  }
}
