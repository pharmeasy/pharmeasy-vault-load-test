package simulations.templates

import actions.scm.OrderPayloadCreation


object Feeders {

  val externalOrderIdfeeder = Iterator.continually(Map("externalOrder" -> s"AutoLoad-${scala.math.abs(java.util.UUID.randomUUID.getMostSignificantBits)}"))
  val b2cMedsFeeder = Iterator.continually(Map("items" -> OrderPayloadCreation.getJsonString()))

}
