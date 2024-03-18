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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitch.EnableTaskListRedesign
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.AuthService
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.UUIDGenerator
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class InitialiseController @Inject()(mcc: MessagesControllerComponents,
                                     authService: AuthService,
                                     uuidGen: UUIDGenerator)
                                    (val appConfig: AppConfig)
                                    (implicit val ec: ExecutionContext)
  extends FrontendController(mcc) with FeatureSwitching {

  val initialise: Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      val id = uuidGen.generateId
      if (isEnabled(EnableTaskListRedesign)) {
        Future.successful(Redirect(routes.BusinessNameConfirmationController.show(id)))
      } else {
        Future.successful(Redirect(routes.BusinessNameController.show(id)))
      }
    }
  }

}
