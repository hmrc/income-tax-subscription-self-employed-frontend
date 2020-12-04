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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.validation.testutils

import play.api.data.validation._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.formatters.DateModelMapping
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.AddAnotherBusinessForm.addAnotherBusinessForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessTradeNameForm._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.{AddAnotherBusinessForm, BusinessAccountingMethodForm}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.ConstraintUtil._

object DataMap {


  object DataMap {

    type DataMap = Map[String, String]

    val EmptyMap: DataMap = DataMap()

    def DataMap(elems: (String, String)*): DataMap = Map(elems: _*)

    def date(prefix: String)(day: String, month: String, year: String): DataMap =
      Map(s"$prefix.${DateModelMapping.day}" -> day, s"$prefix.${DateModelMapping.month}" -> month, s"$prefix.${DateModelMapping.year}" -> year)

    val emptyDate: String => DataMap = (prefix: String) => date(prefix)("", "", "")

    val alwaysFailInvalid: Invalid = Invalid("always fail")

    def addAnotherBusiness(value: String): DataMap = Map(AddAnotherBusinessForm.addAnotherBusiness -> value)

    def businessTradeNameMap(name: String): DataMap = Map(businessTradeName -> name)

    def businessAccountingMethod(accountingMethod: String): DataMap = Map(
      BusinessAccountingMethodForm.businessAccountingMethod -> accountingMethod
    )

    def alwaysFail[T]: Constraint[T] = constraint[T]((t: T) => alwaysFailInvalid)

  }

}
