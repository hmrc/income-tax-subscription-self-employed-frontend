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

import connectors.stubs.IncomeTaxSubscriptionConnectorStub._
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import play.api.libs.json.{JsObject, Json, OFormat}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.IncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.DeleteSubscriptionDetailsHttpParser.DeleteSubscriptionDetailsSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSubscriptionDetailsSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.{DeleteSubscriptionDetailsHttpParser, GetSelfEmploymentsHttpParser, PostSelfEmploymentsHttpParser}

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

      stubGetSubscriptionData(reference, id)(OK, successfulResponseBody)

      val res = connector.getSubscriptionDetails[DummyModel](reference, id)

      await(res) mustBe Right(Some(DummyModel(body = "Test Body")))
    }

    "Return InvalidJson" in {
      stubGetSubscriptionData(reference, id)(OK, Json.obj())

      val res = connector.getSubscriptionDetails[DummyModel](reference, id)

      await(res) mustBe Left(GetSelfEmploymentsHttpParser.InvalidJson)
    }

    "Return None" in {
      stubGetSubscriptionData(reference, id)(NO_CONTENT, Json.obj())

      val res = connector.getSubscriptionDetails[DummyModel](reference, id)

      await(res) mustBe Right(None)

    }
    "Return UnexpectedStatusFailure" in {
      stubGetSubscriptionData(reference, id)(INTERNAL_SERVER_ERROR, Json.obj())

      val res = connector.getSubscriptionDetails[DummyModel](reference, id)

      await(res) mustBe Left(GetSelfEmploymentsHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR))

    }
  }

  "SaveSelfEmployments" should {
    "Return PostSubscriptionDetailsSuccessResponse" in {
      val dummyModel: DummyModel = DummyModel(body = "Test Body")
      stubSaveSubscriptionData(reference, id, body = Json.toJson(dummyModel))(OK)

      val res = connector.saveSubscriptionDetails[DummyModel](reference, id, dummyModel)

      await(res) mustBe Right(PostSubscriptionDetailsSuccessResponse)
    }

    "Return UnexpectedStatusFailure(status)" in {
      val dummyModel: DummyModel = DummyModel(body = "Test Body")
      stubSaveSubscriptionData(reference, id, body = Json.toJson(dummyModel))(INTERNAL_SERVER_ERROR)

      val res = connector.saveSubscriptionDetails[DummyModel](reference, id, dummyModel)

      await(res) mustBe Left(PostSelfEmploymentsHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR))
    }
  }

  "deleteSubscriptionDetails" should {
    "return a DeleteSubscriptionDetailsSuccessResponse" when {
      "the call returned an OK response" in {
        stubDeleteSubscriptionData(reference, id)(OK)

        val res = connector.deleteSubscriptionDetails(reference, id)

        await(res) mustBe Right(DeleteSubscriptionDetailsSuccessResponse)
      }
    }
    "return an UnexpectedStatusFailure(status)" when {
      "an non OK status was returned" in {
        stubDeleteSubscriptionData(reference, id)(INTERNAL_SERVER_ERROR)

        val res = connector.deleteSubscriptionDetails(reference, id)

        await(res) mustBe Left(DeleteSubscriptionDetailsHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR))
      }
    }
  }

}
