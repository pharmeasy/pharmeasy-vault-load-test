package actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.ConfigManager._

object OrderProcessingActions extends Payloads {

  def addToSession(session: Session, attributes: (String, Any)*) = session.setAll(attributes)

  def CreateB2BOrders(baseUrl: String = getString("outward.base_url")) =
    http("Create B2C Order")
      .post(session => baseUrl + "/api/outward/orders")
      .body(StringBody(payload))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.orderGroupId").notNull)
}
