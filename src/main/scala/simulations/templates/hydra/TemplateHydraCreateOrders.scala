package simulations.templates.hydra

import java.lang.System._

import actions.HydraOrderProcessingActions._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import simulations.templates.Feeders._

import scala.concurrent.duration._


class TemplateHydraCreateOrders extends io.gatling.core.Predef.Simulation {

  private val protocol = http.disableWarmUp.disableCaching

  private val rampUpUsers = getProperty("rampUpUsers", "10").trim.toInt
  private val rampUpDuration = getProperty("rampUpDuration", "10").trim.toInt

  private val fetchOrderdelayStartInSeconds = 2

  private val redisClient = new com.redis.RedisClient();

  val createOrder = scenario("Create Order")
    .feed(hydraRetailerIds)
    .feed(hydraMedsFeeder)
    .feed(hydraOrderIdFeeder)
    .exec(createMarketPlaceOrder())
    .exec(session => {
      val value = session(redisKey).as[String]
      if (value != null) {
        redisClient.lpush(redisKey, value)
      }
      session
    })

  val fetchOrder = scenario("Fetch Order")
    .pause(fetchOrderdelayStartInSeconds second)
    .exec(doIf(session => redisClient.llen(redisKey).get > 0) {
      exec(session => session.set(fetchKey, redisClient.rpop(redisKey).get))
        .exec(getOrderById())
    })

  val updateOrder = scenario("Update Order")

  setUp(
    createOrder.inject(rampUsers(rampUpUsers) during (rampUpDuration)) protocols (protocol),
    fetchOrder.inject(rampUsers(rampUpUsers) during (rampUpDuration)) protocols (protocol)
  )

}
