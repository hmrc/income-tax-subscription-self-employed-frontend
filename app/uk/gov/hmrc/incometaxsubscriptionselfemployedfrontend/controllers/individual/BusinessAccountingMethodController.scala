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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.individual

import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.twirl.api.Html
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.utils.ReferenceRetrieval
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessAccountingMethodForm._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.FormUtil._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.AccountingMethod
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, MultipleSelfEmploymentsService, SessionDataService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.BusinessAccountingMethod
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessAccountingMethodController @Inject()(businessAccountingMethod: BusinessAccountingMethod,
                                                   mcc: MessagesControllerComponents,
                                                   multipleSelfEmploymentsService: MultipleSelfEmploymentsService,
                                                   authService: AuthService)
                                                  (val sessionDataService: SessionDataService,
                                                   val appConfig: AppConfig)
                                                  (implicit val ec: ExecutionContext)

  extends FrontendController(mcc) with ReferenceRetrieval with I18nSupport {

  def view(businessAccountingMethodForm: Form[AccountingMethod], id: String, businessCount: Int, isEditMode: Boolean, isGlobalEdit: Boolean)
          (implicit request: Request[AnyContent]): Html =
    businessAccountingMethod(
      businessAccountingMethodForm = businessAccountingMethodForm,
      postAction = routes.BusinessAccountingMethodController.submit(id, isEditMode, isGlobalEdit),
      isEditMode: Boolean,
      backUrl = backUrl(id, isEditMode, isGlobalEdit, businessCount)
    )

  def show(id: String, isEditMode: Boolean, isGlobalEdit: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withIndividualReference { reference =>
        withAccountingMethod(reference) { accountingMethod =>
          withSelfEmploymentsCount(reference) { businessCount =>
            Ok(view(businessAccountingMethodForm.fill(accountingMethod), id, businessCount, isEditMode, isGlobalEdit))
          }
        }
      }
    }
  }

  def submit(id: String, isEditMode: Boolean, isGlobalEdit: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withIndividualReference { reference =>
        businessAccountingMethodForm.bindFromRequest().fold(
          formWithErrors =>
            withSelfEmploymentsCount(reference) { businessCount =>
              BadRequest(view(formWithErrors, id, businessCount, isEditMode, isGlobalEdit))
            },
          businessAccountingMethod =>
            multipleSelfEmploymentsService.saveAccountingMethod(reference, id, businessAccountingMethod) map {
              case Right(_) =>
                if (isEditMode || isGlobalEdit) {
                  Redirect(routes.SelfEmployedCYAController.show(id, isEditMode, isGlobalEdit))
                } else {
                  Redirect(routes.FullIncomeSourceController.show(id))
                }
              case Left(_) => throw new InternalServerException("[BusinessAccountingMethodController][submit] - Could not save business accounting method")
            }
        )
      }
    }
  }

  def backUrl(id: String, isEditMode: Boolean, isGlobalEdit: Boolean, selfEmploymentCount: Int): String = {
    if ((isEditMode || isGlobalEdit) && selfEmploymentCount > 1) {
      routes.ChangeAccountingMethodController.show(id, isGlobalEdit).url
    } else if (isEditMode || isGlobalEdit) {
      routes.SelfEmployedCYAController.show(id, isEditMode = true, isGlobalEdit = isGlobalEdit).url
    } else {
      appConfig.yourIncomeSourcesUrl
    }

  }

  private def withAccountingMethod(reference: String)(f: Option[AccountingMethod] => Future[Result])
                                  (implicit hc: HeaderCarrier): Future[Result] = {
    multipleSelfEmploymentsService.fetchAccountingMethod(reference)
      .map(_.getOrElse(throw new FetchAccountingMethodException))
      .flatMap(f)
  }

  private class FetchAccountingMethodException extends InternalServerException(
    "[BusinessAccountingMethodController][withAccountingMethod] - Failed to retrieve accounting method"
  )

  private def withSelfEmploymentsCount(reference: String)(f: Int => Result)(implicit hc: HeaderCarrier): Future[Result] = {
    multipleSelfEmploymentsService.fetchSoleTraderBusinesses(reference)
      .map(_.getOrElse(throw new FetchAllBusinessesException).map(_.businesses.length).getOrElse(0))
      .map(f)
  }

  private class FetchAllBusinessesException extends InternalServerException(
    "[BusinessAccountingMethodController][withSelfEmploymentsCount] - Failed to retrieve all self employments"
  )

}
