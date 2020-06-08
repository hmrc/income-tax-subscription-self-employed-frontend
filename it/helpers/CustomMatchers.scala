
package helpers


import org.jsoup.Jsoup
import org.scalatest.matchers._
import play.api.libs.json.Reads
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.DateModel

trait CustomMatchers {
  def httpStatus(expectedValue: Int): HavePropertyMatcher[WSResponse, Int] =
    new HavePropertyMatcher[WSResponse, Int] {
      def apply(response: WSResponse) =
        HavePropertyMatchResult(
          response.status == expectedValue,
          "httpStatus",
          expectedValue,
          response.status
        )
    }

  def jsonBodyAs[T](expectedValue: T)(implicit reads: Reads[T]): HavePropertyMatcher[WSResponse, T] =
    new HavePropertyMatcher[WSResponse, T] {
      def apply(response: WSResponse) =
        HavePropertyMatchResult(
          response.json.as[T] == expectedValue,
          "jsonBodyAs",
          expectedValue,
          response.json.as[T]
        )
    }

  val emptyBody: HavePropertyMatcher[WSResponse, String] =
    new HavePropertyMatcher[WSResponse, String] {
      def apply(response: WSResponse) =
        HavePropertyMatchResult(
          response.body == "",
          "emptyBody",
          "",
          response.body
        )
    }

  def pageTitle(expectedValue: String): HavePropertyMatcher[WSResponse, String] =
    new HavePropertyMatcher[WSResponse, String] {

      def apply(response: WSResponse): HavePropertyMatchResult[String] = {
        val body = Jsoup.parse(response.body)

        HavePropertyMatchResult(
          body.title == expectedValue,
          "pageTitle",
          expectedValue,
          body.title
        )
      }
    }

  def dateField(id: String, expectedValue: DateModel): HavePropertyMatcher[WSResponse, String] =
    new HavePropertyMatcher[WSResponse, String] {

      def apply(response: WSResponse): HavePropertyMatchResult[String] = {
        val body = Jsoup.parse(response.body)
        val day = body.getElementById(id + ".dateDay").`val`()
        val month = body.getElementById(id + ".dateMonth").`val`()
        val year = body.getElementById(id + ".dateYear").`val`()
        HavePropertyMatchResult(
          day == expectedValue.day && month == expectedValue.month && year == expectedValue.year,
          "day",
          expectedValue.toString,
          day + " / " + month + " / " + year
        )
      }
    }

  def redirectURI(expectedValue: String): HavePropertyMatcher[WSResponse, String] = new HavePropertyMatcher[WSResponse, String] {
    def apply(response: WSResponse): HavePropertyMatchResult[String] = {
      val redirectLocation: Option[String] = response.header("Location")

      val matchCondition = redirectLocation.exists(_.contains(expectedValue))
      HavePropertyMatchResult(
        matchCondition,
        "redirectUri",
        expectedValue,
        redirectLocation.getOrElse("")
      )
    }
  }

}

