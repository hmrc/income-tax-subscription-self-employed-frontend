
package helpers

import play.api.libs.json.{JsValue, Json}


object IntegrationTestConstants {

  lazy val id: String = "AA111111A"

  lazy val testString = ""


  object Auth {
    def idsResponseJson(internalId: String, externalId: String): JsValue = Json.parse(
      s"""{
           "internalId":"$internalId",
           "externalId":"$externalId"
        }""")
  }

}
