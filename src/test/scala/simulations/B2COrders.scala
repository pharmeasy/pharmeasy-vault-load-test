package simulations


import actions.OrderProcessingActions
import actions.OrderProcessingActions.{addToSession, getFromSession}
import actions.scm.OrderPayloadCreation
import io.gatling.core.Predef._
import io.gatling.http.Predef.{http, _}
import newUtilities.TokenGeneration
import newUtilities.newUtilities
import org.json4s.DefaultFormats

import scala.concurrent.duration._

class B2COrders extends io.gatling.core.Predef.Simulation {

  private val random = scala.util.Random
  implicit val formats = DefaultFormats

  private val httpProtocol: io.gatling.http.protocol.HttpProtocolBuilder = http.baseUrl("https://qa2.thea.gomercury.in")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .authorizationHeader(TokenGeneration.getDefaultToken())
    .disableWarmUp.disableCaching

  private val externalOrderIdfeeder = Iterator.continually(Map("externalOrder" -> s"AL-${scala.math.abs(java.util.UUID.randomUUID.getMostSignificantBits)}"))
  private val b2cMedsFeeder = Iterator.continually(Map("items" -> OrderPayloadCreation.getJsonString()))

  private val createB2COrders = scenario("B2C Order Test")
    .feed(externalOrderIdfeeder)
    .feed(b2cMedsFeeder)
    .feed(OrderPayloadCreation.getTrayListFeeder())
    .feed(OrderPayloadCreation.getPickerListFeeder())
    .exec(http("CreateB2COrder")
      .post("/api/outward/orders")
      .body(StringBody(OrderPayloadCreation.getOrderPayload()))
      .check(status.is(200), jsonPath("$..externalOrderId").notNull.saveAs("externalOrderId")))
    .asLongAsDuring(session => session("pickerTaskId").asOption[String].isEmpty, (10 seconds)) {
      exec(OrderProcessingActions.getPickerTaskFromEpicenter())
    }
    .exitHereIfFailed
    .exec(OrderProcessingActions.prioritisePickerTask())
    .exec(OrderProcessingActions.configureMultiPicking())
    .exec(session => addToSession(session, ("aggregatePickerTaskCount", "0")))
    .asLongAsDuring(session => session("aggregatePickerTaskCount").as[String].equals("0"), (10 seconds)) {
      exec(OrderProcessingActions.aggregateAssignedPickerTasks())
    }
    .asLongAsDuring(session => session("aggregatedPickerTaskId").asOption[String].isEmpty, (10 seconds)) {
      exec(OrderProcessingActions.pickTray())
    }.exitHereIfFailed
    .exec(OrderProcessingActions.aggregatePickerTaskPicked())
    .exec(OrderProcessingActions.searchInventoryPostTaskPicked())
    .exitHereIfFailed
    .exec(OrderProcessingActions.getBarcodes())
    .exec(OrderProcessingActions.pickedItems())
    .exitHereIfFailed
    .exec(OrderProcessingActions.completePickedItems())
    .exitHereIfFailed
    .exec(OrderProcessingActions.scanZone())
    .exec(OrderProcessingActions.generateBill())
  //  .exec(OrderProcessingActions.logoutFromPickerApp())
  //  .exec(session => addToSession(session, ("biller_token", TokenGeneration.getBillerToken())))
  //    .exec(OrderProcessingActions.generateStoreInvoice())
  //    .exec(OrderProcessingActions.generateDispatchNote())
  //    .exec(OrderProcessingActions.recievedAtStore())
  //    .exec(OrderProcessingActions.generateCustomerInvoice())

  setUp(
    createB2COrders.inject(rampUsers(System.getProperty("b2cRampUpUsers", "1").toInt) during (System.getProperty("b2cRampUpDuration", "1").toInt seconds))
  ).protocols(httpProtocol)
}
