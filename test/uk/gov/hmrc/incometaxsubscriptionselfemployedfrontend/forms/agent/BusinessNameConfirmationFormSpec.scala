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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent

import org.scalatestplus.play.PlaySpec
import play.api.data.FormError
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.BusinessNameConfirmationForm._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.submapping.YesNoMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{No, Yes}

class BusinessNameConfirmationFormSpec extends PlaySpec {

  "BusinessNameConfirmationForm" must {
    "bind successfully into Yes" when {
      "bound with the correct form key and a yes value" in {
        val boundForm = businessNameConfirmationForm.bind(Map(fieldName -> YesNoMapping.option_yes))
        boundForm.hasErrors mustBe false
        boundForm.value mustBe Some(Yes)
      }
    }
    "bind successfully into No" when {
      "bound with the correct form key and a no value" in {
        val boundForm = businessNameConfirmationForm.bind(Map(fieldName -> YesNoMapping.option_no))
        boundForm.hasErrors mustBe false
        boundForm.value mustBe Some(No)
      }
    }

    "produce a form error" when {
      "there was no data provided to the binding form" in {
        val emptyForm = businessNameConfirmationForm.bind(Map.empty[String, String])
        emptyForm.value mustBe None
        emptyForm.errors.headOption mustBe Some(FormError(fieldName, "error.agent.business-name-confirmation.empty"))
      }
      "there was an invalid value provided to the binding form" in {
        val invalidForm = businessNameConfirmationForm.bind(Map(fieldName -> "invalid"))
        invalidForm.value mustBe None
        invalidForm.errors.headOption mustBe Some(FormError(fieldName, "error.agent.business-name-confirmation.empty"))
      }
    }
  }

}