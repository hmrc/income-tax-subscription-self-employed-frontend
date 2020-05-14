import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(

    "uk.gov.hmrc"             %% "govuk-template"           % "5.54.0-play-26",
    "uk.gov.hmrc"             %% "play-ui"                  % "8.9.0-play-26",
    "uk.gov.hmrc"             %% "bootstrap-play-26"        % "1.7.0"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-play-26"        % "1.7.0"                 % Test classifier "tests",
    "org.scalatest"           %% "scalatest"                % "3.0.8"                 % "test, it",
    "org.jsoup"               %  "jsoup"                    % "1.10.2"                % "test",
    "com.typesafe.play"       %% "play-test"                % current                 % "test, it",
    "org.pegdown"             %  "pegdown"                  % "1.6.0"                 % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "3.1.2"                 % "test, it",
    "com.github.tomakehurst"  %  "wiremock-jre8"            % "2.23.2"                % "it"
  )

}