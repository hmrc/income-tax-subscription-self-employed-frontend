
import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  private val playLanguageVersion = "5.1.0-play-28"
  private val bootstrapVersion = "5.14.0"
  private val playHmrcFrontendVersion = "3.0.0-play-28"
  private val scalatestplusVersion = "5.0.0"

  val compile = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-language" % playLanguageVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc" % playHmrcFrontendVersion
  )

  val test = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % bootstrapVersion % Test,
    "org.scalatest" %% "scalatest" % "3.0.9" % "test, it",
    "org.jsoup" % "jsoup" % "1.13.1" % "test, it",
    "com.typesafe.play" %% "play-test" % current % "test, it",
    "org.pegdown" % "pegdown" % "1.6.0" % "test, it",
    "org.scalatestplus.play" %% "scalatestplus-play" % scalatestplusVersion % "test, it",
    "com.github.tomakehurst" % "wiremock-jre8" % "2.27.2" % "it",
    "org.mockito" % "mockito-core" % "3.7.0" % "test, it"
  )

}
