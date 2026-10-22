/*
 * Copyright 2026 HM Revenue & Customs
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
import play.api.mvc.*
import play.twirl.api.Html
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.individual.actions.IdentifierAction
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.DisplayNameForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.SoleTraderBusinesses
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.MultipleSelfEmploymentsService
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.views.html.individual.DisplayName
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.play.language.LanguageUtils

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DisplayNameController @Inject()(displayNameView: DisplayName,
                                      mcc: MessagesControllerComponents,
                                      multipleSelfEmploymentsService: MultipleSelfEmploymentsService,
                                      identify: IdentifierAction)
                                     (val languageUtils: LanguageUtils)
                                     (implicit val ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def show(id: String, isEditMode: Boolean, isGlobalEdit: Boolean): Action[AnyContent] = identify.async { implicit request =>
        getBusinessesWithDuplicateBusinessNames(request.reference, id) map { businesses =>
          Ok(view(
            displayNameForm = populateFormFromSavedDetails(businesses.businesses.map(_.displayName)),
            businesses = businesses,
            id = id,
            isEditMode = isEditMode,
            isGlobalEdit = isGlobalEdit
          ))
        }
  }

  def submit(id: String, isEditMode: Boolean, isGlobalEdit: Boolean): Action[AnyContent] =
    identify.async { implicit request =>
      getBusinessesWithDuplicateBusinessNames(request.reference, id).flatMap { businesses =>
        DisplayNameForm.displayNameForm.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, businesses, id, isEditMode, isGlobalEdit))),
          displayNames =>
            val duplicateIndexes = displayNames.indices.filter { index =>
              displayNames.count(_ == displayNames(index)) > 1
            }
            if (duplicateIndexes.nonEmpty) {
              val formWithDuplicateErrors = duplicateIndexes.foldLeft(
                DisplayNameForm.displayNameForm.fill(displayNames)
              ) { (form, index) =>
                form.withError(
                  s"${DisplayNameForm.displayName}[$index]",
                  "individual.error.display-name.duplicate"
                )
              }
              Future.successful(BadRequest(view(
                formWithDuplicateErrors,
                businesses, id, isEditMode, isGlobalEdit
              )))
            } else {
              saveAndContinue(request.reference, id, businesses, displayNames, isEditMode, isGlobalEdit)
            }
        )
      }
  }

  private def populateFormFromSavedDetails(displayNames: Seq[Option[String]])
                                          (implicit hc: HeaderCarrier): Form[Seq[String]] =
      DisplayNameForm.displayNameForm
        .bind(DisplayNameForm.populateNameData(displayNames))
        .discardingErrors


  private def getBusinessesWithDuplicateBusinessNames(reference: String, id: String)(implicit hc: HeaderCarrier): Future[SoleTraderBusinesses] = {
    multipleSelfEmploymentsService.fetchSoleTraderBusinesses(reference) map {
      case Right(Some(soleTraderBusinesses)) =>
        soleTraderBusinesses.businesses.find(_.id == id).map(_.name.getOrElse("")) match {
          case Some(name) if name != "" =>
            soleTraderBusinesses.businesses.filter(_.id != id).filter(_.name.contains(name)) match {
              case businesses if businesses.nonEmpty => SoleTraderBusinesses(businesses)
              case _ =>  throw new InternalServerException(
                s"[DisplayNameController][getBusinessesWithDuplicateBusinessNames] - No duplicate businesses for id $id and name $name")
            }
          case _ => throw new InternalServerException(
            s"[DisplayNameController][getBusinessesWithDuplicateBusinessNames] - Could not find name for business with id $id")
        }
      case Right(None) =>
        throw new InternalServerException(
          s"[DisplayNameController][getBusinessesWithDuplicateBusinessNames] - No businesses found for reference $reference")
      case Left(_) =>
        throw new InternalServerException(
          s"[DisplayNameController][getBusinessesWithDuplicateBusinessNames] - Unable to fetch businesses for reference $reference")
    }
  }

  private def saveAndContinue(reference: String, id: String,
                              businesses: SoleTraderBusinesses,
                              displayNames: Seq[String],
                              isEditMode: Boolean,
                              isGlobalEdit: Boolean)
                             (implicit hc: HeaderCarrier): Future[Result] = {
    Future.traverse(displayNames.zip(businesses.businesses.map(_.id))) { case (displayName, businessId) =>
      multipleSelfEmploymentsService.saveDisplayName(reference, businessId, displayName).map {
        case Right(_) => ()
        case Left(_) =>
          throw new InternalServerException(
            s"[DisplayNameController][saveAndContinue] - Could not save display name for business with id $businessId")
      }
    }.map { _ =>
      if (isEditMode || isGlobalEdit) {
        Redirect(routes.SelfEmployedCYAController.show(id, isEditMode, isGlobalEdit))
      } else {
        Redirect(routes.BusinessStartDateBeforeLimitController.show(id, isEditMode, isGlobalEdit))
      }
    }
  }

  private def view(displayNameForm: Form[Seq[String]], businesses: SoleTraderBusinesses, id: String, isEditMode: Boolean, isGlobalEdit: Boolean)
                  (implicit request: Request[AnyContent]): Html =
    displayNameView(
      displayNameForm = displayNameForm,
      businesses = businesses,
      postAction = routes.DisplayNameController.submit(id, isEditMode, isGlobalEdit),
      isEditMode = isEditMode || isGlobalEdit
    )
}