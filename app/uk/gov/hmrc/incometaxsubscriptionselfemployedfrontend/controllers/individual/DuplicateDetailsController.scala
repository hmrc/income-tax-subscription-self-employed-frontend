/*
 * Copyright 2025 HM Revenue & Customs
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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, SessionDataService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.individual.{DuplicateDetails => DuplicateDetailsView}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class DuplicateDetailsController @Inject()(mcc: MessagesControllerComponents,
                                           authService: AuthService,
                                           duplicateDetailsView: DuplicateDetailsView,
                                           sessionDataService: SessionDataService)
                                          (implicit ec: ExecutionContext) extends FrontendController(mcc) {

  def show(id: String, isEditMode: Boolean, isGlobalEdit: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      sessionDataService.getDuplicateDetails(id) map {
        case Some(duplicateDetails) if duplicateDetails.id == id =>
          Ok(duplicateDetailsView(
            id = id,
            trade = duplicateDetails.trade,
            name = duplicateDetails.name,
            isEditMode = isEditMode,
            isGlobalEdit = isGlobalEdit,
            backUrl = routes.FullIncomeSourceController.show(id, isEditMode, isGlobalEdit).url
          ))
        case _ =>
          Redirect(routes.FullIncomeSourceController.show(id, isEditMode, isGlobalEdit))
      }
    }
  }
}