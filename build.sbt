import uk.gov.hmrc.ForkedJvmPerTestSettings.oneForkedJvmPerTest
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import scala.sys.process._

val appName = "income-tax-subscription-self-employed-frontend"

lazy val microservice = Project(AppDependencies.appName, file("."))
  .settings(scalaVersion := "2.12.15")
  // silence all warnings on autogenerated files
  .settings(scalacOptions += s"-Wconf:src=${target.value}/.*:s")
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    majorVersion := 0,
    libraryDependencies ++= (AppDependencies.compile ++ AppDependencies.test)
  )
  .settings(publishingSettings: _*)
  .configs(IntegrationTest)
  .settings(
    Keys.fork in IntegrationTest := false,
    Defaults.itSettings,
    IntegrationTest / unmanagedSourceDirectories += baseDirectory(_ / "it").value,
    IntegrationTest / parallelExecution := false,
    IntegrationTest / testGrouping := oneForkedJvmPerTest((IntegrationTest / definedTests).value),
  )
  .settings(resolvers += Resolver.jcenterRepo)

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;models/.data/..*;" +
      "filters.*;.handlers.*;components.*;.*BuildInfo.*;.*FrontendAuditConnector.*;.*Routes.*;views.html.*;appConfig.*;" +
      "controllers.feedback.*;app.*;prod.*;appConfig.*;com.*;testOnly.*;\"",
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true
  )
}

TwirlKeys.templateImports ++= Seq(
  "uk.gov.hmrc.govukfrontend.views.html.components._",
  "uk.gov.hmrc.hmrcfrontend.views.html.components._",
  "uk.gov.hmrc.hmrcfrontend.views.html.helpers._",
  "uk.gov.hmrc.govukfrontend.views.html.components.implicits._"
)

lazy val results = taskKey[Unit]("Opens test results'")
results := { "open target/test-reports/html-report/index.html" ! }
Test / results := (results).value

lazy val itResults = taskKey[Unit]("Opens it test results'")
itResults := { "open target/int-test-reports/html-report/index.html" ! }
IntegrationTest / results := (itResults).value
