package simulations.templates

import java.util.Random

import actions.hydra.HydraOrderCreation
import actions.scm.OrderPayloadCreation
import com.redis.RedisClientPool
import io.gatling.core.Predef._
import io.gatling.redis.Predef.redisFeeder

object Feeders {

  def randonDigitNumber(): Int = {
    return new Random().nextInt(900000) + 100000
  }

  val externalOrderIdfeeder = Iterator.continually(Map("externalOrder" -> s"AutoLoad-${scala.math.abs(java.util.UUID.randomUUID.getMostSignificantBits)}"))
  val b2cMedsFeeder = Iterator.continually(Map("items" -> OrderPayloadCreation.getJsonString()))

  //Hydra
  val hydraRedbookMedsFeeder = Iterator.continually(Map("items" -> HydraOrderCreation.ItemsData()))
  val hydraRedbookOrderIdFeeder = Iterator.continually(Map("orderId" -> randonDigitNumber()))
  val hydraRetailerIds = csv("HydraRetailerIds.csv").eager.circular

  val retailerRedbookId = csv("retailerRedbookId.csv").eager.circular

}
