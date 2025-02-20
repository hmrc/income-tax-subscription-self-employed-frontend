/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, when}
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models.ClientDetails
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.ClientDetailsRetrieval

import scala.concurrent.Future

trait MockClientDetailsRetrieval extends MockitoSugar with BeforeAndAfterEach {
  suite: Suite =>

  val mockClientDetailsRetrieval: ClientDetailsRetrieval = mock[ClientDetailsRetrieval]
  val testName: String = "FirstName LastName"
  val testNino: String = "AA111111A"

  val clientDetails: ClientDetails = ClientDetails(testName, testNino)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockClientDetailsRetrieval)

    mockGetClientDetails()
  }

  def mockGetClientDetails(): Unit = {
    when(mockClientDetailsRetrieval.getClientDetails(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(
      Future.successful(clientDetails)
    )
  }

}
