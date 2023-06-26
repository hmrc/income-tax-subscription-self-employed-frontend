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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers

import _root_.uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.UUIDGenerator
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitch.EnableTaskListRedesign
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.AuthService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class InitialiseController @Inject()(mcc: MessagesControllerComponents,
                                     authService:
                                     AuthService,
                                     uuidGen: UUIDGenerator)
                                    (implicit val ec: ExecutionContext,
                                     val appConfig: AppConfig) extends FrontendController(mcc) with FeatureSwitching {

  def initialise: Action[AnyContent] = Action.async { implicit request =>
    val id = uuidGen.generateId

    authService.authorised() {
      Future.successful(Redirect(redirectLocation(id)))
    }
  }

  def redirectLocation(id: String): Call = if (isEnabled(EnableTaskListRedesign)) {
    controllers.individual.routes.BusinessNameConfirmationController.show(id)
  } else {
    controllers.routes.BusinessNameController.show(id)
  }
}
