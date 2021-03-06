/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services

import org.scalatestplus.play.PlaySpec
import play.api.test.Helpers._
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.SelfEmploymentDataKeys.businessesKey
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.httpparser.{GetSelfEmploymentsHttpParser, PostSelfEmploymentsHttpParser}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.connectors.mocks.MockIncomeTaxSubscriptionConnector
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.models._

class MultipleSelfEmploymentsServiceSpec extends PlaySpec with MockIncomeTaxSubscriptionConnector {

  trait Setup {
    val service: MultipleSelfEmploymentsService = new MultipleSelfEmploymentsService(mockIncomeTaxSubscriptionConnector)
  }

  def businessStartDate(id: String): BusinessStartDate = BusinessStartDate(DateModel(id, "1", "2017"))

  def businessName(id: String): BusinessNameModel = BusinessNameModel(s"ABC Limited $id")

  def businessTrade(id: String): BusinessTradeNameModel = BusinessTradeNameModel(s"Plumbing $id")

  def businessAddress(id: String): BusinessAddressModel = BusinessAddressModel(s"Audit Ref $id", Address(Seq("line1", "line2", "line3"), "TF3 4NT"))

  def fullSelfEmploymentData(id: String): SelfEmploymentData = SelfEmploymentData(
    id = id,
    businessStartDate = Some(businessStartDate(id)),
    businessName = Some(businessName(id)),
    businessTradeName = Some(businessTrade(id)),
    businessAddress = Some(businessAddress(id))
  )

