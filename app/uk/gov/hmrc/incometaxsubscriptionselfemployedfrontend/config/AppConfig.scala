/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config

import javax.inject.{Inject, Singleton}
import play.api.i18n.Lang
import play.api.mvc.Call
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class AppConfig @Inject()(servicesConfig: ServicesConfig) {

  private val assetsUrl = servicesConfig.getString("assets.url")

  protected lazy val contactHost: String = servicesConfig.getString("contact-frontend.host")

  lazy val protectedMicroServiceUrl: String = servicesConfig.baseUrl("income-tax-subscription")
  lazy val allSelfEmployedUrl = s"$protectedMicroServiceUrl/income-tax-subscription/self-employments/all"
  lazy val incomeTaxSubscriptionFrontendBaseUrl: String = servicesConfig.getString("income-tax-subscription-frontend.url")
  lazy val incomeTaxSubscriptionSelfEmployedFrontendBaseUrl: String = servicesConfig.getString("income-tax-subscription-self-employed-frontend.url")
  lazy val feedbackUrl: String = incomeTaxSubscriptionFrontendBaseUrl + "/feedback"

  //  Individual routes
  lazy val howDoYouReceiveYourIncomeUrl: String = incomeTaxSubscriptionFrontendBaseUrl + "/details/income-receive"
  lazy val whatIncomeSourceToSignUpUrl: String = incomeTaxSubscriptionFrontendBaseUrl + "/details/income-source"
  lazy val subscriptionFrontendRoutingController: String = incomeTaxSubscriptionFrontendBaseUrl + "/business/routing"
  lazy val taskListUrl: String = incomeTaxSubscriptionFrontendBaseUrl + "/business/task-list"
  lazy val subscriptionFrontendFinalCYAController: String = incomeTaxSubscriptionFrontendBaseUrl + "/check-your-answers"
  lazy val subscriptionFrontendProgressSavedUrl: String = incomeTaxSubscriptionFrontendBaseUrl + "/business/progress-saved"
  //  Agent routes
  lazy val subscriptionFrontendClientProgressSavedUrl: String = incomeTaxSubscriptionFrontendBaseUrl + "/client/business/progress-saved"
  lazy val subscriptionFrontendClientRoutingController: String = incomeTaxSubscriptionFrontendBaseUrl + "/client/business/routing"
  lazy val subscriptionFrontendClientIncomeUrl: String = incomeTaxSubscriptionFrontendBaseUrl + "/client/income"
  lazy val clientTaskListUrl: String = incomeTaxSubscriptionFrontendBaseUrl + "/client/business/task-list"
  lazy val subscriptionFrontendClientFinalCYAController: String = incomeTaxSubscriptionFrontendBaseUrl + "/client/check-your-answers"

  lazy val ggUrl: String = servicesConfig.getString(s"government-gateway.url")
  lazy val limitOnNumberOfBusinesses: Int = servicesConfig.getInt("check-your-answers.maxNumberOfBusinesses")
  lazy val addressLookupUrl: String = servicesConfig.baseUrl("address-lookup-frontend")
  lazy val timeoutWarningInSeconds: String = servicesConfig.getString("session-timeout.warning")
  lazy val timeoutInSeconds: String = servicesConfig.getString("session-timeout.seconds")

  def addressLookupChangeUrl(id: String): String = s"$addressLookupUrl/lookup-address/$id/lookup"

  val serviceIdentifier = "MTDIT"

  val contactFormServiceIdentifier = "MTDIT"
  val assetsPrefix: String = assetsUrl + servicesConfig.getString("assets.version")
  val analyticsToken: String = servicesConfig.getString(s"google-analytics.token")
  val analyticsHost: String = servicesConfig.getString(s"google-analytics.host")
  val reportAProblemPartialUrl: String = s"$contactHost/contact/problem_reports_ajax?service=$contactFormServiceIdentifier"
  val reportAProblemNonJSUrl: String = s"$contactHost/contact/problem_reports_nonjs?service=$contactFormServiceIdentifier"
  val feedbackFrontendRedirectUrl: String = servicesConfig.getString("feedback-frontend.url")
  val feedbackFrontendRedirectUrlAgent: String = servicesConfig.getString("feedback-frontend.agent.url")
  val urBannerUrl: String = servicesConfig.getString("urBannerUrl.url")

  def ggSignOutUrl(redirectionUrl: String = incomeTaxSubscriptionFrontendBaseUrl): String = s"$ggUrl/bas-gateway/sign-out-without-state?continue=$redirectionUrl"

  def languageMap: Map[String, Lang] = Map(
    "english" -> Lang("en"),
    "cymraeg" -> Lang("cy")
  )

  def betaFeedbackUrl: String = s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier"

  def betaFeedbackUnauthenticatedUrl: String = s"$contactHost/contact/beta-feedback-unauthenticated?service=$contactFormServiceIdentifier"

  def routeToSwitchLanguage(language: String): Call = {
    uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.routes.LanguageSwitchController.switchToLanguage(language)
  }

  def routeToSwitchAgentLanguage(language: String): Call = {
    uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers.agent.routes.AgentLanguageSwitchController.switchToLanguage(language)
  }

}
