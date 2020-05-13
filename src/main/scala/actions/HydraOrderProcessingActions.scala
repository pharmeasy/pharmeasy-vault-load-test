package actions

import actions.hydra.{HydraOrderCreation, HydraOrderUpdate, HydraUpdate}
import io.gatling.core.Predef.{jsonPath, _}
import io.gatling.core.session.Session
import io.gatling.http.Predef._
import newUtilities.{TokenGeneration, newConfigManager}
import simulations.templates.Feeders

object HydraOrderProcessingActions {

  protected def getFromSession(session: Session, key: String, defaultValue: String = null): String = session(key).asOption[String].getOrElse(defaultValue)

  def addToSession(session: Session, attributes: (String, Any)*) = session.setAll(attributes)

  def createMarketPlaceOrder(baseUrl: String = newConfigManager.getString("hydra.base_url")) =
    http("Create Hydra Order")
      .post(session => baseUrl + "/orders")
      .header("accept", "application/json")
      .header("contentType", "application/json")
      .header("Authorization", TokenGeneration.getDefaultToken())
      .body(StringBody(HydraOrderCreation.getOrderPayload()))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("CREATED"),
        jsonPath("$.retailerOrderId").saveAs("retailerOrderId"))


  def getOrderById(baseUrl: String = newConfigManager.getString("hydra.base_url")) =
    http("Fetch Hydra Order")
      .get(session => baseUrl + "/orders/" + getFromSession(session, Feeders.fetchKey))
      .header("accept", "application/json")
      .header("contentType", "application/json")
      .header("Authorization", TokenGeneration.getDefaultToken())
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").saveAs("status"))


  def updateOrder(baseUrl: String = newConfigManager.getString("hydra.base_url"),
                  orderId: String, hydraUpdate: HydraUpdate) =
    http("Update Hydra Order")
      .get(session => baseUrl + "/orders/rb/" + getFromSession(session, "retailerOrderId") + "/status")
      .header("accept", "application/json")
      .header("contentType", "application/json")
      .header("Authorization", TokenGeneration.getDefaultToken())
      .body(StringBody(HydraOrderUpdate.getRandomUpdateScenario()))
      .asJson
      .check(
        status.is(200))
}
