package simulations

import actions.actions.B2BOrderProcessingActions
import actions.scm.CreateB2BOrderPayload._
import io.gatling.core.Predef._
import io.gatling.http.Predef.http

import scala.concurrent.duration._

class B2BTemplate extends Simulation {

  private val random = scala.util.Random


  private val httpProtocol: io.gatling.http.protocol.HttpProtocolBuilder = http
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .disableWarmUp
    .disableCaching


  private val createB2BOrdersScenario = scenario("B2BCreateOrder")
    .feed(b2bMedsFeeder)
    .exec(B2BOrderProcessingActions.createB2BOrders())
    .exec(B2BOrderProcessingActions.fetchOrderId())



  setUp(
    createB2BOrdersScenario.inject(rampUsers(System.getProperty("b2bRampUpUsers", "1").toInt) during (System.getProperty("b2bRampUpDuration", "2").toInt seconds))
  ).protocols(httpProtocol)

}

