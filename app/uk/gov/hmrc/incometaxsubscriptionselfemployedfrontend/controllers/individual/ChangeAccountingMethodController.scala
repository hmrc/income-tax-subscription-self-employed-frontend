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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.individual

import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import play.twirl.api.Html
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.AuthService
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.ChangeAccountingMethod
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ChangeAccountingMethodController @Inject()(changeAccountingMethod: ChangeAccountingMethod,
                                                 mcc: MessagesControllerComponents,
                                                 authService: AuthService)
                                                (implicit val ec: ExecutionContext, val appConfig: AppConfig)
  extends FrontendController(mcc) with I18nSupport {

  def view(id: String)
          (implicit request: Request[AnyContent]): Html =
    changeAccountingMethod(
      postAction = routes.ChangeAccountingMethodController.submit(id),
      backUrl = backUrl(id)
    )

  def show(id: String): Action[AnyContent] = Action.async { implicit request =>
    request.lang
    authService.authorised() {
      Future.successful(Ok(view(id)))
    }
  }

  def submit(id: String): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      Future.successful(Redirect(routes.BusinessAccountingMethodController.show(id, isEditMode = true)))
    }
  }

  def backUrl(id: String): String = routes.SelfEmployedCYAController.show(id, isEditMode = true).url

}
