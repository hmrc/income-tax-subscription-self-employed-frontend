
import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val appName = "income-tax-subscription-self-employed-frontend"

  private val bootstrapVersion = "8.3.0"
  private val playHmrcFrontendVersion = "8.1.0"
  private val catsVersion = "2.8.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % playHmrcFrontendVersion,
    "org.typelevel" %% "cats-core" % catsVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % Test,
    "org.jsoup" % "jsoup" % "1.14.3" % Test
  )

}
