/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors

import org.scalatest.EitherValues
import play.api.http.Status.OK
import play.api.libs.json._
import play.api.test.Helpers._
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetAllSelfEmploymentsHttpParser.{GetAllSelfEmploymentConnectionFailure, GetAllSelfEmploymentDataModel, GetAllSelfEmploymentHttpReads, InvalidJson}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.TestModels._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.UnitTestTrait


class GetAllSelfEmploymentsHttpParserSpec extends UnitTestTrait with EitherValues{



  val testHttpVerb = "GET"
  val testUri = "/"

  "GetAllSelfEmploymentHttpReads" when {
    "read" should {
      "parse a correctly formatted OK response as a GetAllSelfEmploymentsDataModel" in {
        val httpResponse = HttpResponse(OK, Some(testGetAllSelfEmploymentModelJsValue))

        val res = GetAllSelfEmploymentHttpReads.read(testHttpVerb, testUri, httpResponse)


        res mustBe Right(Some(GetAllSelfEmploymentDataModel(testGetAllSelfEmploymentModel)))
      }
      "parse an incorrectly formatted Ok response as an invalid Json" in {
        val httpResponse = HttpResponse(OK, Some(Json.obj()))

        val res = GetAllSelfEmploymentHttpReads.read(testHttpVerb, testUri, httpResponse)


        res mustBe Left(InvalidJson)
      }
      "parse an no content response as None" in {
        val httpResponse = HttpResponse(NO_CONTENT)

        val res = GetAllSelfEmploymentHttpReads.read(testHttpVerb, testUri, httpResponse)


        res mustBe Right(None)
      }

      "parse any other http status as a GetAllSelfEmploymentConnectionFailure" in {
        val httpResponse = HttpResponse(INTERNAL_SERVER_ERROR)

        val res = GetAllSelfEmploymentHttpReads.read(testHttpVerb, testUri, httpResponse)


        res mustBe Left(GetAllSelfEmploymentConnectionFailure(INTERNAL_SERVER_ERROR))
      }
    }

  }
}
