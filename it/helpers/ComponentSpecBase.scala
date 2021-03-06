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

package helpers

import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, GivenWhenThen}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import org.scalatestplus.play.{PlaySpec, PortNumber}
import play.api.http.HeaderNames
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, Environment, Mode}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.AddAnotherBusinessAgentForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._

trait ComponentSpecBase extends PlaySpec with CustomMatchers with GuiceOneServerPerSuite
  with WiremockHelper with BeforeAndAfterAll with BeforeAndAfterEach with GivenWhenThen {


  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(config)
    .build

  implicit def ws(implicit app: Application): WSClient = app.injector.instanceOf[WSClient]

  val titleSuffix = " - Use software to send Income Tax updates - GOV.UK"
  val agentTitleSuffix = " - Use software to report your client’s Income Tax - GOV.UK"


  val mockHost: String = WiremockHelper.wiremockHost
  val mockPort: String = WiremockHelper.wiremockPort.toString
  val mockUrl: String = s"http://$mockHost:$mockPort"

  implicit val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  def config: Map[String, String] = Map(
    "auditing.enabled" -> "false",
    "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck",
    "microservice.services.auth.host" -> mockHost,
    "microservice.services.auth.port" -> mockPort,
    "microservice.services.base.host" -> mockHost,
    "microservice.services.base.port" -> mockPort,
    "microservice.services.des.url" -> mockUrl,
    "microservice.services.income-tax-subscription.host" -> mockHost,
    "microservice.services.income-tax-subscription.port" -> mockPort,
    "microservice.services.address-lookup-frontend.host" -> mockHost,
    "microservice.services.address-lookup-frontend.port" -> mockPort
  )

  override def beforeAll(): Unit = {
    startWiremock()
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    stopWiremock()
    super.afterAll()
  }

  override def beforeEach(): Unit = {
    resetWiremock()
    super.beforeEach()
  }

  def get[T](uri: String)(implicit ws: WSClient, portNumber: PortNumber): WSResponse = {
    await(buildClient(uri).get)
  }

  def getWithHeaders(uri: String, headers: (String, String)*): WSResponse = {
    await(
      buildClient(uri)
        .withHttpHeaders(headers: _*)
        .get()
    )
  }

  def post[T](uri: String)(body: Map[String, Seq[String]]): WSResponse = {
    await(
      buildClient(uri)
        .withHttpHeaders("Csrf-Token" -> "nocheck")
        .post(body)
    )
  }


  val baseUrl: String = "/report-quarterly/income-and-expenses/sign-up/self-employments"

  def signOut: WSResponse = get("/logout")

  private def buildClient(path: String)(implicit ws: WSClient, portNumber: PortNumber): WSRequest =
    ws.url(s"http://localhost:${portNumber.value}$baseUrl$path").withFollowRedirects(false)


  def getBusinessStartDate(id: String): WSResponse = get(s"/details/business-start-date?id=$id")

  def getClientBusinessStartDate(id: String): WSResponse = get(s"/client/details/business-start-date?id=$id")

  def submitClientBusinessStartDate(id: String, request: Option[BusinessStartDate], inEditMode: Boolean = false): WSResponse = {
    val uri = s"/client/details/business-start-date?id=$id&isEditMode=$inEditMode"
    post(uri)(
      request.fold(Map.empty[String, Seq[String]])(
        model =>
          uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.BusinessStartDateForm.businessStartDateForm(
            "minStartDateError", "maxStartDateError"
          ).fill(model).data.map {
            case (k, v) =>
              (k, Seq(v))
          }
      )
    )
  }

  def getClientBusinessName(id: String): WSResponse = get(s"/client/details/business-name?id=$id")

  def submitClientBusinessName(id: String, inEditMode: Boolean, request: Option[BusinessNameModel]): WSResponse = {
    val uri = s"/client/details/business-name?id=$id&isEditMode=$inEditMode"
    post(uri)(
      request.fold(Map.empty[String, Seq[String]])(
        model =>
          uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.BusinessNameForm.businessNameValidationForm(Nil).fill(model).data.map {
            case (k, v) =>
              (k, Seq(v))
          }
      )
    )
  }

  def getClientTradeName(id: String): WSResponse = get(s"/client/details/business-trade?id=$id")

  def submitClientTradeName(id: String, request: Option[BusinessTradeNameModel], inEditMode: Boolean = false): WSResponse = {
    val uri = s"/client/details/business-trade?id=$id&isEditMode=$inEditMode"
    post(uri)(
      request.fold(Map.empty[String, Seq[String]])(
        model =>
          BusinessTradeNameForm.businessTradeNameValidationForm(Nil).fill(model).data.map {
            case (k, v) =>
              (k, Seq(v))
          }
      )
    )
  }

  def submitBusinessStartDate(request: Option[BusinessStartDate], id: String, inEditMode: Boolean = false): WSResponse = {
    val uri = s"/details/business-start-date?id=$id&isEditMode=$inEditMode"
    post(uri)(
      request.fold(Map.empty[String, Seq[String]])(
        model =>
          uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.individual.BusinessStartDateForm.businessStartDateForm(
            "minStartDateError", "maxStartDateError"
          ).fill(model).data.map {
            case (k, v) =>
              (k, Seq(v))
          }
      )
    )
  }

  def getBusinessName(id: String): WSResponse = get(s"/details/business-name?id=$id")

  def submitBusinessName(id: String, inEditMode: Boolean, request: Option[BusinessNameModel]): WSResponse = {
    val uri = s"/details/business-name?id=$id&isEditMode=$inEditMode"
    post(uri)(
      request.fold(Map.empty[String, Seq[String]])(
        model =>
          BusinessNameForm.businessNameValidationForm(Nil).fill(model).data.map { case (k, v) => (k, Seq(v)) }
      )
    )
  }

  def getBusinessTradeName(id: String): WSResponse = get(s"/details/business-trade?id=$id")

  def submitBusinessTradeName(id: String, inEditMode: Boolean, request: Option[BusinessTradeNameModel]): WSResponse = {
    val uri = s"/details/business-trade?id=$id&editMode=$inEditMode"
    post(uri)(
      request.fold(Map.empty[String, Seq[String]])(
        model =>
          BusinessTradeNameForm.businessTradeNameValidationForm(Nil).fill(model).data.map {
            case (k, v) =>
              (k, Seq(v))
          }
      )
    )
  }

  def getTimeout: WSResponse = get(uri = "/timeout")
  def getClientTimeout: WSResponse = get(uri = "/client/timeout")

  def getKeepAlive: WSResponse = get(uri = "/keep-alive")
  def getClientKeepAlive: WSResponse = get(uri = "/client/keep-alive")


  def getBusinessAccountingMethod(inEditMode: Boolean = false): WSResponse = get(s"/details/business-accounting-method?isEditMode=$inEditMode")

  def submitBusinessAccountingMethod(request: Option[AccountingMethodModel],
                                     inEditMode: Boolean = false): WSResponse = {
    val uri = s"/details/business-accounting-method?isEditMode=$inEditMode"
    post(uri)(
      request.fold(Map.empty[String, Seq[String]])(
        model =>
          BusinessAccountingMethodForm.businessAccountingMethodForm.fill(model).data.map {
            case (k, v) =>
              (k, Seq(v))
          }
      )
    )
  }

  def getClientBusinessAccountingMethod(): WSResponse = get("/client/details/business-accounting-method")

  def submitClientBusinessAccountingMethod(request: Option[AccountingMethodModel],
                                           inEditMode: Boolean = false): WSResponse = {
    val uri = s"/client/details/business-accounting-method?isEditMode=$inEditMode"
    post(uri)(
      request.fold(Map.empty[String, Seq[String]])(
        model =>
          uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.agent.BusinessAccountingMethodForm.businessAccountingMethodForm.fill(model).data.map {
            case (k, v) =>
              (k, Seq(v))
          }
      )
    )
  }

  def getCheckYourAnswers: WSResponse = get(s"/details/business-list")

  def submitCheckYourAnswers(request: Option[AddAnotherBusinessModel],
                             currentBusinesses: Int,
                             limit: Int
                            ): WSResponse = {
    val uri = s"/details/business-list"
    post(uri)(
      request.fold(Map.empty[String, Seq[String]])(
        model =>
          AddAnotherBusinessForm.addAnotherBusinessForm(currentBusinesses, limit).fill(model).data.map { case (k, v) => (k, Seq(v)) }
      )
    )
  }

  def getClientCheckYourAnswers(id: String): WSResponse = get(s"/client/details/business-list?id=$id")

  def submitClientCheckYourAnswers(request: Option[AddAnotherBusinessModel],
                                   currentBusinesses: Int,
                                   limit: Int): WSResponse = {
    val uri = s"/client/details/business-list"
    post(uri)(
      request.fold(Map.empty[String, Seq[String]])(
        model =>
          AddAnotherBusinessAgentForm.addAnotherBusinessForm(currentBusinesses, limit).fill(model).data.map { case (k, v) => (k, Seq(v)) }
      )
    )
  }

  def getAddressLookupInitialise(itsaId: String): WSResponse = get(s"/address-lookup-initialise/$itsaId")

  def getAddressLookup(itsaId: String, id: String): WSResponse = get(s"/details/address-lookup/$itsaId?id=$id")

  def getClientAddressLookupInitialise(itsaId: String): WSResponse = get(s"/client/address-lookup-initialise/$itsaId")

  def getClientAddressLookup(itsaId: String, id: String): WSResponse = get(s"/client/details/address-lookup/$itsaId?id=$id")

  def removeHtmlMarkup(stringWithMarkup: String): String =
    stringWithMarkup.replaceAll("<.+?>", " ").replaceAll("[\\s]{2,}", " ").trim

}
