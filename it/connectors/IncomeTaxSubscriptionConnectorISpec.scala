
package connectors

import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import play.api.libs.json.{JsObject, Json, OFormat}
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.IncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetAllSelfEmploymentsHttpParser.{GetAllSelfEmploymentConnectionFailure, GetAllSelfEmploymentDataModel}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.PostSelfEmploymentsHttpParser.PostSelfEmploymentsSuccessResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.{GetAllSelfEmploymentsHttpParser, GetSelfEmploymentsHttpParser, PostSelfEmploymentsHttpParser}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.GetAllSelfEmploymentModel
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.GetAllSelfEmploymentModel._
import connectors.stubs.IncomeTaxSubscriptionConnectorStub._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.BusinessStartDateController


class IncomeTaxSubscriptionConnectorISpec extends ComponentSpecBase {

  lazy val connector: IncomeTaxSubscriptionConnector = app.injector.instanceOf[IncomeTaxSubscriptionConnector]
  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  case class DummyModel(body: String)
  object DummyModel{
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

  "GetAllSelfEmployments" should {
    "Return GetAllSelfEmploymentDataModel" in {
      val model = GetAllSelfEmploymentModel(businessStartDate = testValidBusinessStartDateModel,businessName = testBusinessNameModel)
      val successfulResponseBody: JsObject = Json.toJsObject(model)

      stubGetAllSelfEmployments(OK, successfulResponseBody)

      val res = connector.getAllSelfEmployments

      await(res) mustBe Right(Some(GetAllSelfEmploymentDataModel(GetAllSelfEmploymentModel(
        businessStartDate = testValidBusinessStartDateModel,businessName = testBusinessNameModel))))
    }

    "Return InvalidJson" in {
      stubGetAllSelfEmployments(OK, Json.obj())

      val res = connector.getAllSelfEmployments

      await(res) mustBe Left(GetAllSelfEmploymentsHttpParser.InvalidJson)
    }

    "Return None" in {
      stubGetAllSelfEmployments(NO_CONTENT, Json.obj())

      val res = connector.getAllSelfEmployments

      await(res) mustBe Right(None)
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
      stubSaveSelfEmployments(id, body = Json.toJson(dummyModel))(OK)

      val res = connector.saveSelfEmployments[DummyModel](id,dummyModel)

      await(res) mustBe Right(PostSelfEmploymentsSuccessResponse)
    }

    "Return UnexpectedStatusFailure(status)" in {
      val dummyModel: DummyModel = DummyModel(body = "Test Body")
      stubSaveSelfEmployments(id, body = Json.toJson(dummyModel))(INTERNAL_SERVER_ERROR)

      val res = connector.saveSelfEmployments[DummyModel](id,dummyModel)

      await(res) mustBe Left(PostSelfEmploymentsHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR))
    }
  }





}
