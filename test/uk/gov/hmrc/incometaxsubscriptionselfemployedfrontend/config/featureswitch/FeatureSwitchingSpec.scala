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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch

import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.Configuration
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.AppConfig
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.config.featureswitch.FeatureSwitch.{EnableUseRealAddressLookup, switches}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.UnitTestTrait
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class FeatureSwitchingSpec extends UnitTestTrait with FeatureSwitching {

  class Setup(sarEnabled: Boolean = false, alEnabled: Boolean = false) extends FeatureSwitching {

    val servicesConfig: ServicesConfig = app.injector.instanceOf[ServicesConfig]
    val mockConfig: Configuration = mock[Configuration]

    if(sarEnabled) {
      when(mockConfig.getOptional[String]("feature-switch.enable-save-and-retrieve")).thenReturn(Some(FEATURE_SWITCH_ON))
    } else
      when(mockConfig.getOptional[String]("feature-switch.enable-save-and-retrieve")).thenReturn(Some(FEATURE_SWITCH_OFF))

    if(alEnabled)
      when(mockConfig.getOptional[String]("feature-switch.enable-use-real-AL")).thenReturn(Some(FEATURE_SWITCH_ON))
    else
      when(mockConfig.getOptional[String]("feature-switch.enable-use-real-AL")).thenReturn(Some(FEATURE_SWITCH_OFF))

    override val appConfig: AppConfig = new AppConfig(servicesConfig, mockConfig)

    FeatureSwitch.switches foreach { switch =>
      sys.props -= switch.name
    }
  }

  "FeatureSwitch switches" should {
    "contain all the feature switches in the app" in new Setup {
      FeatureSwitch.switches mustBe switches
    }
  }

  "FeatureSwitching constants" should {
    "be true and false" in new Setup {
      FEATURE_SWITCH_ON mustBe "true"
      FEATURE_SWITCH_OFF mustBe "false"
    }
  }

  "EnableUseRealAddressLookup" should {
    "return true if EnableUseRealAddressLookup feature switch is enabled" in new Setup {
      enable(EnableUseRealAddressLookup)
      isEnabled(EnableUseRealAddressLookup) mustBe true
    }
    "return false if EnableUseRealAddressLookup feature switch is disabled" in new Setup {
      disable(EnableUseRealAddressLookup)
      isEnabled(EnableUseRealAddressLookup) mustBe false
    }
    "return false if EnableUseRealAddressLookup feature switch does not exist" in new Setup {
      isEnabled(EnableUseRealAddressLookup) mustBe false
    }
    "return false if EnableUseRealAddressLookup feature switch is not in sys.props but is set to off in config" in new Setup {
      isEnabled(EnableUseRealAddressLookup) mustBe false
    }
    "return true if EnableUseRealAddressLookup feature switch is not in sys.props but is set to on in config" in new Setup(alEnabled = true) {
      isEnabled(EnableUseRealAddressLookup) mustBe true
    }
  }

}
