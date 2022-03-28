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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config

import org.scalatest.funsuite.AnyFunSuiteLike
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper

import scala.io.Source

class MessagesSpec extends AnyFunSuiteLike with UrlHelpers {

  lazy val messageKeysEnglish: Set[String] = getMessageKeys("messages")
  lazy val messageKeysWelsh: Set[String] = getMessageKeys("messages.cy")

  test("No messages present in Welsh, but not in English") {
    val keysInWelshNotEnglish = messageKeysWelsh -- messageKeysEnglish
    keysInWelshNotEnglish foreach println
    keysInWelshNotEnglish.size shouldBe 0
  }

  test("No messages present in English, but not in Welsh") {
    val keysInEnglishNotWelsh = messageKeysEnglish -- messageKeysWelsh
    keysInEnglishNotWelsh foreach println
    keysInEnglishNotWelsh.size shouldBe 0
  }

  private def getMessageKeys(fileName: String) = {
    Source.fromResource(fileName)
      .getLines
      .map(_.trim)
      .filter(!_.startsWith("#"))
      .filter(_.nonEmpty)
      .map(_.split(' ').head)
      .toSet
  }
}
