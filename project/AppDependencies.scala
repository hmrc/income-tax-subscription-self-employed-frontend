
import sbt.*
import play.sbt.PlayImport.*

object AppDependencies {

  val appName = "income-tax-subscription-self-employed-frontend"

  private val bootstrapVersion = "10.4.0"
  private val playHmrcFrontendVersion = "12.20.0"
  private val catsVersion = "2.13.0"
  private val cryptoJsonVersion = "8.4.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % playHmrcFrontendVersion,
    "uk.gov.hmrc" %% "crypto-json-play-30" % cryptoJsonVersion,
    "org.typelevel" %% "cats-core" % catsVersion
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % Test,
    "org.jsoup" % "jsoup" % "1.21.2" % Test
  )

}
