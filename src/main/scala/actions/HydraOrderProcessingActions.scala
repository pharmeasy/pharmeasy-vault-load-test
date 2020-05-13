package actions

import actions.hydra.{HydraOrderCreation, HydraOrderUpdate}
import io.gatling.core.Predef.{jsonPath, _}
import io.gatling.core.session.Session
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import newUtilities.{TokenGeneration, newConfigManager}
import org.json4s.DefaultFormats
import simulations.templates.Feeders

object HydraOrderProcessingActions {

  protected def getFromSession(session: Session, key: String, defaultValue: String = null): String = session(key).asOption[String].getOrElse(defaultValue)

  def addToSession(session: Session, attributes: (String, Any)*) = session.setAll(attributes)

  implicit val formats = DefaultFormats

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
        jsonPath("$.retailerOrderId").saveAs("retailerOrderId"),
        jsonPath("$.orderId").saveAs("orderId"),
        jsonPath("$.id").saveAs("id"))


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

  def GetById(baseUrl: String = newConfigManager.getString("hydra.base_url")) =
    http("Create Hydra Order")
      .get(session => baseUrl + "/orders/gateway/" + getFromSession(session, "fetch_id"))
      .header("Authorization", TokenGeneration.getDefaultToken())
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").saveAs("status"))

  def updateOrder(baseUrl: String = newConfigManager.getString("hydra.base_url"), body: String) =
    http("Update Hydra Order")
      .post(session => baseUrl + "/orders/rb/" + getFromSession(session, "fetch_order_id") + "/status")
      .header("Authorization", TokenGeneration.getDefaultToken())
      .body(StringBody(body))
      .asJson
      .check(
        status.is(200))

  //  def continue(baseUrl: String = newConfigManager.getString("hydra.base_url")) = {
  //    doWhile(session => !(getFromSession(session, "status").equals("CANCELLED")) &&
  //      !(getFromSession(session, "status").equals("DELIVERED")))(
  //      doSwitch(session => getFromSession(session, "status"))(
  //        "CREATED" -> exec(updateOrder(baseUrl, HydraOrderUpdate.getRandomUpdateScenario())).exitHereIfFailed.exec(GetById()),
  //        "ACCEPTED" -> exec(updateOrder(baseUrl, HydraOrderUpdate.getRandomUpdateAfterAcceptScenario())).exitHereIfFailed.exec(GetById()),
  //        "BILLED" -> exec(updateOrder(baseUrl, HydraOrderUpdate.getRandomUpdateAfterBilledScenarios())).exitHereIfFailed.exec(GetById()),
  //        "ON_HOLD" -> exec(updateOrder(baseUrl, HydraOrderUpdate.getRandomUpdateAfterOnHoldScenarios())).exitHereIfFailed.exec(GetById()),
  //        "READY_FOR_DISPATCH" -> exec(updateOrder(baseUrl, HydraOrderUpdate.getDelivered))).exitHereIfFailed.exec(GetById())
  //    )
  //  }

  def Update(baseUrl: String = newConfigManager.getString("hydra.base_url"), updatePayload: String): ChainBuilder = {
    return exec(updateOrder(baseUrl, updatePayload)).exitHereIfFailed.exec(GetById())
  }

  def continueNew(baseUrl: String = newConfigManager.getString("hydra.base_url")) = {
    doWhile(session => !(getFromSession(session, "status").equals("CANCELLED")) &&
      !(getFromSession(session, "status").equals("DELIVERED")))(
      doSwitch(session => getFromSession(session, "status"))(
        "CREATED" -> randomSwitch(
          90.0 -> Update(baseUrl, HydraOrderUpdate.getAccepted()),
          5.0 -> Update(baseUrl, HydraOrderUpdate.getRejected()),
          5.0 -> Update(baseUrl, HydraOrderUpdate.getCancelled()),
        ),
        "ACCEPTED" -> randomSwitch(
          70.0 -> Update(baseUrl, HydraOrderUpdate.getBilled()),
          20.0 -> Update(baseUrl, HydraOrderUpdate.getOnHold()),
          10.0 -> Update(baseUrl, HydraOrderUpdate.getCancelled()),
        ),
        "BILLED" -> randomSwitch(
          80.0 -> Update(baseUrl, HydraOrderUpdate.getRFD()),
          20.0 -> Update(baseUrl, HydraOrderUpdate.getCancelled()),
        ),
        "ON_HOLD" -> randomSwitch(
          80.0 -> Update(baseUrl, HydraOrderUpdate.getBilled()),
          20.0 -> Update(baseUrl, HydraOrderUpdate.getCancelled()),
        ),
        "READY_FOR_DISPATCH" -> Update(baseUrl, HydraOrderUpdate.getDelivered())
      )
    )
  }
}
