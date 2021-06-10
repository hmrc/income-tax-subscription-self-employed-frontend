/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent

import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.AuthService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SignOutController @Inject()(mcc: MessagesControllerComponents,
                                  appConfig: AppConfig,
                                  authService: AuthService)
                                 (implicit ec: ExecutionContext) extends FrontendController(mcc) {

  def signOut: Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      Future.successful(Redirect(appConfig.ggSignOutUrl(appConfig.feedbackFrontendRedirectUrl + "_A")))
    }
  }

}

object SignOutController {

  def agentSignOut: Call = routes.SignOutController.signOut()

}