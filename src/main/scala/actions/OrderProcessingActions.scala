package actions

import actions.scm.OrderPayloadCreation
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import newUtilities.{TokenGeneration, newConfigManager}

object OrderProcessingActions  {

  protected def getFromSession(session: Session, key: String, defaultValue: String = null): String = session(key).asOption[String].getOrElse(defaultValue)

  def addToSession(session: Session, attributes: (String, Any)*) = session.setAll(attributes)

  def CreateB2BOrders(baseUrl: String = newConfigManager.getString("outward.base_url")) =
    http("Create B2C Order")
      .post(session => baseUrl + "/api/outward/orders")
      .header("accept","application/json")
      .header("contentType","application/json")
      .header("Authorization",TokenGeneration.getDefaultToken())
      .body(StringBody(OrderPayloadCreation.getOrderPayload()))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.orderGroupId").notNull)
}
