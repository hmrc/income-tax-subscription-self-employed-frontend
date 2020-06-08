
import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(

    "uk.gov.hmrc"             %% "govuk-template"           % "5.54.0-play-26",
    "uk.gov.hmrc"             %% "play-ui"                  % "8.9.0-play-26",
    "uk.gov.hmrc"             %% "bootstrap-play-26"        % "1.7.0",
    "uk.gov.hmrc"             %% "play-language"            % "4.3.0-play-26"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-play-26"        % "1.7.0"                 % Test classifier "tests",
    "org.scalatest"           %% "scalatest"                % "3.0.8"                 % "test, it",
    "org.jsoup"               %  "jsoup"                    % "1.10.2"                % "test, it",
    "com.typesafe.play"       %% "play-test"                % current                 % "test, it",
    "org.pegdown"             %  "pegdown"                  % "1.6.0"                 % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "3.1.2"                 % "test, it",
    "com.github.tomakehurst"  %  "wiremock-jre8"            % "2.23.2"                % "it",
    "org.mockito"             % "mockito-core"              % "2.25.1"                % "test, it"
  )

}
