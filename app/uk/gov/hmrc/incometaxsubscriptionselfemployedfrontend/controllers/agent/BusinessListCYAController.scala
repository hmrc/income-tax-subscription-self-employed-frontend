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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import play.twirl.api.Html
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.IncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetAllSelfEmployedDetailsHttpParser.{InvalidJson, UnexpectedStatusFailure}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{GetAllSelfEmploymentModel, SelfEmploymentData}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.AuthService
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.ImplicitDateFormatterImpl
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.check_your_answers
import uk.gov.hmrc.play.bootstrap.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessListCYAController @Inject()(authService: AuthService,
                                          incomeTaxSubscriptionConnector: IncomeTaxSubscriptionConnector,
                                          mcc: MessagesControllerComponents)
                                         (implicit val ec: ExecutionContext, val appConfig: AppConfig, dateFormatter: ImplicitDateFormatterImpl)
  extends FrontendController(mcc) with I18nSupport {

  def view(selfEmploymentData: GetAllSelfEmploymentModel)(implicit request: Request[AnyContent]): Html = {
    check_your_answers(
      selfEmploymentAnswers = selfEmploymentData,
      postAction = routes.BusinessListCYAController.submit(),
      implicitDateFormatter = dateFormatter
    )
  }

  def show: Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      incomeTaxSubscriptionConnector.getAllSelfEmployedDetails[GetAllSelfEmploymentModel]().map {
        case Right(Some(getAllSelfEmployment)) => Ok(view(getAllSelfEmployment))
        case Right(_) => Redirect(routes.DateOfCommencementController.show())
        case Left(UnexpectedStatusFailure(status)) =>
          throw new InternalServerException(s"[BusinessListCYAController][show] - getAllSelfEmployedDetails connection failure, status: $status")
        case Left(InvalidJson) =>
          throw new InternalServerException("[BusinessListCYAController][show] - Invalid Json")
      }
    }
  }

  def submit(): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      Future.successful(Redirect(routes.BusinessListCYAController.show()))
    }
  }
}
