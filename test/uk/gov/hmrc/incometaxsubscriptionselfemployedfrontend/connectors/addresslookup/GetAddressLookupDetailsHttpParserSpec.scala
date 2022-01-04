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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.addresslookup

import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.UnitTestTrait
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.addresslookup.GetAddressLookupDetailsHttpParser._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{Address, BusinessAddressModel}

class GetAddressLookupDetailsHttpParserSpec extends UnitTestTrait {


  val testHttpVerb = "GET"
  val testUri = "/"

  val testValidJson: JsObject = Json.obj("auditRef"-> "1",
    "address" -> Json.obj("lines" -> Seq("line1", "line2", "line3"), "postcode" -> "TF3 4NT"))

  "GetAddressLookupDetailsHttpReads" when {
    "read" should {
      "parse a correctly formatted OK response and return the data in a model" in {
        val httpResponse = HttpResponse(OK, json = testValidJson, headers = Map.empty)

        lazy val res = getAddressLookupDetailsHttpReads.read(testHttpVerb, testUri, httpResponse)

        res mustBe Right(Some(BusinessAddressModel(auditRef = "1",
          Address(lines = Seq("line1", "line2", "line3"), postcode = "TF3 4NT"))))
      }
      "parse an incorrectly formatted Ok response as an invalid Json" in {
        val httpResponse = HttpResponse(OK, json = Json.obj(), headers = Map.empty)

        lazy val res = getAddressLookupDetailsHttpReads.read(testHttpVerb, testUri, httpResponse)

        res mustBe Left(InvalidJson)
      }

      "parse an 404 NOT_FOUND response as None" in {
        val httpResponse = HttpResponse(NOT_FOUND, body = "")

        lazy val res = getAddressLookupDetailsHttpReads.read(testHttpVerb, testUri, httpResponse)

        res mustBe Right(None)
      }

      "parse any other http status as a UnexpectedStatusFailure" in {
        val httpResponse = HttpResponse(INTERNAL_SERVER_ERROR, body = "")

        lazy val res = getAddressLookupDetailsHttpReads.read(testHttpVerb, testUri, httpResponse)

        res mustBe Left(UnexpectedStatusFailure(INTERNAL_SERVER_ERROR))

      }
    }

  }
}
