
package connectors.stubs


import helpers.servicemocks.WireMockMethods
import play.api.libs.json.{JsValue, Json}

object IncomeTaxSubscriptionConnectorStub extends WireMockMethods {

  private def selfEmploymentsUri(id: String) = s"/income-tax-subscription/self-employments/id/$id"

  def stubGetSelfEmployments(id: String)(responseStatus: Int, responseBody: JsValue = Json.obj()): Unit = {
    when(
      method = GET,
      uri = selfEmploymentsUri(id)
    ) thenReturn(responseStatus, responseBody)
  }

  def stubSaveSelfEmployments(id: String, body: JsValue = Json.obj())(responseStatus: Int, responseBody: JsValue = Json.obj()): Unit = {
    when (
      method = POST,
      uri = selfEmploymentsUri(id),
      body = body
    ) thenReturn (responseStatus, responseBody)
  }




}
