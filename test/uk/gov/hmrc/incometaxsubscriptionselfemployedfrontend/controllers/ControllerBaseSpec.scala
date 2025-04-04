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

package uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.controllers

import org.apache.pekko.actor.ActorSystem
import org.mockito.Mockito
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, _}
import uk.gov.hmrc.auth.core.{AuthorisationException, InvalidBearerToken}
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.services.mocks.MockAuthService
import uk.gov.hmrc.incometaxsubscriptionselfemployedfrontend.utilities.{ITSASessionKeys, UnitTestTrait}
import scala.language.implicitConversions


trait ControllerBaseSpec extends UnitTestTrait with MockAuthService {

  implicit val system: ActorSystem = ActorSystem()

  val controllerName: String
  val authorisedRoutes: Map[String, Action[AnyContent]]

  lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
    .withHeaders("Content-Type" -> "application/x-www-form-urlencoded")
    .withMethod(POST)

  final def authorisationTests(): Unit = {
    authorisedRoutes.foreach {
      case (name, call) =>
        s"Calling the $name action of the $controllerName with an unauthorised user" should {
          lazy val result = call(FakeRequest())

          "return an AuthorisationException" in {
            Mockito.reset(mockAuthService)

            val exception = InvalidBearerToken()
            mockAuthUnauthorised(exception)

            intercept[AuthorisationException](await(result)) mustBe exception
          }
        }
    }
  }

  implicit class FakeRequestUtil(fakeRequest: FakeRequest[_]) {

    implicit def post[T](form: Form[T], data: T): FakeRequest[AnyContentAsFormUrlEncoded] =
      fakeRequest.post(form.fill(data))

    implicit def postInvalid[T, I](form: Form[T], data: I): FakeRequest[AnyContentAsFormUrlEncoded] =
      fakeRequest.withFormUrlEncodedBody(form.mapping.key -> data.toString)

    implicit def post[T](form: Form[T]): FakeRequest[AnyContentAsFormUrlEncoded] =
      fakeRequest.withFormUrlEncodedBody(form.data.toSeq: _*)
        .withHeaders("Content-Type" -> "application/x-www-form-urlencoded")
        .withMethod(POST)
  }
}
