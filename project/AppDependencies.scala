
import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  private val playLanguageVersion = "4.10.0-play-26"
  private val bootstrapVersion = "5.3.0"
  private val playHmrcFrontendVersion = "0.72.0-play-26"

  val compile = Seq(
    "uk.gov.hmrc" %% "govuk-template" % "5.60.0-play-26",
    "uk.gov.hmrc" %% "play-ui" % "8.19.0-play-26",
    "uk.gov.hmrc" %% "bootstrap-frontend-play-26" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-language" % playLanguageVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc" % playHmrcFrontendVersion
  )

  val test = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-26" % "5.3.0" % Test,
    "org.scalatest" %% "scalatest" % "3.0.9" % "test, it",
    "org.jsoup" % "jsoup" % "1.13.1" % "test, it",
    "com.typesafe.play" %% "play-test" % current % "test, it",
    "org.pegdown" % "pegdown" % "1.6.0" % "test, it",
    "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.3" % "test, it",
    "com.github.tomakehurst" % "wiremock-jre8" % "2.27.2" % "it",
    "org.mockito" % "mockito-core" % "3.7.0" % "test, it"
  )

}
