/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.SaveSessionDataHttpParser.SaveSessionDataSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.{GetSessionDataHttpParser, SaveSessionDataHttpParser}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.mocks.MockSessionDataConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.DuplicateDetails
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ITSASessionKeys

import scala.concurrent.ExecutionContext.Implicits.global

class SessionDataServiceSpec extends PlaySpec with GuiceOneAppPerSuite with MockSessionDataConnector {

  val applicationCrypto: ApplicationCrypto = app.injector.instanceOf[ApplicationCrypto]

  trait Setup {
    val service: SessionDataService = new SessionDataService(applicationCrypto, mockSessionDataConnector)
  }

  val testReference: String = "test-reference"
  val testNino: String = "test-nino"

  def duplicateDetails(id: String): DuplicateDetails = DuplicateDetails(
    id = id,
    name = "test-name",
    trade = "test-trade",
    startDateBeforeLimit = false
  )

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "fetchReference" must {
    "return a reference" when {
      "the connector returns a valid result" in new Setup {
        mockGetSessionData(ITSASessionKeys.REFERENCE)(Right(Some(testReference)))

        await(service.fetchReference) mustBe Right(Some(testReference))
      }
    }
    "return no reference" when {
      "the connector returns no data" in new Setup {
        mockGetSessionData(ITSASessionKeys.REFERENCE)(Right(None))

        await(service.fetchReference) mustBe Right(None)
      }
    }
    "return an error" when {
      "the connector returns an error" in new Setup {
        mockGetSessionData(ITSASessionKeys.REFERENCE)(Left(GetSessionDataHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))

        await(service.fetchReference) mustBe Left(GetSessionDataHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR))
      }
    }
  }

  "fetchNino" must {
    "return a nino" when {
      "the connector returns a valid result" in new Setup {
        mockGetSessionData(ITSASessionKeys.NINO)(Right(Some(testNino)))

        await(service.fetchNino) mustBe Right(Some(testNino))
      }
    }
    "return no nino" when {
      "the connector returns no data" in new Setup {
        mockGetSessionData(ITSASessionKeys.NINO)(Right(None))

        await(service.fetchNino) mustBe Right(None)
      }
    }
    "return an error" when {
      "the connector returns an error" in new Setup {
        mockGetSessionData(ITSASessionKeys.NINO)(Left(GetSessionDataHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))

        await(service.fetchNino) mustBe Left(GetSessionDataHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR))
      }
    }
  }

  "getDuplicateDetails" must {
    "return duplicate details" when {
      "the connector returns duplicate details with the specified id" in new Setup {
        mockGetSessionData(ITSASessionKeys.DUPLICATE_DETAILS)(Right(Some(duplicateDetails("id"))))

        await(service.getDuplicateDetails("id")) mustBe Some(duplicateDetails("id"))
      }
    }
    "return no duplicate details" when {
      "the connector returns duplicate details with a different id than specified" in new Setup {
        mockGetSessionData(ITSASessionKeys.DUPLICATE_DETAILS)(Right(Some(duplicateDetails("id-2"))))

        await(service.getDuplicateDetails("id")) mustBe None
      }
      "the connector returns no data" in new Setup {
        mockGetSessionData(ITSASessionKeys.DUPLICATE_DETAILS)(Right(None))

        await(service.getDuplicateDetails("id")) mustBe None
      }
    }
    "throw an exception" when {
      "the connector returns an error" in new Setup {
        mockGetSessionData(ITSASessionKeys.DUPLICATE_DETAILS)(Left(GetSessionDataHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))

        intercept[InternalServerException](await(service.getDuplicateDetails("id")))
          .message mustBe s"[SessionDataService][getDuplicateDetails] - Unable to get duplicate details from session - ${GetSessionDataHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)}"
      }
    }
  }

  "saveDuplicateDetails" must {
    "return a save success response" when {
      "the connector returns a save success response" in new Setup {
        mockSaveSessionData(ITSASessionKeys.DUPLICATE_DETAILS, duplicateDetails("id"))(Right(SaveSessionDataSuccessResponse))

        await(service.saveDuplicateDetails(duplicateDetails("id"))) mustBe SaveSessionDataSuccessResponse
      }
    }
    "throw an exception" when {
      "the connector returns a save failure response" in new Setup {
        mockSaveSessionData(ITSASessionKeys.DUPLICATE_DETAILS, duplicateDetails("id"))(
          Left(SaveSessionDataHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR))
        )

        intercept[InternalServerException](await(service.saveDuplicateDetails(duplicateDetails("id"))))
          .message mustBe s"[SessionDataService][saveDuplicateDetails] - Unable to save duplicate details to session - ${SaveSessionDataHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)}"
      }
    }
  }

}
