/*
 * Copyright 2024 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models

import play.api.mvc._

import scala.util.matching.Regex

object ClientDetails{
  implicit class ClientInfoRequestUtil(request: Request[AnyContent]) {
    def getClientDetails: ClientDetails = {
      val clientName: String = Seq(request.session.get("FirstName"), request.session.get("LastName")).flatten.mkString(" ")
      val clientNino: String = request.session.get("NINO").mkString("")

      ClientDetails(clientName, clientNino)
    }
  }

}

  case class ClientDetails(name: String, nino: String){
    private val ninoRegex: Regex = """^([a-zA-Z]{2})\s*(\d{2})\s*(\d{2})\s*(\d{2})\s*([a-zA-Z])$""".r

    val formattedNino: String = nino match {
      case ninoRegex(startLetters, firstDigits, secondDigits, thirdDigits, finalLetter) =>
        s"$startLetters $firstDigits $secondDigits $thirdDigits $finalLetter"
      case other => other
    }
  }



