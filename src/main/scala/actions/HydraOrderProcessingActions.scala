package actions

import actions.hydra.{HydraOrderCreation, HydraOrderUpdate}
import io.gatling.core.Predef.{jsonPath, _}
import io.gatling.core.session.Session
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import newUtilities.{TokenGeneration, newConfigManager}
import org.json4s.DefaultFormats

object HydraOrderProcessingActions {

  protected def getFromSession(session: Session, key: String, defaultValue: String = null): String = session(key).asOption[String].getOrElse(defaultValue)

  def addToSession(session: Session, attributes: (String, Any)*) = session.setAll(attributes)

  implicit val formats = DefaultFormats

  def createMarketPlaceOrder(baseUrl: String = newConfigManager.getString("hydra.base_url")) =
    http("create hydra order")
      .post(  baseUrl + "/orders")
      .header("Authorization", TokenGeneration.getDefaultToken())
      .body(StringBody(HydraOrderCreation.getOrderPayload()))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("CREATED"),
        jsonPath("$.retailerOrderId").saveAs("retailerOrderId"),
        jsonPath("$.orderId").saveAs("orderId"),
        jsonPath("$.id").saveAs("id"))


  def getOrderDetails(baseUrl: String = newConfigManager.getString("hydra.base_url")) =
    http("get hydra order by id")
      .get(session => baseUrl + "/orders/" + getFromSession(session, "fetch_order_id"))
      .header("Authorization", TokenGeneration.getDefaultToken())
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").saveAs("status"))

  def getById(baseUrl: String = newConfigManager.getString("hydra.base_url")) =
    http("get hydra order by id")
      .get(session => baseUrl + "/orders/gateway/" + getFromSession(session, "fetch_id"))
      .header("Authorization", TokenGeneration.getDefaultToken())
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").saveAs("status"))

  def updateOrder(baseUrl: String = newConfigManager.getString("hydra.base_url"), body: String) =
    http("update hydra order")
      .post(session => baseUrl + "/orders/rb/" + getFromSession(session, "fetch_order_id") + "/status")
      .header("Authorization", TokenGeneration.getDefaultToken())
      .body(StringBody(body))
      .asJson
      .check(
        status.is(200))

  val statusCount = new java.util.concurrent.ConcurrentHashMap[String, Long]()

  def update(baseUrl: String = newConfigManager.getString("hydra.base_url"), updatePayload: String, key: String) = {
    exec(updateOrder(baseUrl, updatePayload))
      .exec(session => {
        statusCount.put(key, if (statusCount.containsKey(key)) (statusCount.get(key) + 1) else 1)
        session
      }).exitHereIfFailed.exec(getById())
  }

  def statusUpdates(baseUrl: String = newConfigManager.getString("hydra.base_url")): ChainBuilder = {
    doWhile(session => !(getFromSession(session, "status").equals("CANCELLED")) &&
      !(getFromSession(session, "status").equals("DELIVERED")))(
      doSwitch(session => getFromSession(session, "status"))(
        "CREATED" -> randomSwitch(
          90.0 -> update(baseUrl, HydraOrderUpdate.getAccepted(), "created-accepted"),
          5.0 -> update(baseUrl, HydraOrderUpdate.getRejected(), "created-rejected"),
          5.0 -> update(baseUrl, HydraOrderUpdate.getCancelled(), "created-cancelled"),
        ),
        "ACCEPTED" -> randomSwitch(
          70.0 -> update(baseUrl, HydraOrderUpdate.getBilled(), "accepted-billed"),
          20.0 -> update(baseUrl, HydraOrderUpdate.getOnHold(), "accepted-on_hold"),
          10.0 -> update(baseUrl, HydraOrderUpdate.getCancelled(), "accepted-cancelled"),
        ),
        "BILLED" -> randomSwitch(
          80.0 -> update(baseUrl, HydraOrderUpdate.getRFD(), "billed-rfd"),
          20.0 -> update(baseUrl, HydraOrderUpdate.getCancelled(), "billed-cancelled"),
        ),
        "ON_HOLD" -> randomSwitch(
          80.0 -> update(baseUrl, HydraOrderUpdate.getBilled(), "on_hold-billed"),
          20.0 -> update(baseUrl, HydraOrderUpdate.getCancelled(), "on_hold-cancelled"),
        ),
        "READY_FOR_DISPATCH" -> update(baseUrl, HydraOrderUpdate.getDelivered(), "ready_for_dispatch-delivered")
      )
    )
  }
}
