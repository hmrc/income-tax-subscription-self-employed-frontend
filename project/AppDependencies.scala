
import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val appName = "income-tax-subscription-self-employed-frontend"

  private val bootstrapVersion = "7.19.0"
  private val playHmrcFrontendVersion = "8.1.0"
  private val scalatestplusVersion = "5.1.0"
  private val catsVersion = "2.8.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-28" % playHmrcFrontendVersion,
    "org.typelevel" %% "cats-core" % catsVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-28" % bootstrapVersion % Test,
    "org.scalatest" %% "scalatest" % "3.2.11" % "test, it",
    "org.jsoup" % "jsoup" % "1.14.3" % "test, it",
    "com.typesafe.play" %% "play-test" % current % "test, it",
    "org.pegdown" % "pegdown" % "1.6.0" % "test, it",
    "org.scalatestplus.play" %% "scalatestplus-play" % scalatestplusVersion % "test, it",
    "com.github.tomakehurst" % "wiremock-jre8" % "2.32.0" % "it",
    "org.mockito" % "mockito-core" % "4.4.0" % "test, it",
    "org.scalatestplus" %% "mockito-3-12" % "3.2.10.0" % "test, it",
    "com.vladsch.flexmark" % "flexmark-all" % "0.62.2" % "test, it",
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.0" % "it"
  )

}
