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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent

import javax.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.businessesKey
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.IncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSelfEmploymentsHttpParser.{InvalidJson, UnexpectedStatusFailure}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.SelfEmploymentData
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.AuthService
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RemoveBusinessController @Inject()(authService: AuthService,
                                         incomeTaxSubscriptionConnector: IncomeTaxSubscriptionConnector,
                                         mcc: MessagesControllerComponents)
                                        (implicit val ec: ExecutionContext, val appConfig: AppConfig)
  extends FrontendController(mcc) with I18nSupport {

  def show(id: String): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      incomeTaxSubscriptionConnector.getSelfEmployments[Seq[SelfEmploymentData]](businessesKey).flatMap {
        case Right(Some(businesses)) if businesses.exists(_.isComplete) => {
          val updatedBusinesses: Seq[SelfEmploymentData] = businesses.filter(_.isComplete).filterNot(_.id == id)
          incomeTaxSubscriptionConnector.saveSelfEmployments(businessesKey, updatedBusinesses) map {
            case Right(_) => {
              if(updatedBusinesses.size == 0) Redirect(routes.InitialiseController.initialise())
              else Redirect(routes.BusinessListCYAController.show())
            }
            case Left(error) => throw new InternalServerException(
              s"[RemoveBusinessController][show] - saveSelfEmployments failure, error: ${error.toString}")
          }
        }
        case Right(_) => Future.successful(Redirect(routes.InitialiseController.initialise()))
        case Left(UnexpectedStatusFailure(status)) =>
          throw new InternalServerException(s"[RemoveBusinessController][show] - getSelfEmployments connection failure, status: $status")
        case Left(InvalidJson) =>
          throw new InternalServerException("[RemoveBusinessController][show] - Invalid Json")
      }
    }
  }

}
