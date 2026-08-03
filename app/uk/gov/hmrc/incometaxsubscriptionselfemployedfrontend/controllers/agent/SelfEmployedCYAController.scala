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

import play.api.mvc._
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{SelfEmploymentsCYAModel, SoleTraderBusiness}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.MultipleSelfEmploymentsService
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.SelfEmployedCYA
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.actions.IdentifierAction
import play.api.i18n.I18nSupport

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SelfEmployedCYAController @Inject()(checkYourAnswersView: SelfEmployedCYA,
                                          multipleSelfEmploymentsService: MultipleSelfEmploymentsService,
                                          mcc: MessagesControllerComponents)
                                         (identify: IdentifierAction,
                                          val appConfig: AppConfig)
                                         (implicit val ec: ExecutionContext)
  extends FrontendController(mcc) with FeatureSwitching with I18nSupport {

  def show(id: String, isEditMode: Boolean, isGlobalEdit: Boolean): Action[AnyContent] = identify.async { implicit request =>
    withSelfEmploymentCYAModel(request.reference, id) { selfEmploymentCYAModel =>
      Future.successful(Ok(checkYourAnswersView(
        answers = selfEmploymentCYAModel,
        postAction = routes.SelfEmployedCYAController.submit(id, isGlobalEdit),
        clientDetails = request.clientDetails,
        isGlobalEdit = isGlobalEdit
      )))
    }
  }

  def submit(id: String, isGlobalEdit: Boolean): Action[AnyContent] = identify.async { implicit request =>
    withSelfEmploymentCYAModel(request.reference, id) { selfEmploymentCYAModel =>
      if (selfEmploymentCYAModel.isComplete) {
        multipleSelfEmploymentsService.confirmBusiness(request.reference, id) map {
          case Right(_) =>
            if (isGlobalEdit) Redirect(appConfig.globalCYAUrl)
            else Redirect(appConfig.clientYourIncomeSourcesUrl)
          case Left(_) =>
            throw new InternalServerException("[SelfEmployedCYAController][submit] - Could not confirm self employment business")
        }
      } else {
        Future.successful(Redirect(appConfig.clientYourIncomeSourcesUrl))
      }
    }
  }

  private def withSelfEmploymentCYAModel(reference: String, id: String)(f: SelfEmploymentsCYAModel => Future[Result])
                                        (implicit hc: HeaderCarrier): Future[Result] =
    for {
      businesses <- fetchBusinessList(reference)
      business = businesses.find(_.id == id)
      result <- f(SelfEmploymentsCYAModel(id, business))
    } yield result

  private def fetchBusinessList(reference: String)(implicit hc: HeaderCarrier) = {
    multipleSelfEmploymentsService.fetchSoleTraderBusinesses(reference)
      .map(_.getOrElse(throw new FetchSoleTraderBusinessesException))
      .map {
        case Some(soleTraderBusinesses) => soleTraderBusinesses.businesses
        case None => Seq.empty[SoleTraderBusiness]
      }
  }

  private class FetchSoleTraderBusinessesException extends InternalServerException(
    "[SelfEmployedCYAController][fetchBusinessList] - Failed to retrieve sole trader businesses"
  )

}
