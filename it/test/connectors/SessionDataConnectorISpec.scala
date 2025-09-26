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

package connectors

import connectors.stubs.SessionDataConnectorStub._
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import play.api.libs.json.{JsObject, Json, OFormat}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.SessionDataConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSessionDataHttpParser.{InvalidJson, UnexpectedStatusFailure}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.SaveSessionDataHttpParser

class SessionDataConnectorISpec extends ComponentSpecBase {

  lazy val connector: SessionDataConnector = app.injector.instanceOf[SessionDataConnector]
  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  case class DummyModel(body: String)

  object DummyModel {
    implicit val format: OFormat[DummyModel] = Json.format[DummyModel]
  }

  "getSessionData" should {
    "return the provided model" in {
      val successfulResponseBody: JsObject = Json.obj("body" -> "Test Body")

      stubGetSessionData(id)(OK, successfulResponseBody)

      val res = connector.getSessionData[DummyModel](id)

      await(res) mustBe Right(Some(DummyModel(body = "Test Body")))
    }

    "Return InvalidJson" in {
      stubGetSessionData(id)(OK, Json.obj())

      val res = connector.getSessionData[DummyModel](id)

      await(res) mustBe Left(InvalidJson)
    }

    "Return None" in {
      stubGetSessionData(id)(NO_CONTENT, Json.obj())

      val res = connector.getSessionData[DummyModel](id)

      await(res) mustBe Right(None)

    }
    "Return UnexpectedStatusFailure" in {
      stubGetSessionData(id)(INTERNAL_SERVER_ERROR, Json.obj())

      val res = connector.getSessionData[DummyModel](id)

      await(res) mustBe Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR))

    }
  }

  "saveSessionData" must {
    "return a successful response" when {
      "an OK response was returned" in {
        stubSaveSessionData(id, DummyModel("test"))(OK)

        val res = connector.saveSessionData(id, DummyModel("test"))

        await(res) mustBe Right(SaveSessionDataHttpParser.SaveSessionDataSuccessResponse)
      }
    }
    "return an unexpected status failure" when {
      "an unexpected status was returned" in {
        stubSaveSessionData(id, DummyModel("test"))(INTERNAL_SERVER_ERROR)

        val res = connector.saveSessionData(id, DummyModel("test"))

        await(res) mustBe Left(SaveSessionDataHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR))
      }
    }
  }

}
