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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.data.FormError
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.AddAnotherBusinessAgentForm._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.submapping.YesNoMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.validation.testutils.DataMap.DataMap
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.validation.testutils._

class AddAnotherBusinessAgentFormSpec extends PlaySpec with GuiceOneAppPerTest {


  "The AddAnotherBusinessForm" should {

    val empty = "agent.error.add_another_business.empty"
    val invalid = "agent.error.add_another_business.invalid"
    val limitReached = "agent.error.add_another_business.limit_reached"

    "validate add another business Yes/No correctly" when {

      val limit = 50
      val currentBusinesses = 3

      "the map is empty" in {
        val emptyInput0 = DataMap.EmptyMap
        val emptyTest0 = addAnotherBusinessForm(currentBusinesses, limit).bind(emptyInput0)
        emptyTest0.errors must contain(FormError(addAnotherBusiness, empty))
      }

      "the input is empty" in {
        val emptyInput = DataMap.addAnotherBusiness("")
        val emptyTest = addAnotherBusinessForm(currentBusinesses, limit).bind(emptyInput)
        emptyTest.errors must contain(FormError(addAnotherBusiness, empty))
      }

      "the input is invalid" in {
        val invalidInput = DataMap.addAnotherBusiness("α")
        val invalidTest = addAnotherBusinessForm(currentBusinesses, limit).bind(invalidInput)
        invalidTest.errors must contain(FormError(addAnotherBusiness, invalid))
      }

      "The following submission should be valid" in {
        val testYes = DataMap.addAnotherBusiness(YesNoMapping.option_yes)
        addAnotherBusinessForm(currentBusinesses, limit) isValidFor testYes
        val testNo = DataMap.addAnotherBusiness(YesNoMapping.option_no)
        addAnotherBusinessForm(currentBusinesses, limit) isValidFor testNo
      }
    }

    "validate add another business limit correctly" when {
      "they have reach the business limit" in {
        val limit = 50
        val currentBusinesses = 51

          val withinLimitInput = DataMap.addAnotherBusiness(YesNoMapping.option_yes)
          val withinLimitTest = addAnotherBusinessForm(limit, currentBusinesses).bind(withinLimitInput)
          withinLimitTest.value mustNot contain(limitReached)
        }

    }
  }
}
