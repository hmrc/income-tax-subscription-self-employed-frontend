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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config

import org.scalatestplus.play.PlaySpec
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.helpers.MessagesMatcher

import scala.io.Source

class MessagesSpec extends PlaySpec with MessagesMatcher {

  override val excludedKeys: Set[String] = Set.empty
  private val messageLinesEnglish: List[String] = getMessageLines("messages").toList
  private val messageKeysEnglish: List[String] = getMessageKeys("messages").toList
  private lazy val messageKeySetEnglish = messageKeysEnglish.toSet

  private val messageLinesWelsh: List[String] = getMessageLines("messages.cy").toList
  private val messageKeysWelsh: List[String] = getMessageKeys("messages.cy").toList
  private lazy val messageKeySetWelsh = messageKeysWelsh.toSet

  "Messages present in Welsh (conf/messages.cy)" should {
    "also have an English translation (conf/messages)" in {
      messageKeySetWelsh must allBeIn(messageKeySetEnglish)
    }

    "not contain duplicate keys" in {
      messageKeysWelsh must containUniqueKeys
    }

    "contain only permitted characters" in {
      messageKeysWelsh must containOnlyPermittedCharacters
    }

    //TODO: not quite working right, we need to show single quotes in our error message, find way to detect non-escaped single quotes
//    "not contain single quotes" in {
//      messageLinesWelsh must containNoSingleQuotes
//    }
  }

  "Messages present in English (conf/messages)" should {
    "also have a Welsh translation (conf/messages.cy)" in {
      messageKeySetEnglish must allBeIn(messageKeySetWelsh)
    }

    "not contain duplicate keys" in {
      messageKeysEnglish must containUniqueKeys
    }

    "contain only permitted characters" in {
      messageKeysEnglish must containOnlyPermittedCharacters
    }
    //TODO: not quite working right, we need to show single quotes in our error message, find way to detect non-escaped single quotes
//    "not contain single quotes" in {
//      messageLinesEnglish must containNoSingleQuotes
//    }
  }

  private def getMessageKeys(fileName: String) = {
    Source.fromResource(fileName)
      .getLines()
      .map(_.trim)
      .filter(!_.startsWith("#"))
      .filter(_.nonEmpty)
      .map(_.split(' ').head)
  }

  private def getMessageLines(fileName: String) = {
    Source.fromResource(fileName)
      .getLines()
      .map(_.trim)
      .filterNot(_.startsWith("#"))
      .filter(_.nonEmpty)
  }
}

