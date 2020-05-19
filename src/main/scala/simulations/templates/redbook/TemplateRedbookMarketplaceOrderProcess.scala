package simulations.templates.redbook

import java.lang.System._

import actions.RedbookOrderProcessingActions._
import actions.redbook.Medicines
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import simulations.templates.Feeders._
import utils.Utilities

import scala.concurrent.duration._

class TemplateRedbookMarketplaceOrderProcess extends io.gatling.core.Predef.Simulation{

  private val protocol = http.disableWarmUp.disableCaching
  private val DEMILITER = "-"
  private val rampUpUsers = getProperty("rampUpUsers", "400").trim.toInt
  private val rampUpDuration = getProperty("rampUpDuration", "60").trim.toInt

  private val fetchOrderDelayStartInSeconds = 3

  private val queue = new java.util.LinkedList[String]
  private var hashMap=
    scala.collection.mutable.Map[String,Array[Medicines]]()

  val createOrder = scenario("Create Order at Redbook")
    .feed(retailerRedbookId)
    .feed(hydraRedbookOrderIdFeeder)
    .feed(redbookMedsFeeder)
    .exec(createOrderInRedBook())
    .exec(session => {
      val redbookId = session("reference_order_id").as[String]
      val retailerId = session("app_id").as[String]
      val meds = Utilities.deserializeJson[Array[Medicines]](session("meds").as[String])
      if (!redbookId.isEmpty && !retailerId.isEmpty) {
        queue.add(redbookId + DEMILITER + retailerId)
        hashMap(redbookId.toString)=meds
      }
      session
    }).exec(session => {
    println("99999>> "+queue.get(0))
    session
  })


  val updateOrder = scenario("Update Order")
    .pause(fetchOrderDelayStartInSeconds second)
    .exec(doIf(session => !queue.isEmpty) {
      exec(session => {
        val id = queue.remove(0)
        val value = id.split(DEMILITER)
        session.set("fetch_reference_order_id", value(0))
          .set("fetch_app_id", value(1))
          .set("fetch_meds",hashMap(value(0)))
      })
        .exec(getOrderByPEId())
        .exec(statusUpdates())
    })

  setUp(
    createOrder.inject(rampUsers(rampUpUsers) during (rampUpDuration)) protocols (protocol),
    updateOrder.inject(rampUsers(rampUpUsers) during (rampUpDuration)) protocols (protocol)
  )

}
