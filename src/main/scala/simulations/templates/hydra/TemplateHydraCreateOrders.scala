package simulations.templates.hydra

import java.lang.System._

import actions.HydraOrderProcessingActions._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import simulations.templates.Feeders._

import scala.concurrent.duration._

class TemplateHydraCreateOrders extends io.gatling.core.Predef.Simulation {

  private val protocol = http.disableWarmUp.disableCaching
  private val DEMILITER = "-"
  private val rampUpUsers = getProperty("rampUpUsers", "50").trim.toInt
  private val rampUpDuration = getProperty("rampUpDuration", "10").trim.toInt

  private val fetchOrderDelayStartInSeconds = 5

  private val vector = new java.util.LinkedList[String]

  private val redisClient = new com.redis.RedisClient();

  val createOrder = scenario("Create Order")
    .feed(hydraRetailerIds)
    .feed(hydraMedsFeeder)
    .feed(hydraOrderIdFeeder)
    .exec(createMarketPlaceOrder())
    .exec(session => {
      val id = session("id").asOption[String]
      val orderId = session("orderId").asOption[String]
      if (!id.isEmpty && !orderId.isEmpty) {
        vector.add(id.get + DEMILITER + orderId.get)
      }
      session
    })

  val updateOrder = scenario("Update Order")
    .pause(fetchOrderDelayStartInSeconds second)
    .exec(doIf(session => !vector.isEmpty) {
      exec(session => {
        val id = vector.remove(0)
        val value = id.split(DEMILITER)
        session.set("fetch_id", value(0)).set("fetch_order_id", value(1))
      })
        .exec(GetById())
        .exec(continueNew())
    })

  setUp(
    createOrder.inject(rampUsers(rampUpUsers) during (rampUpDuration)) protocols (protocol),
    updateOrder.inject(rampUsers(rampUpUsers) during (rampUpDuration)) protocols (protocol)
  )

}
