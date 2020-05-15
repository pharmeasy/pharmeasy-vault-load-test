package simulations.templates.redbook

import java.lang.System._

import actions.RedbookOrderProcessingActions._
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import simulations.templates.Feeders._

import scala.concurrent.duration._

class TemplateRedbookMarketplaceOrderProcess extends io.gatling.core.Predef.Simulation{

  private val protocol = http.disableWarmUp.disableCaching
  private val DEMILITER = "-"
  private val rampUpUsers = getProperty("rampUpUsers", "10").trim.toInt
  private val rampUpDuration = getProperty("rampUpDuration", "5").trim.toInt

  private val fetchOrderDelayStartInSeconds = 2

  private val queue = new java.util.LinkedList[String]

  val createOrder = scenario("Create Order at Redbook")
    .feed(retailerRedbookId)
    .feed(hydraRedbookMedsFeeder)
    .feed(hydraRedbookOrderIdFeeder)
    .exec(createOrderInRedBook())
    .exec(session => {
      val redbookId = session("reference_order_id").asOption[String]
      val retailerId = session("app_id").asOption[String]
      if (!redbookId.isEmpty && !retailerId.isEmpty) {
        queue.add(redbookId.get + DEMILITER + retailerId.get)
      }
      session
    })


  val updateOrder = scenario("Update Order")
    .pause(fetchOrderDelayStartInSeconds second)
    .exec(doIf(session => !queue.isEmpty) {
      exec(session => {
        val id = queue.remove(0)
        val value = id.split(DEMILITER)
        session.set("fetch_reference_order_id", value(0)).set("fetch_app_id", value(1))
      })
        .exec(getOrderByPEId())
        .exec(statusUpdates())
    })

  setUp(
    createOrder.inject(rampUsers(rampUpUsers) during (rampUpDuration)) protocols (protocol),
    updateOrder.inject(rampUsers(rampUpUsers) during (rampUpDuration)) protocols (protocol)
  )

}
