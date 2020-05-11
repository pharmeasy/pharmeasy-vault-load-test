package actions

import actions.hydra.HydraOrderCreation
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import newUtilities.{TokenGeneration, newConfigManager}

object HydraOrderProcessingActions {

  protected def getFromSession(session: Session, key: String, defaultValue: String = null): String = session(key).asOption[String].getOrElse(defaultValue)

  def addToSession(session: Session, attributes: (String, Any)*) = session.setAll(attributes)

  def CreateMarketPlaceOrder(baseUrl: String = newConfigManager.getString("hydra.base_url")) =
    http("Create Hydra Order")
      .post(session => baseUrl + "/orders")
      .header("accept","application/json")
      .header("contentType","application/json")
      .header("Authorization",TokenGeneration.getDefaultToken())
      .body(StringBody(HydraOrderCreation.getOrderPayload()))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.retailerOrderId").notNull,
        jsonPath("$.status").is("CREATED"))


}
