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
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.GetSessionDataHttpParser.UnexpectedStatusFailure
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.SessionDataService

import scala.concurrent.Future

trait MockSessionDataService extends MockitoSugar with BeforeAndAfterEach {
  suite: Suite =>

  val mockSessionDataService: SessionDataService = mock[SessionDataService]

  val testReference: String = "test-reference"

  override def beforeEach(): Unit = {
    reset(mockSessionDataService)
    super.beforeEach()

    mockFetchReferenceSuccess(Some(testReference))
  }

  def mockFetchReferenceSuccess(reference: Option[String]): Unit = {
    when(mockSessionDataService.fetchReference(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Right(reference)))
  }

  def mockFetchReferenceFailure(status: Int): Unit = {
    when(mockSessionDataService.fetchReference(ArgumentMatchers.any()))
      .thenReturn(Future.successful(Left(UnexpectedStatusFailure(status))))
  }

}
