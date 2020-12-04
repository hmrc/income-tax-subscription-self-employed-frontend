
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

    def elementTextByID(id: String)(expectedValue: String): HavePropertyMatcher[WSResponse, String] =
    new HavePropertyMatcher[WSResponse, String] {

      def apply(response: WSResponse): HavePropertyMatchResult[String] = {
        val body = Jsoup.parse(response.body)

        HavePropertyMatchResult(
          body.getElementById(id).text == expectedValue,
          s"elementByID($id)",
          expectedValue,
          body.getElementById(id).text
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

  def textField(id: String, expectedValue: String): HavePropertyMatcher[WSResponse, String] =
    new HavePropertyMatcher[WSResponse, String] {

      def apply(response: WSResponse): HavePropertyMatchResult[String] = {
        val body = Jsoup.parse(response.body)
        val text = body.getElementById(id).`val`()
        HavePropertyMatchResult(
          text == expectedValue,
          "text field",
          expectedValue,
          text
        )
      }
    }

  def radioButtonSet(id: String, selectedRadioButton: Option[String]): HavePropertyMatcher[WSResponse, String] =
    new HavePropertyMatcher[WSResponse, String] {
      def apply(response: WSResponse): HavePropertyMatchResult[String] = {
        val body = Jsoup.parse(response.body)
        val radios = body.select(s"input[id^=$id]")
        val checkedAttr = "checked"

        def textForSelectedButton(idForSelectedRadio: String) =
          if (idForSelectedRadio.isEmpty) ""
          else body.select(s"label[for=$idForSelectedRadio]").text()

        val matchCondition = selectedRadioButton match {
          case Some(expectedOption) =>
            val idForSelectedRadio = radios.select(s"input[checked]").attr("id")
            textForSelectedButton(idForSelectedRadio) == expectedOption
          case None => !radios.hasAttr(checkedAttr)
        }

        HavePropertyMatchResult(
          matches = matchCondition,
          propertyName = "radioButton",
          expectedValue = selectedRadioButton.fold("")(identity),
          actualValue = {
            val selected = radios.select("input[checked]")
            selected.size() match {
              case 0 =>
                "no radio button is selected"
              case 1 =>
                val idForSelectedRadio = selected.attr("id")
                s"""The "${textForSelectedButton(idForSelectedRadio)}" selected"""
              case _ =>
                s"multiple radio buttons are selected: [$radios]"
            }
          }
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
