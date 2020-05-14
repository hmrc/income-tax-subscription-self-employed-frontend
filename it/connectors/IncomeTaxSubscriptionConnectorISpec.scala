
package connectors

import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import connectors.stubs.IncomeTaxSubscriptionConnectorStub._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.IncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetAllSelfEmploymentsHttpParser.{GetAllSelfEmploymentConnectionFailure, GetAllSelfEmploymentConnectionSuccess, GetAllSelfEmploymentDataModel}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.{GetSelfEmploymentsData, GetSelfEmploymentsEmpty, InvalidJson, UnexpectedStatusFailure}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.{PostSelfEmploymentsSuccessResponse, UnexpectedStatusFailure}
import play.api.libs.json.{JsObject, JsValue, Json, OFormat}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.{GetAllSelfEmploymentsHttpParser, GetSelfEmploymentsHttpParser, PostSelfEmploymentsHttpParser}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.GetAllSelfEmploymentModel




class IncomeTaxSubscriptionConnectorISpec extends ComponentSpecBase {

  lazy val connector: IncomeTaxSubscriptionConnector = app.injector.instanceOf[IncomeTaxSubscriptionConnector]
  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  case class DummyModel(body: String)
  object DummyModel{
    implicit val format: OFormat[DummyModel] = Json.format[DummyModel]
  }

  "GetSelfEmployments" should {
    "Return GetSelfEmploymentsData" in {
      val successfulResponseBody: JsObject = Json.obj("body" -> "Test Body"
      )

      stubGetSelfEmployments(id)(OK, successfulResponseBody)

      val res = connector.getSelfEmployments[DummyModel](id)

      await(res) mustBe Right(GetSelfEmploymentsData(DummyModel(body = "Test Body")))
    }

    "Return InvalidJson" in {
      stubGetSelfEmployments(id)(OK, Json.obj())

      val res = connector.getSelfEmployments[DummyModel](id)

      await(res) mustBe Left(GetSelfEmploymentsHttpParser.InvalidJson)
    }

    "Return GetSelfEmploymentsEmpty" in {
      stubGetSelfEmployments(id)(NO_CONTENT, Json.obj())

      val res = connector.getSelfEmployments[DummyModel](id)

      await(res) mustBe Right(GetSelfEmploymentsEmpty)

    }
    "Return UnexpectedStatusFailure" in {
      stubGetSelfEmployments(id)(INTERNAL_SERVER_ERROR, Json.obj())

      val res = connector.getSelfEmployments[DummyModel](id)

      await(res) mustBe Left(GetSelfEmploymentsHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR))

    }
  }

  "GetAllSelfEmployments" should {
    "Return GetAllSelfEmploymentDataModel" in {
      val successfulResponseBody: JsObject = Json.obj("BusinessName" -> "Business Name",
        "Address" -> "Address"
      )
      stubGetAllSelfEmployments(OK, successfulResponseBody)

      val res = connector.getAllSelfEmployments

      await(res) mustBe Right(GetAllSelfEmploymentDataModel(GetAllSelfEmploymentModel(businessName = "Business Name", address = "Address")))
    }

    "Return InvalidJson" in {
      stubGetAllSelfEmployments(OK, Json.obj())

      val res = connector.getAllSelfEmployments

      await(res) mustBe Left(GetAllSelfEmploymentsHttpParser.InvalidJson)
    }

    "Return GetAllSelfEmploymentConnectionSuccess" in {
      stubGetAllSelfEmployments(NO_CONTENT, Json.obj())

      val res = connector.getAllSelfEmployments

      await(res) mustBe Right(GetAllSelfEmploymentConnectionSuccess)
    }

    "Return GetAllSelfEmploymentConnectionFailure" in {
      stubGetAllSelfEmployments(INTERNAL_SERVER_ERROR, Json.obj())

      val res = connector.getAllSelfEmployments

      await(res) mustBe Left(GetAllSelfEmploymentConnectionFailure(INTERNAL_SERVER_ERROR))
    }

  }

  "SaveSelfEmployments" should {
    "Return PostSelfEmploymentsSuccessResponse" in {
      val dummyModel: DummyModel = DummyModel(body = "Test Body")
      stubSaveSelfEmployments(id)(OK,responseBody = Json.toJson(dummyModel))

      val res = connector.saveSelfEmployments[DummyModel](id,dummyModel)

      await(res) mustBe Right(PostSelfEmploymentsSuccessResponse)
    }

    "Return UnexpectedStatusFailure(status)" in {
      val dummyModel: DummyModel = DummyModel(body = "Test Body")
      stubSaveSelfEmployments(id)(INTERNAL_SERVER_ERROR,responseBody = Json.toJson(dummyModel))

      val res = connector.saveSelfEmployments[DummyModel](id,dummyModel)

      await(res) mustBe Left(PostSelfEmploymentsHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR))
    }
  }





}
