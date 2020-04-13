package simulations


import actions.OrderProcessingActions
import actions.OrderProcessingActions.addToSession
import actions.scm.OrderPayloadCreation
import io.gatling.core.Predef._
import io.gatling.http.Predef.{http, _}
import org.json4s.DefaultFormats
import newUtilities.newUtilities

import scala.concurrent.duration._

class B2COrders extends io.gatling.core.Predef.Simulation {

  private val random = scala.util.Random
  implicit val formats = DefaultFormats

  private val httpProtocol: io.gatling.http.protocol.HttpProtocolBuilder = http.baseUrl("https://qa.thea.gomercury.in")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .authorizationHeader(TokenGeneration.getDefaultToken())
    .disableWarmUp.disableCaching

  private val externalOrderIdfeeder = Iterator.continually(Map("externalOrder" -> s"AutoLoad-${scala.math.abs(java.util.UUID.randomUUID.getMostSignificantBits)}"))
  private val b2cMedsFeeder = Iterator.continually(Map("items" -> OrderPayloadCreation.getJsonString()))

  private val createB2COrders = scenario("AsynchronousTest")
    .feed(externalOrderIdfeeder)
    .feed(b2cMedsFeeder)
    .exec(http("AsynchronousAPIs")
      .post("/api/outward/orders")
      .body(StringBody(OrderPayloadCreation.getOrderPayload()))
      .check(status.is(200), jsonPath("$..externalOrderId").notNull.saveAs("externalOrderId")))
    .exec(session => {
      newUtilities.writeFile("externalOrderId.csv", session("externalOrderId").as[String] + "\n");
      session
    })
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
    .exec(OrderProcessingActions.signInToPickerApp())
    .exec(session => {
      addToSession(
        session,
        ("token", session("token").as[String])
      )
    })
    .exec(session => {
      addToSession(
        session,
        ("pickerId", session("pickerId").as[String])
      )
    })
    .pause(5, 10)
    .exec(OrderProcessingActions.aggregateAssignedPickerTasks())

  setUp(
    createB2COrders.inject(rampUsers(System.getProperty("b2cRampUpUsers", "1").toInt) during (System.getProperty("b2cRampUpDuration", "2").toInt seconds))
  ).protocols(httpProtocol)
}
