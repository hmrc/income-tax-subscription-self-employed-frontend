
import play.core.PlayVersion.current
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  val compile = Seq(

    "uk.gov.hmrc"             %% "govuk-template"           % "5.60.0-play-26",
    "uk.gov.hmrc"             %% "play-ui"                  % "8.19.0-play-26",
    "uk.gov.hmrc"             %% "bootstrap-play-26"        % "2.3.0",
    "uk.gov.hmrc"             %% "play-language"            % "4.5.0-play-26"
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-play-26"        % "2.3.0"                 % Test classifier "tests",
    "org.scalatest"           %% "scalatest"                % "3.0.9"                 % "test, it",
    "org.jsoup"               %  "jsoup"                    % "1.13.1"                % "test, it",
    "com.typesafe.play"       %% "play-test"                % current                 % "test, it",
    "org.pegdown"             %  "pegdown"                  % "1.6.0"                 % "test, it",
    "org.scalatestplus.play"  %% "scalatestplus-play"       % "3.1.3"                 % "test, it",
    "com.github.tomakehurst"  %  "wiremock-jre8"            % "2.27.2"                % "it",
    "org.mockito"             % "mockito-core"              % "3.7.0"                 % "test, it"
  )

}
