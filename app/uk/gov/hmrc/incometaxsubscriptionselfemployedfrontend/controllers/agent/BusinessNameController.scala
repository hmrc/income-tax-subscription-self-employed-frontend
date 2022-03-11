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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitch.SaveAndRetrieve
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.IncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.utils.ReferenceRetrieval
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.BusinessNameForm._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.FormUtil._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, MultipleSelfEmploymentsService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.agent.BusinessName

@Singleton
class BusinessNameController @Inject()(mcc: MessagesControllerComponents,
                                       multipleSelfEmploymentsService: MultipleSelfEmploymentsService,
                                       val incomeTaxSubscriptionConnector: IncomeTaxSubscriptionConnector,
                                       authService: AuthService,
                                       businessName: BusinessName)
                                      (implicit val ec: ExecutionContext, val appConfig: AppConfig)
  extends FrontendController(mcc) with I18nSupport with FeatureSwitching with ReferenceRetrieval {

  def view(businessNameForm: Form[BusinessNameModel], id: String, isEditMode: Boolean, isSaveAndRetrieve: Boolean)(implicit request: Request[AnyContent]): Html =
    businessName(
      businessNameForm = businessNameForm,
      postAction = routes.BusinessNameController.submit(id, isEditMode = isEditMode),
      isEditMode,
      backUrl = backUrl(id, isEditMode),
      isSaveAndRetrieve = isSaveAndRetrieve
    )

  def show(id: String, isEditMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withReference { reference =>
        withAllBusinesses(reference) { businesses =>
          val excludedBusinessNames = getExcludedBusinessNames(id, businesses)
          val currentBusinessName = businesses.find(_.id == id).flatMap(_.businessName)
          Future.successful(Ok(
            view(businessNameValidationForm(excludedBusinessNames).fill(currentBusinessName), id, isEditMode = isEditMode, isSaveAndRetrieve = isSaveAndRetrieve)
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
          businessNameValidationForm(excludedBusinessNames).bindFromRequest.fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, id, isEditMode = isEditMode, isSaveAndRetrieve = isSaveAndRetrieve))),
            businessNameData =>
              multipleSelfEmploymentsService.saveBusinessName(reference, id, businessNameData).map(_ =>
                next(id, isEditMode)
              )
          )
        }
      }
    }
  }

  //save & retrieve on should have an order of: business name (this) -> business start date -> business trade
  //save & retrieve off should have an order of: business start date -> business name (this) -> business trade
  private def next(id: String, isEditMode: Boolean) = Redirect((isEditMode, isSaveAndRetrieve) match {
    case (true, true) => routes.SelfEmployedCYAController.show(id)
    case (false, true) => routes.BusinessStartDateController.show(id)
    case (true, false) => routes.BusinessListCYAController.show
    case (false, false) => routes.BusinessTradeNameController.show(id)
  })

  private def getExcludedBusinessNames(id: String, businesses: Seq[SelfEmploymentData]): Seq[BusinessNameModel] = {
    val currentBusinessTrade = businesses.find(_.id == id).flatMap(_.businessTradeName)
    businesses.filterNot(_.id == id).filter {
      case SelfEmploymentData(_, _, _, _, Some(trade), _, _) if currentBusinessTrade contains trade => true
      case _ => false
    }.flatMap(_.businessName)
  }

  private def withAllBusinesses(reference: String)(f: Seq[SelfEmploymentData] => Future[Result])(implicit hc: HeaderCarrier): Future[Result] = {
    multipleSelfEmploymentsService.fetchAllBusinesses(reference).flatMap {
      case Right(businesses) => f(businesses)
      case Left(error) => throw new InternalServerException(s"[BusinessNameController][withAllBusinesses] - Error retrieving businesses, error: $error")
    }
  }

  private def isSaveAndRetrieve: Boolean = isEnabled(SaveAndRetrieve)

  //save & retrieve on should have an order of: business name (this) -> business start date -> business trade
  //save & retrieve off should have an order of: business start date -> business name (this) -> business trade
  def backUrl(id: String, isEditMode: Boolean): String = (isEditMode, isSaveAndRetrieve) match {
    case (true, true) => routes.SelfEmployedCYAController.show(id).url
    case (false, true) => appConfig.clientWhatIncomeSourceToSignUpUrl
    case (true, false) => routes.BusinessListCYAController.show.url
    case (false, false) => routes.BusinessStartDateController.show(id).url
  }

}
