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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual

import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.data.validation.Invalid
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.submapping.AccountingMethodMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.AccountingMethodModel

object BusinessAccountingMethodForm {

  val businessAccountingMethod = "businessAccountingMethod"

  val businessAccountingMethodForm: Form[AccountingMethodModel] = Form(
    mapping(
      businessAccountingMethod -> AccountingMethodMapping(
        errInvalid = Invalid("error.business_accounting_method.invalid"),
        errEmpty = Some(Invalid("error.business_accounting_method.empty"))
      )
    )(AccountingMethodModel.apply)(AccountingMethodModel.unapply)
  )

}
