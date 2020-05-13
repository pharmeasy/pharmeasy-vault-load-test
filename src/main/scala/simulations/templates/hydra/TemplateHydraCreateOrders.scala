package simulations.templates.hydra

import java.lang.System._

import actions.{HydraOrderProcessingActions}
import io.gatling.core.Predef._
import simulations.templates.Feeders


class TemplateHydraCreateOrders extends io.gatling.core.Predef.Simulation{

  private val rampUpUsers = getProperty("rampUpUsers", "1").trim.toInt
  private val rampUpDuration = getProperty("rampUpDuration", "2").trim.toInt

   def name() = "MarketPlaceOrder"

  val scn = scenario(name)
    .feed(Feeders.HydraRetailerIds)
    .feed(Feeders.HydraMedsFeeder)
    .feed(Feeders.HydraOrderIdFeeder)
    .exec(HydraOrderProcessingActions.CreateMarketPlaceOrder())
    .exec(HydraOrderProcessingActions.GetById())
    .exec(HydraOrderProcessingActions.continueNew())

  setUp(
    scn.inject(rampUsers(rampUpUsers) during (rampUpDuration))
  )

}
