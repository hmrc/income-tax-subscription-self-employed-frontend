

import uk.gov.hmrc.DefaultBuildSettings

import scala.sys.process._

val appName = "income-tax-subscription-self-employed-frontend"

ThisBuild / majorVersion := 1
ThisBuild / scalaVersion := "2.13.12"
lazy val microservice = Project(AppDependencies.appName, file("."))
  // silence all warnings on autogenerated files
  .settings(
    scalacOptions += "-Wconf:src=routes/.*:s",
    scalacOptions += "-Wconf:cat=unused-imports&src=html/.*:s",
    scalacOptions ++= Seq("-deprecation", "-feature"),
  )
  .settings(PlayKeys.playDefaultPort := 9563)
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    libraryDependencies ++= (AppDependencies.compile ++ AppDependencies.test)
  )

TwirlKeys.templateImports ++= Seq(
  "uk.gov.hmrc.govukfrontend.views.html.components._",
  "uk.gov.hmrc.hmrcfrontend.views.html.components._",
  "uk.gov.hmrc.hmrcfrontend.views.html.helpers._",
  "uk.gov.hmrc.govukfrontend.views.html.components.implicits._"
)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test") // the "test->test" allows reusing test code and test dependencies
  .settings(DefaultBuildSettings.itSettings())
  .settings(javaOptions += "-Dlogger.resource=logback-test.xml")

