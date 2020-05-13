package simulations


import actions.OrderProcessingActions
import actions.OrderProcessingActions.addToSession
import actions.scm.OrderPayloadCreation
import io.gatling.core.Predef._
import io.gatling.http.Predef.{http, _}
import newUtilities.TokenGeneration
import newUtilities.newUtilities
import org.json4s.DefaultFormats

import scala.concurrent.duration._

class B2COrders extends io.gatling.core.Predef.Simulation {

  private val random = scala.util.Random
  private val trayCount = newUtilities.inc()
  implicit val formats = DefaultFormats

  private val httpProtocol: io.gatling.http.protocol.HttpProtocolBuilder = http.baseUrl("https://staging.thea.gomercury.in")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .authorizationHeader(TokenGeneration.getDefaultToken())
    .disableWarmUp.disableCaching

  private val externalOrderIdfeeder = Iterator.continually(Map("externalOrder" -> s"AL-${scala.math.abs(java.util.UUID.randomUUID.getMostSignificantBits)}"))
  private val b2cMedsFeeder = Iterator.continually(Map("items" -> OrderPayloadCreation.getJsonString()))


  private val createB2COrders = scenario("AsynchronousTest")
    .feed(externalOrderIdfeeder)
    .feed(b2cMedsFeeder)
    .exec(http("AsynchronousAPIs")
      .post("/api/outward/orders")
      .body(StringBody(OrderPayloadCreation.getOrderPayload()))
      .check(status.is(200), jsonPath("$..externalOrderId").notNull.saveAs("externalOrderId")))
    .exec(session => {
      addToSession(
        session,
       ("externalOrderId", session("externalOrderId").as[String])
      )
    })

    .exec(OrderProcessingActions.configureMultiPicking())
    .exec(OrderProcessingActions.getPickerTaskFromEpicenter())
    .exec(session => {
      addToSession(
        session,
        ("pickerTaskId", session("pickerTaskId").as[String])
      )
    })
    .exec(OrderProcessingActions.prioritisePickerTask())
    .pause(5, 10)
    .exec(OrderProcessingActions.signInToPickerApp())
    .exec(session => {
      addToSession(
        session, ("token", session("token").as[String])
      )
    })
    .exec(session => {
      addToSession(
        session,
        ("pickerId", session("pickerId").as[String])
      )
    })
    .exec(session => {
      addToSession(
        session,
        ("trayId", OrderPayloadCreation.getTray(trayCount))
      )
    })
    .pause(5, 10)
    .exec(OrderProcessingActions.aggregateAssignedPickerTasks())
    .pause(5, 10)
    .exec(OrderProcessingActions.pickTray())
    .exec(session => addToSession(session, ("aggregatedPickerTaskId", session("aggregatedPickerTaskId").as[String])))
    .exec(OrderProcessingActions.aggregatePickerTaskPicked())
    .exec(session => addToSession(session, ("ucode", session("ucode").as[String])))
    .exec(session => addToSession(session, ("bin", session("bin").as[String])))
    .exec(OrderProcessingActions.searchInventoryPostTaskPicked())
    .exec(session => addToSession(session, ("batch", session("batch").as[String])))
    .exec(OrderProcessingActions.getBarcodes())
    .exec(session => addToSession(session, ("barcodes", session("barcodes").as[String])))
    .exec(OrderProcessingActions.pickedItems())
    .exec(OrderProcessingActions.completePickedItems())
    .exec(OrderProcessingActions.scanZone())
    .exec(OrderProcessingActions.generateBill())
    .exec(session => addToSession(session, ("biller_token", TokenGeneration.getBillerToken())))
    .exec(OrderProcessingActions.generateStoreInvoice())
    .exec(OrderProcessingActions.generateDispatchNote())
    .exec(OrderProcessingActions.recievedAtStore())
    .exec(OrderProcessingActions.generateCustomerInvoice())

  setUp(
    createB2COrders.inject(rampUsers(System.getProperty("b2cRampUpUsers", "1").toInt) during (System.getProperty("b2cRampUpDuration", "2").toInt seconds))
  ).protocols(httpProtocol)
}