  "findData[T]" must {
    "return the specified data" when {
      "the searched business is present in the returned businesses and has the required data" in new Setup {
        mockGetSelfEmployments[Seq[SelfEmploymentData]](businessesKey)(
          response = Right(Some(Seq(
            fullSelfEmploymentData("1"),
            fullSelfEmploymentData("2")
          )))
        )

        await(service.findData[BusinessNameModel]("1", _.businessName)) mustBe Right(Some(businessName("1")))
        await(service.findData[BusinessNameModel]("2", _.businessName)) mustBe Right(Some(businessName("2")))
      }
    }
    "return no data" when {
      "the searched business is present in the returned businesses but does not have the required data" in new Setup {
        mockGetSelfEmployments[Seq[SelfEmploymentData]](businessesKey)(
          response = Right(Some(Seq(
            fullSelfEmploymentData("1").copy(businessName = None)
          )))
        )

        await(service.findData[BusinessNameModel]("1", _.businessName)) mustBe Right(None)
      }
      "the searched business is not present in the returned businesses" in new Setup {
        mockGetSelfEmployments[Seq[SelfEmploymentData]](businessesKey)(
          response = Right(Some(Seq(
            fullSelfEmploymentData("1")
          )))
        )

        await(service.findData[BusinessNameModel]("2", _.businessName)) mustBe Right(None)
      }
      "no data for the user was returned" in new Setup {
        mockGetSelfEmployments[Seq[SelfEmploymentData]](businessesKey)(
          response = Right(None)
        )

        await(service.findData[BusinessNameModel]("1", _.businessName)) mustBe Right(None)
      }
    }
    "return a failure" when {
      "a failure is returned from the connector" in new Setup {
        mockGetSelfEmployments[Seq[SelfEmploymentData]](businessesKey)(
          response = Left(GetSelfEmploymentsHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR))
        )

        await(service.findData[BusinessNameModel]("1", _.businessName)) mustBe Left(GetSelfEmploymentsHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR))
      }
    }
  }

  "fetchBusinessStartDate" must {
    "return the business start date" when {
      "the business has the business start date" in new Setup {
        mockGetSelfEmployments[Seq[SelfEmploymentData]](businessesKey)(
          response = Right(Some(Seq(
            fullSelfEmploymentData("1")
          )))
        )

        await(service.findData[BusinessStartDate]("1", _.businessStartDate)) mustBe Right(Some(businessStartDate("1")))
      }
    }
  }

  "fetchBusinessName" must {
    "return the business name" when {
      "the business has the business name" in new Setup {
        mockGetSelfEmployments[Seq[SelfEmploymentData]](businessesKey)(
          response = Right(Some(Seq(
            fullSelfEmploymentData("1")
          )))
        )

        await(service.findData[BusinessNameModel]("1", _.businessName)) mustBe Right(Some(businessName("1")))
      }
    }
  }

  "fetchBusinessTrade" must {
    "return the business trade" when {
      "the business has the business trade" in new Setup {
        mockGetSelfEmployments[Seq[SelfEmploymentData]](businessesKey)(
          response = Right(Some(Seq(
            fullSelfEmploymentData("1")
          )))
        )

        await(service.findData[BusinessTradeNameModel]("1", _.businessTradeName)) mustBe Right(Some(businessTrade("1")))
      }
    }
  }

  "fetchBusinessAddress" must {
    "return the business address" when {
      "the business has the business name" in new Setup {
        mockGetSelfEmployments[Seq[SelfEmploymentData]](businessesKey)(
          response = Right(Some(Seq(
            fullSelfEmploymentData("1")
          )))
        )

        await(service.findData[BusinessAddressModel]("1", _.businessAddress)) mustBe Right(Some(businessAddress("1")))
      }
    }
  }

  "saveData" must {
    "return the save result" when {
      "the business is returned with data already present for the field being saved" in new Setup {
        mockGetSelfEmployments[Seq[SelfEmploymentData]](businessesKey)(
          response = Right(Some(Seq(
            fullSelfEmploymentData("1")
          )))
        )
        mockSaveSelfEmployments[Seq[SelfEmploymentData]](
          id = businessesKey,
          value = Seq(fullSelfEmploymentData("1").copy(businessName = Some(businessName("2"))))
        )(Right(PostSelfEmploymentsHttpParser.PostSelfEmploymentsSuccessResponse))

        await(service.saveData("1", _.copy(businessName = Some(businessName("2"))))) mustBe
          Right(PostSelfEmploymentsHttpParser.PostSelfEmploymentsSuccessResponse)
      }
      "the business is returned with no data for the field being saved" in new Setup {
        mockGetSelfEmployments[Seq[SelfEmploymentData]](businessesKey)(
          response = Right(Some(Seq(
            fullSelfEmploymentData("1").copy(businessName = None)
          )))
        )
        mockSaveSelfEmployments[Seq[SelfEmploymentData]](
          id = businessesKey,
          value = Seq(fullSelfEmploymentData("1"))
        )(Right(PostSelfEmploymentsHttpParser.PostSelfEmploymentsSuccessResponse))

        await(service.saveData("1", _.copy(businessName = Some(businessName("1"))))) mustBe
          Right(PostSelfEmploymentsHttpParser.PostSelfEmploymentsSuccessResponse)
      }
      "no business matches the business id to save against" in new Setup {
        mockGetSelfEmployments[Seq[SelfEmploymentData]](businessesKey)(
          response = Right(Some(Seq(
            fullSelfEmploymentData("2")
          )))
        )
        mockSaveSelfEmployments[Seq[SelfEmploymentData]](
          id = businessesKey,
          value = Seq(fullSelfEmploymentData("2"), SelfEmploymentData("1", businessName = Some(businessName("1"))))
        )(Right(PostSelfEmploymentsHttpParser.PostSelfEmploymentsSuccessResponse))

        await(service.saveData("1", _.copy(businessName = Some(businessName("1"))))) mustBe
          Right(PostSelfEmploymentsHttpParser.PostSelfEmploymentsSuccessResponse)
      }
      "there are no businesses currently saved" in new Setup {
        mockGetSelfEmployments[Seq[SelfEmploymentData]](businessesKey)(
          response = Right(None)
        )
        mockSaveSelfEmployments[Seq[SelfEmploymentData]](
          id = businessesKey,
          value = Seq(SelfEmploymentData("1", businessName = Some(businessName("1"))))
        )(Right(PostSelfEmploymentsHttpParser.PostSelfEmploymentsSuccessResponse))

        await(service.saveData("1", _.copy(businessName = Some(businessName("1"))))) mustBe
          Right(PostSelfEmploymentsHttpParser.PostSelfEmploymentsSuccessResponse)
      }
    }
    "return a failure" when {
      "a failure is returned from the save" in new Setup {
        mockGetSelfEmployments[Seq[SelfEmploymentData]](businessesKey)(
          response = Right(None)
        )
        mockSaveSelfEmployments[Seq[SelfEmploymentData]](
          id = businessesKey,
          value = Seq(SelfEmploymentData("1", businessName = Some(businessName("1"))))
        )(Left(PostSelfEmploymentsHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR)))

        await(service.saveData("1", _.copy(businessName = Some(businessName("1"))))) mustBe
          Left(MultipleSelfEmploymentsService.SaveSelfEmploymentDataFailure)
      }
      "a failure is returned from the fetch" in new Setup {
        mockGetSelfEmployments[Seq[SelfEmploymentData]](businessesKey)(
          response = Left(GetSelfEmploymentsHttpParser.UnexpectedStatusFailure(INTERNAL_SERVER_ERROR))
        )
        await(service.saveData("1", _.copy(businessName = Some(businessName("1"))))) mustBe
          Left(MultipleSelfEmploymentsService.SaveSelfEmploymentDataFailure)
      }
    }
  }

  "saveBusinessStartDate" must {
    "return a save success" when {
      "business start date was successfully saved" in new Setup {
        mockGetSelfEmployments[Seq[SelfEmploymentData]](businessesKey)(
          response = Right(None)
        )
        mockSaveSelfEmployments[Seq[SelfEmploymentData]](
          id = businessesKey,
          value = Seq(SelfEmploymentData("1", businessStartDate = Some(businessStartDate("1"))))
        )(Right(PostSelfEmploymentsHttpParser.PostSelfEmploymentsSuccessResponse))

        await(service.saveData("1", _.copy(businessStartDate = Some(businessStartDate("1"))))) mustBe
          Right(PostSelfEmploymentsHttpParser.PostSelfEmploymentsSuccessResponse)
      }
    }
  }

  "saveBusinessName" must {
    "return a save success" when {
      "business name was successfully saved" in new Setup {
        mockGetSelfEmployments[Seq[SelfEmploymentData]](businessesKey)(
          response = Right(None)
        )
        mockSaveSelfEmployments[Seq[SelfEmploymentData]](
          id = businessesKey,
          value = Seq(SelfEmploymentData("1", businessName = Some(businessName("1"))))
        )(Right(PostSelfEmploymentsHttpParser.PostSelfEmploymentsSuccessResponse))

        await(service.saveData("1", _.copy(businessName = Some(businessName("1"))))) mustBe
          Right(PostSelfEmploymentsHttpParser.PostSelfEmploymentsSuccessResponse)
      }
    }
  }

  "saveBusinessTrade" must {
    "return a save success" when {
      "business trade was successfully saved" in new Setup {
        mockGetSelfEmployments[Seq[SelfEmploymentData]](businessesKey)(
          response = Right(None)
        )
        mockSaveSelfEmployments[Seq[SelfEmploymentData]](
          id = businessesKey,
          value = Seq(SelfEmploymentData("1", businessTradeName = Some(businessTrade("1"))))
        )(Right(PostSelfEmploymentsHttpParser.PostSelfEmploymentsSuccessResponse))

        await(service.saveData("1", _.copy(businessTradeName = Some(businessTrade("1"))))) mustBe
          Right(PostSelfEmploymentsHttpParser.PostSelfEmploymentsSuccessResponse)
      }
    }
  }

  "saveBusinessAddress" must {
    "return a save success" when {
      "business address was successfully saved" in new Setup {
        mockGetSelfEmployments[Seq[SelfEmploymentData]](businessesKey)(
          response = Right(None)
        )
        mockSaveSelfEmployments[Seq[SelfEmploymentData]](
          id = businessesKey,
          value = Seq(SelfEmploymentData("1", businessAddress = Some(businessAddress("1"))))
        )(Right(PostSelfEmploymentsHttpParser.PostSelfEmploymentsSuccessResponse))

        await(service.saveData("1", _.copy(businessAddress = Some(businessAddress("1"))))) mustBe
          Right(PostSelfEmploymentsHttpParser.PostSelfEmploymentsSuccessResponse)
      }
    }
  }
}
