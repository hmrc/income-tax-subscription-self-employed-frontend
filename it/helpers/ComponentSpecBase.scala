
package helpers

import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, GivenWhenThen}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import org.scalatestplus.play.{PlaySpec, PortNumber}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.test.Helpers._
import play.api.{Application, Environment, Mode}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.forms.BusinessStartDateForm
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.BusinessStartDate


trait ComponentSpecBase extends PlaySpec with CustomMatchers with GuiceOneServerPerSuite
  with WiremockHelper with BeforeAndAfterAll with BeforeAndAfterEach with GivenWhenThen {


  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(config)
    .build

  implicit def ws(implicit app: Application): WSClient = app.injector.instanceOf[WSClient]

  val mockHost: String = WiremockHelper.wiremockHost
  val mockPort: String = WiremockHelper.wiremockPort.toString
  val mockUrl: String = s"http://$mockHost:$mockPort"


  def config: Map[String, String] = Map(
    "auditing.enabled" -> "false",
    "play.filters.csrf.header.bypassHeaders.Csrf-Token" -> "nocheck",
    "microservice.services.auth.host" -> mockHost,
    "microservice.services.auth.port" -> mockPort,
    "microservice.services.base.host" -> mockHost,
    "microservice.services.base.port" -> mockPort,
    "microservice.services.des.url" -> mockUrl,
    "microservice.services.income-tax-subscription.host" -> mockHost,
    "microservice.services.income-tax-subscription.port" -> mockPort
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


  def getBusinessStartDate(): WSResponse = get("/details/business-start-date")


  def submitBusinessStartDate(request: Option[BusinessStartDate]): WSResponse = {
    val uri = "/details/business-start-date"
    post(uri)(
      request.fold(Map.empty[String, Seq[String]])(
        model =>
          BusinessStartDateForm.businessStartDateForm("error").fill(model).data.map {
            case (k, v) =>
              (k, Seq(v))
          }
      )
    )
  }


}

