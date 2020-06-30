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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers

import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import play.twirl.api.Html
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.IncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.BusinessNameForm._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.AuthService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessNameController @Inject()(mcc: MessagesControllerComponents, incomeTaxSubscriptionConnector: IncomeTaxSubscriptionConnector,
                                       authService: AuthService)
                                      (implicit val ec: ExecutionContext, val appConfig: AppConfig) extends FrontendController(mcc) with I18nSupport {

  implicit class FormUtil[T](form: Form[T]) {
    def fill(data: Option[T]): Form[T] = data.fold(form)(form.fill)
  }

  def view(businessNameForm: Form[BusinessNameModel])(implicit request: Request[AnyContent]): Html =
    uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.business_name(
      businessNameForm = businessNameForm,
      postAction = uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.BusinessNameController.submit(),
      backUrl = backUrl()
    )

  def show(): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised(){
      incomeTaxSubscriptionConnector.getSelfEmployments[BusinessNameModel](BusinessNameController.businessNameKey).map{
        case Right(businessNameData) =>
          Ok(view(businessNameValidationForm.fill(businessNameData)))
        case error  => throw new InternalServerException(error.toString)
      }}
  }

  def submit(): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised(){
      businessNameValidationForm.bindFromRequest.fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors))),
        businessNameData =>
          incomeTaxSubscriptionConnector.saveSelfEmployments[BusinessNameModel](BusinessNameController.businessNameKey,businessNameData) map (_ =>
            Redirect(uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.BusinessTradeNameController.show()))
      )
    }}

  def backUrl(): String =
    uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.BusinessStartDateController.show().url
}

object BusinessNameController{
  val businessNameKey: String = "BusinessName"
}
