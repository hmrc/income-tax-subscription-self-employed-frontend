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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent

import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.submapping.YesNoMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.validation.utils.ConstraintUtil.constraint
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{AddAnotherBusinessModel, No, Yes, YesNo}

object AddAnotherBusinessForm {

  val addAnotherBusiness = "yes-no"

  def maxBusinessesValidation(currentBusinesses: Int, limit: Int): Constraint[YesNo] = constraint[YesNo] {
    case Yes => if (currentBusinesses >= limit) Invalid(s"agent.error.add_another_business.limit_reached", limit) else Valid
    case No => Valid
  }

  def addAnotherBusinessForm(currentBusinesses: Int, limit: Int): Form[AddAnotherBusinessModel] = Form(
    mapping(
      addAnotherBusiness -> YesNoMapping.yesNoMapping(
        yesNoInvalid = Invalid("agent.error.add_another_business.invalid"),
        yesNoEmpty = Some(Invalid("agent.error.add_another_business.empty"))
      ).verifying(maxBusinessesValidation(currentBusinesses, limit))
    )(AddAnotherBusinessModel.apply)(AddAnotherBusinessModel.unapply)
  )

}
