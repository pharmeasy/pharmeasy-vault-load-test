package simulations.templates

import java.util.Random
import io.gatling.core.Predef._

import actions.hydra.HydraOrderCreation
import actions.scm.OrderPayloadCreation


object Feeders {

  def randonDigitNumber(): Int ={
    return new Random().nextInt(900000) + 100000
  }

  val externalOrderIdfeeder = Iterator.continually(Map("externalOrder" -> s"AutoLoad-${scala.math.abs(java.util.UUID.randomUUID.getMostSignificantBits)}"))
  val b2cMedsFeeder = Iterator.continually(Map("items" -> OrderPayloadCreation.getJsonString()))

  //Hydra
  val HydraMedsFeeder = Iterator.continually(Map("items" -> HydraOrderCreation.ItemsData()))
  val HydraOrderIdFeeder = Iterator.continually(Map("orderId" -> randonDigitNumber()))
  val HydraRetailerIds= csv("HydraRetailerIds.csv").eager.circular



}
