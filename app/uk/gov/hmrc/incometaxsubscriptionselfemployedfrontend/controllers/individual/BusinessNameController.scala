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
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitching
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.utils.ReferenceRetrieval
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessNameForm._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.utils.FormUtil._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.{AuthService, MultipleSelfEmploymentsService, SessionDataService}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.BusinessName
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessNameController @Inject()(mcc: MessagesControllerComponents,
                                       businessName: BusinessName,
                                       multipleSelfEmploymentsService: MultipleSelfEmploymentsService,
                                       authService: AuthService)
                                      (val sessionDataService: SessionDataService,
                                       val appConfig: AppConfig)
                                      (implicit val ec: ExecutionContext)
  extends FrontendController(mcc) with ReferenceRetrieval with I18nSupport with FeatureSwitching {

  def view(businessNameForm: Form[String], id: String, isEditMode: Boolean, isGlobalEdit: Boolean)
          (implicit request: Request[AnyContent]): Html =
    businessName(
      businessNameForm = businessNameForm,
      postAction = routes.BusinessNameController.submit(id, isEditMode = isEditMode, isGlobalEdit = isGlobalEdit),
      isEditMode,
      backUrl = backUrl(id, isEditMode, isGlobalEdit)
    )

  def show(id: String, isEditMode: Boolean, isGlobalEdit: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withIndividualReference { reference =>
        getCurrentNameAndExcludedNames(reference, id) flatMap { case (currentName, excludedNames) =>
          Future.successful(Ok(
            view(businessNameValidationForm(excludedNames).fill(currentName), id, isEditMode = isEditMode, isGlobalEdit = isGlobalEdit)
          ))
        }
      }
    }
  }

  def submit(id: String, isEditMode: Boolean, isGlobalEdit: Boolean): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      withIndividualReference { reference =>
        getCurrentNameAndExcludedNames(reference, id) flatMap { case (_, excludedNames) =>
          businessNameValidationForm(excludedNames).bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, id, isEditMode = isEditMode, isGlobalEdit = isGlobalEdit))),
            name =>
              multipleSelfEmploymentsService.saveName(reference, id, name) map {
                case Right(_) =>
                  next(id, isEditMode, isGlobalEdit)
                case Left(_) =>
                  throw new InternalServerException("[BusinessNameController][submit] - Could not save business name")
              }
          )
        }
      }
    }
  }

  private def next(id: String, isEditMode: Boolean, isGlobalEdit: Boolean) = Redirect(
    if (isEditMode || isGlobalEdit) {
      routes.SelfEmployedCYAController.show(id, isEditMode = isEditMode, isGlobalEdit = isGlobalEdit)
    } else {
      routes.BusinessStartDateController.show(id)
    }
  )

  private def getCurrentNameAndExcludedNames(reference: String, businessId: String)
                                            (implicit request: Request[AnyContent]): Future[(Option[String], Seq[String])] = {
    multipleSelfEmploymentsService.fetchAllNameTradeCombos(reference) map {
      case Right(combos) =>
        val currentBusinessTrade: Option[String] = combos.collectFirst {
          case (id, _, trade) if id == businessId => trade
        }.flatten

        val currentBusinessName: Option[String] = combos.collectFirst {
          case (id, name, _) if id == businessId => name
        }.flatten

        val excludedNames = combos collect {
          case (id, Some(name), Some(trade)) if id != businessId && currentBusinessTrade.contains(trade) => name
        }

        (currentBusinessName, excludedNames)
      case _ =>
        throw new InternalServerException("[BusinessNameController][getCurrentNameAndExcludedNames] - Unable to retrieve name trade combos")
    }
  }

  def backUrl(id: String, isEditMode: Boolean, isGlobalEdit: Boolean)(implicit request: Request[AnyContent]): String = {
    if (isEditMode || isGlobalEdit) {
      routes.SelfEmployedCYAController.show(id, isEditMode = isEditMode, isGlobalEdit = isGlobalEdit).url
    } else if (doesUserNameExists) {
      routes.BusinessNameConfirmationController.show(id).url
    } else {
      appConfig.yourIncomeSourcesUrl
    }
  }

  private val FullNameSessionKey: String = "FULLNAME"

  private def doesUserNameExists(implicit request: Request[AnyContent]): Boolean = {
    request.session.get(FullNameSessionKey).isDefined
  }


}


