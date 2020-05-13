package simulations.templates.scm

import java.lang.System._

import actions.OrderProcessingActions
import io.gatling.core.Predef._
import simulations.templates.{ Feeders}


class TemplateB2COrders extends io.gatling.core.Predef.Simulation{

  private val rampUpUsers = getProperty("rampUpUsers", "1").trim.toInt
  private val rampUpDuration = getProperty("rampUpDuration", "2").trim.toInt

   def name() = "B2COrders"

  val scn = scenario(name)
    .feed(Feeders.externalOrderIdfeeder)
    .feed(Feeders.b2cMedsFeeder)
    .exec(OrderProcessingActions.CreateB2COrders())

  setUp(
    scn.inject(rampUsers(rampUpUsers) during (rampUpDuration))
  )

}
