package simulations


import java.lang.System._
import java.util

import actions.OrderProcessingActions._
import actions.scm.OrderPayloadCreation
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import newUtilities.{TokenGeneration, newConfigManager}
import org.json4s.DefaultFormats

import scala.concurrent.duration._

class B2COrders extends io.gatling.core.Predef.Simulation {

  private val random = scala.util.Random
  implicit val formats = DefaultFormats
  private val rampUpCreationUsers = getProperty("rampUpCreationUsers", "1").trim.toInt
  private val rampUpUsers = getProperty("rampUpUsers", "1").trim.toInt
  private val rampUpDuration = getProperty("rampUpDuration", "5").trim.toInt
  private val maxProcessCount = getProperty("maxOrdersProcessCount", "5").trim.toInt

  private val DELIMITER = "::"

  private val fetchOrderDelayStartInSeconds = 5

  private val queue = new java.util.LinkedList[String]
  private val pickerTasks = new util.LinkedList[String]
  private val processOrders = new util.LinkedList[String]

  private val httpProtocol: io.gatling.http.protocol.HttpProtocolBuilder = http.baseUrl(newConfigManager.getString("outward.create_order"))
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .authorizationHeader(TokenGeneration.getDefaultToken())
    .disableWarmUp
    .disableCaching

  private val externalOrderIdfeeder = Iterator.continually(Map("externalOrder" -> s"AL-${scala.math.abs(java.util.UUID.randomUUID.getMostSignificantBits)}"))
  private val b2cMedsFeeder = Iterator.continually(Map("items" -> OrderPayloadCreation.getJsonString()))

  private val createB2COrders = scenario("Create B2C Order")
    .feed(externalOrderIdfeeder)
    .feed(b2cMedsFeeder)
    .exec(generateB2COrders())
    .exec(session => {
      val externalOrderId = session("externalOrderId").asOption[String]
      val size = session("orderedItems").asOption[Any].seq.size
      println(externalOrderId + " :: " + size)
      if (!externalOrderId.isEmpty) {
        queue.add(externalOrderId.get + DELIMITER + size)
      }
      session
    })

  val processB2COrders = scenario("Process B2C Order")
    .feed(OrderPayloadCreation.getPickerListFeeder())
    .pause(fetchOrderDelayStartInSeconds second)
    .exec(doWhile(session => !queue.isEmpty && queue.size() >= maxProcessCount) {
      exec(repeat(maxProcessCount, "count") {
        exec(session => {
          val id = queue.remove(0)
          val initialValue = 0
          val value = id.split(DELIMITER)
          processOrders.add(value(0))
          session.set("externalOrderId", value(0)).set("noOfUcodes", value(1).toInt).set("totalUcodes", 0)
            .set("aggregatePickedCount", initialValue).set("aggregatePickerTaskCount", initialValue).
            set("aggregatedPickerTaskId","0")
        })
          .asLongAsDuring(session => session("noOfUcodes").as[Int] != session("totalUcodes").as[Int], (10 seconds)) {
            exec(getPickerTaskFromEpicenter())
          }.exitHereIfFailed
          .exec(session => {
            val pickerId = session("pickerTaskId").as[String].trim
            println("Picker Task Id  " + pickerId)
            println("Sessions " + session)
            pickerTasks.add(pickerId)
            session
          })
          .exec(getPickerTaskPriority())
          .exec(doIf(session=> !session("pickerTaskPriority").as[String].equals("P1")) {
            exec(prioritisePickerTask())
              .exitHereIfFailed
          })
      })
        .exec(configureMultiPicking(maxProcessCount)).exitHereIfFailed
        .asLongAsDuring(session => !session("aggregatePickerTaskCount").as[String].equals(maxProcessCount.toString), (10 seconds)) {
          exec(aggregateAssignedPickerTasks()).exitHereIfFailed
        }
        .exec(getAvailableTray(maxProcessCount)).exitHereIfFailed
        .pause(500 milliseconds)
        .foreach("${trayIds}", "tray") {
          exec(session => {
            val tray = session("tray")
            session.set("trayId", tray)
          })
            .exec(pickLastTray()).exitHereIfFailed
        }.pause(500 milliseconds)
        .asLongAsDuring(session => session("aggregatedPickerTaskId").as[Any] != null
          || !session("aggregatedPickerTaskId").asOption[String].isEmpty, 10 seconds) {
          exec(aggregatePickerTaskPicked())
            .exitHereIfFailed
        }
        .asLongAsDuring(session => !"ZONE_SCANNING".equals(session("aggregatePickerTaskStatus").as[String]), 10 second) {
          exec(getBarcodes()).exitHereIfFailed
            .foreach("${barcodeList}", "barcode") {
              exec(sessionFunction = session => {
                session.set("barcode", session("barcode").as[String])
              })
                .exec(pickedItems())
                .exitHereIfFailed
            }
            .exec(completePickedItems())
            .exitHereIfFailed
        }
        .exec(completePickedItemLater())
        .foreach("${pickerTaskIds}", "pIds") {
          exec(scanZone())
            .exitHereIfFailed
            .exec(getOrderId())
            .exitHereIfFailed
            .exec(generateBill())
            .exitHereIfFailed

        }
        .exec(aggregateUnAssignedPickerTasks())
              .exitHereIfFailed
      //        .pause(500 millisecond)
      //        .repeat(maxProcessCount, "count") {
      //          exec(session => {
      //            val orderId = processOrders.remove(0)
      //            session.set("externalOrderId", orderId)
      //          })
      //            .exec(generateBill())
      //            .exitHereIfFailed
      //        }
      //        .exec(aggregateUnAssignedPickerTasks())
      //        .exitHereIfFailed


      //        .doIf(session => "ZONE_SCANNING".equals(session("aggregatePickerTaskStatus").as[String])) {
      //          exec(completePickedItemLater())
      //        }


      //        .repeat(maxProcessCount, "count") {
      //          exec(session => {
      //            val pickerTaskId = pickerTasks.remove(0)
      //            session.set("pickerTaskId", pickerTaskId)
      //          })
      //            .exec(scanZone())
      //            .exitHereIfFailed
      //        }

    }

    )


  setUp(
    createB2COrders.inject(constantUsersPerSec(rampUpCreationUsers) during (rampUpDuration)),
    processB2COrders.inject(rampUsers(rampUpUsers) during (rampUpDuration))
  ).protocols(httpProtocol)
}