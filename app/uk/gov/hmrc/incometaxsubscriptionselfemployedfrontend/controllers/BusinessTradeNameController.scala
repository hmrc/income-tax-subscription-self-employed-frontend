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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers

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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessTradeNameForm._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.FormUtil._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.{BusinessTradeNameModel, SelfEmploymentData}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, MultipleSelfEmploymentsService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.BusinessTradeName
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessTradeNameController @Inject()(mcc: MessagesControllerComponents,
                                            businessTradeName: BusinessTradeName,
                                            multipleSelfEmploymentsService: MultipleSelfEmploymentsService,
                                            val incomeTaxSubscriptionConnector: IncomeTaxSubscriptionConnector,
                                            authService: AuthService)
                                           (implicit val ec: ExecutionContext, val appConfig: AppConfig)
  extends FrontendController(mcc) with I18nSupport with FeatureSwitching with ReferenceRetrieval {

  private def isSaveAndRetrieve: Boolean = isEnabled(SaveAndRetrieve)

  def view(businessTradeNameForm: Form[BusinessTradeNameModel], id: String, isEditMode: Boolean)(implicit request: Request[AnyContent]): Html =
    businessTradeName(
      businessTradeNameForm = businessTradeNameForm,
      postAction = uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.BusinessTradeNameController.submit(id, isEditMode = isEditMode),
      isEditMode,
      backUrl = backUrl(id, isEditMode)
    )

  def show(id: String, isEditMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withReference { reference =>
        withAllBusinesses(reference) { businesses =>
          val excludedBusinessTradeNames = getExcludedBusinessTradeNames(id, businesses)
          val currentBusiness = businesses.find(_.id == id)
          (currentBusiness.flatMap(_.businessName), currentBusiness.flatMap(_.businessTradeName)) match {
            case (None, _) => Future.successful(Redirect(routes.BusinessNameController.show(id)))
            case (_, trade) =>
              Future.successful(Ok(view(businessTradeNameValidationForm(excludedBusinessTradeNames).fill(trade), id, isEditMode)))
          }
        }
      }
    }
  }


  def submit(id: String, isEditMode: Boolean): Action[AnyContent] = Action.async {
    implicit request =>
      authService.authorised() {
        withReference { reference =>
          withAllBusinesses(reference) { businesses =>
            val excludedBusinessTradeNames = getExcludedBusinessTradeNames(id, businesses)
            businessTradeNameValidationForm(excludedBusinessTradeNames).bindFromRequest.fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, id, isEditMode = isEditMode))),
              businessTradeNameData =>
                multipleSelfEmploymentsService.saveBusinessTrade(reference, id, businessTradeNameData).map { _ =>
                  val call = (isEditMode, isSaveAndRetrieve) match {
                    case (true, true) => uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.SelfEmployedCYAController.show(id)
                    case (false, true) => uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.AddressLookupRoutingController.initialiseAddressLookupJourney(id)
                    case (true, false) => uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.BusinessListCYAController.show
                    case (false, false) => uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.AddressLookupRoutingController.initialiseAddressLookupJourney(id)
                  }
                  Redirect(call)
                }
            )
          }
        }
      }
  }

  private def getExcludedBusinessTradeNames(id: String, businesses: Seq[SelfEmploymentData]): Seq[BusinessTradeNameModel] = {
    val currentBusinessName = businesses.find(_.id == id).flatMap(_.businessName)
    businesses.filterNot(_.id == id).filter {
      case SelfEmploymentData(_, _, _, Some(name), _, _, _) if currentBusinessName contains name => true
      case _ => false
    }.flatMap(_.businessTradeName)
  }

  private def withAllBusinesses(reference: String)(f: Seq[SelfEmploymentData] => Future[Result])(implicit hc: HeaderCarrier): Future[Result] = {
    multipleSelfEmploymentsService.fetchAllBusinesses(reference).flatMap {
      case Right(businesses) => f(businesses)
      case Left(error) => throw new InternalServerException(s"[BusinessTradeNameController][withAllBusinesses] - Error retrieving businesses, error: $error")
    }
  }

  def backUrl(id: String, isEditMode: Boolean): String = {
    (isEditMode, isSaveAndRetrieve) match {
      case (true, true) => uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.SelfEmployedCYAController.show(id).url
      case (false, true) => uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.BusinessStartDateController.show(id).url
      case (true, false) => uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.BusinessListCYAController.show.url
      case (false, false) => uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.BusinessNameController.show(id).url
    }
  }

}
