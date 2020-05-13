package actions

import actions.ConsumerActions.getFromSession
import actions.hydra.HydraOrderCreation.order
import actions.hydra.{HydraOrderCreation, HydraUpdate}
import io.gatling.core.Predef.{jsonPath, _}
import newUtilities.{TokenGeneration, newConfigManager}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.core.session.Session
import org.json4s.native.Serialization.write

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
        jsonPath("$.status").is("CREATED"),
        jsonPath("$.retailerOrderId").saveAs("retailerOrderId"))


  def GetOrderById(baseUrl: String = newConfigManager.getString("hydra.base_url")) =
    http("Create Hydra Order")
      .get(session => baseUrl + "/orders/"+ getFromSession(session,"retailerOrderId"))
      .header("accept","application/json")
      .header("contentType","application/json")
      .header("Authorization",TokenGeneration.getDefaultToken())
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").saveAs("status"))


  def UpdateOrder(baseUrl: String = newConfigManager.getString("hydra.base_url"),
                  orderId: String,hydraUpdate: HydraUpdate) =
    http("Create Hydra Order")
      .get(session => baseUrl + "/orders/rb/"+orderId+"/status")
      .header("accept","application/json")
      .header("contentType","application/json")
      .header("Authorization",TokenGeneration.getDefaultToken())
      .body(write(hydraUpdate))
      .asJson
      .check(
        status.is(200))



}
