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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers

import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.twirl.api.Html
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.businessAccountingMethodKey
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.IncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.utils.ReferenceRetrieval
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessAccountingMethodForm._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.FormUtil._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.AccountingMethodModel
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, MultipleSelfEmploymentsService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.BusinessAccountingMethod
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessAccountingMethodController @Inject()(businessAccountingMethod: BusinessAccountingMethod,
                                                   mcc: MessagesControllerComponents,
                                                   val incomeTaxSubscriptionConnector: IncomeTaxSubscriptionConnector,
                                                   multipleSelfEmploymentsService: MultipleSelfEmploymentsService,
                                                   authService: AuthService)
                                                  (implicit val ec: ExecutionContext, val appConfig: AppConfig)
  extends FrontendController(mcc) with ReferenceRetrieval with I18nSupport {

  def view(businessAccountingMethodForm: Form[AccountingMethodModel], id: String, businessCount: Int, isEditMode: Boolean)
          (implicit request: Request[AnyContent]): Html =
    businessAccountingMethod(
      businessAccountingMethodForm = businessAccountingMethodForm,
      postAction = uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.BusinessAccountingMethodController.submit(id, isEditMode),
      isEditMode: Boolean,
      backUrl = backUrl(id, isEditMode, businessCount)
    )

  def show(id: String, isEditMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withReference { reference =>
        withAccountingMethod(reference) { accountingMethod =>
          withSelfEmploymentsCount(reference) { businessCount =>
            Ok(view(businessAccountingMethodForm.fill(accountingMethod), id, businessCount, isEditMode))
          }
        }
      }
    }
  }

  def submit(id: String, isEditMode: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withReference { reference =>
        businessAccountingMethodForm.bindFromRequest().fold(
          formWithErrors =>
            withSelfEmploymentsCount(reference) { businessCount =>
              BadRequest(view(formWithErrors, id, businessCount, isEditMode))
            },
          businessAccountingMethod =>
            incomeTaxSubscriptionConnector.saveSubscriptionDetails(reference, businessAccountingMethodKey, businessAccountingMethod) map {
              case Right(_) =>
                Redirect(routes.SelfEmployedCYAController.show(id, isEditMode))
              case Left(_) =>
                throw new InternalServerException("[BusinessAccountingMethodController][submit] - Could not save business accounting method")
            }
        )
      }
    }
  }

  def backUrl(id: String, isEditMode: Boolean, selfEmploymentCount: Int): Option[String] = {
    if (isEditMode && selfEmploymentCount > 1) {
      Some(routes.ChangeAccountingMethodController.show(id).url)
    } else if (isEditMode) {
      Some(routes.SelfEmployedCYAController.show(id, isEditMode = true).url)
    } else None
  }

  private def withAccountingMethod(reference: String)(f: Option[AccountingMethodModel] => Future[Result])(implicit hc: HeaderCarrier): Future[Result] = {
    incomeTaxSubscriptionConnector.getSubscriptionDetails[AccountingMethodModel](reference, businessAccountingMethodKey)
      .map(_.getOrElse(throw new FetchAccountingMethodException))
      .flatMap(f)
  }

  private class FetchAccountingMethodException extends InternalServerException(
    "[BusinessAccountingMethodController][withAccountingMethod] - Failed to retrieve accounting method"
  )

  private def withSelfEmploymentsCount(reference: String)(f: Int => Result)(implicit hc: HeaderCarrier): Future[Result] = {
    multipleSelfEmploymentsService.fetchAllBusinesses(reference)
      .map(_.getOrElse(throw new FetchAllBusinessesException).length)
      .map(f)
  }

  private class FetchAllBusinessesException extends InternalServerException(
    "[BusinessAccountingMethodController][withSelfEmploymentsCount] - Failed to retrieve all self employments"
  )

}
