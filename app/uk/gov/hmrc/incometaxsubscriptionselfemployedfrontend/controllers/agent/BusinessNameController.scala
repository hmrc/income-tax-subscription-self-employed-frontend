/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.twirl.api.Html
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.IncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.utils.ReferenceRetrieval
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.BusinessNameForm._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.FormUtil._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, MultipleSelfEmploymentsService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.BusinessName
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessNameController @Inject()(mcc: MessagesControllerComponents,
                                       multipleSelfEmploymentsService: MultipleSelfEmploymentsService,
                                       val incomeTaxSubscriptionConnector: IncomeTaxSubscriptionConnector,
                                       authService: AuthService,
                                       businessName: BusinessName)
                                      (implicit val ec: ExecutionContext, val appConfig: AppConfig)
  extends FrontendController(mcc) with ReferenceRetrieval with I18nSupport {

  def view(businessNameForm: Form[BusinessNameModel], id: String, isEditMode: Boolean)(implicit request: Request[AnyContent]): Html =
    businessName(
      businessNameForm = businessNameForm,
      postAction = routes.BusinessNameController.submit(id, isEditMode = isEditMode),
      isEditMode,
      backUrl = backUrl(id, isEditMode)
    )

  def show(id: String, isEditMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withReference { reference =>
        withAllBusinesses(reference) { businesses =>
          val excludedBusinessNames = getExcludedBusinessNames(id, businesses)
          val currentBusinessName = businesses.find(_.id == id).flatMap(_.businessName)
          Future.successful(Ok(
            view(businessNameValidationForm(excludedBusinessNames).fill(currentBusinessName), id, isEditMode = isEditMode)
          ))
        }
      }
    }
  }

  def submit(id: String, isEditMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withReference { reference =>
        withAllBusinesses(reference) { businesses =>
          val excludedBusinessNames = getExcludedBusinessNames(id, businesses)
          businessNameValidationForm(excludedBusinessNames).bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, id, isEditMode = isEditMode))),
            businessNameData =>
              multipleSelfEmploymentsService.saveBusinessName(reference, id, businessNameData) map {
                case Right(_) =>
                  next(id, isEditMode)
                case Left(_) =>
                  throw new InternalServerException("[BusinessNameController][submit] - Could not save business name")
              }
          )
        }
      }
    }
  }

  private def next(id: String, isEditMode: Boolean) = Redirect(if (isEditMode) {
    routes.SelfEmployedCYAController.show(id, isEditMode = isEditMode)
  } else {
    routes.BusinessStartDateController.show(id)
  })

  private def getExcludedBusinessNames(id: String, businesses: Seq[SelfEmploymentData]): Seq[BusinessNameModel] = {
    val currentBusinessTrade = businesses.find(_.id == id).flatMap(_.businessTradeName)
    businesses.filterNot(_.id == id).filter {
      case SelfEmploymentData(_, _, _, _, Some(trade), _) if currentBusinessTrade contains trade => true
      case _ => false
    }.flatMap(_.businessName)
  }

  private def withAllBusinesses(reference: String)(f: Seq[SelfEmploymentData] => Future[Result])(implicit hc: HeaderCarrier): Future[Result] = {
    multipleSelfEmploymentsService.fetchAllBusinesses(reference).flatMap {
      case Right(businesses) => f(businesses)
      case Left(error) => throw new InternalServerException(s"[BusinessNameController][withAllBusinesses] - Error retrieving businesses, error: $error")
    }
  }

  def backUrl(id: String, isEditMode: Boolean): String = if (isEditMode) {
    routes.SelfEmployedCYAController.show(id, isEditMode = isEditMode).url
  } else {
    appConfig.clientWhatIncomeSourceToSignUpUrl
  }

}
