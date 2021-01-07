/*
 * Copyright 2021 HM Revenue & Customs
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

import connectors.stubs.IncomeTaxSubscriptionConnectorStub._
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import play.api.libs.json.{JsObject, Json, OFormat}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.IncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSelfEmploymentsSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.{GetSelfEmploymentsHttpParser, PostSelfEmploymentsHttpParser}


class IncomeTaxSubscriptionConnectorISpec extends ComponentSpecBase {

  lazy val connector: IncomeTaxSubscriptionConnector = app.injector.instanceOf[IncomeTaxSubscriptionConnector]
  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  case class DummyModel(body: String)

  object DummyModel {
    implicit val format: OFormat[DummyModel] = Json.format[DummyModel]
  }

  "GetSelfEmployments" should {
    "Return Some DummyModel" in {
      val successfulResponseBody: JsObject = Json.obj("body" -> "Test Body")

      stubGetSelfEmployments(id)(OK, successfulResponseBody)

      val res = connector.getSelfEmployments[DummyModel](id)

      await(res) mustBe Right(Some(DummyModel(body = "Test Body")))
    }

    "Return InvalidJson" in {
      stubGetSelfEmployments(id)(OK, Json.obj())

      val res = connector.getSelfEmployments[DummyModel](id)

      await(res) mustBe Left(GetSelfEmploymentsHttpParser.InvalidJson)
    }

    "Return None" in {
      stubGetSelfEmployments(id)(NO_CONTENT, Json.obj())

      val res = connector.getSelfEmployments[DummyModel](id)

      await(res) mustBe Right(None)

    }
    "Return UnexpectedStatusFailure" in {
      stubGetSelfEmployments(id)(INTERNAL_SERVER_ERROR, Json.obj())

      val res = connector.getSelfEmployments[DummyModel](id)

      await(res) mustBe Left(GetSelfEmploymentsHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR))

    }
  }

  "SaveSelfEmployments" should {
    "Return PostSelfEmploymentsSuccessResponse" in {
      val dummyModel: DummyModel = DummyModel(body = "Test Body")
      stubSaveSelfEmployments(id, body = Json.toJson(dummyModel))(OK)

      val res = connector.saveSelfEmployments[DummyModel](id, dummyModel)

      await(res) mustBe Right(PostSelfEmploymentsSuccessResponse)
    }

    "Return UnexpectedStatusFailure(status)" in {
      val dummyModel: DummyModel = DummyModel(body = "Test Body")
      stubSaveSelfEmployments(id, body = Json.toJson(dummyModel))(INTERNAL_SERVER_ERROR)

      val res = connector.saveSelfEmployments[DummyModel](id, dummyModel)

      await(res) mustBe Left(PostSelfEmploymentsHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR))
    }
  }


}
