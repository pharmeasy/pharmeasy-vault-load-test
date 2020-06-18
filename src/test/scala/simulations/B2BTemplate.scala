//package simulations
//
//import actions.OrderProcessingActions
//import actions.OrderProcessingActions.addToSession
//import actions.actions.B2BOrderProcessingActions
//import actions.scm.CreateB2BOrderPayload._
//import io.gatling.core.Predef._
//import io.gatling.http.Predef.http
//
//import scala.concurrent.duration._
//
//class B2BTemplate extends Simulation {
//
//  private val random = scala.util.Random
//
//
//  private val httpProtocol: io.gatling.http.protocol.HttpProtocolBuilder = http
//    .acceptHeader("application/json")
//    .contentTypeHeader("application/json")
//    .disableWarmUp
//    .disableCaching
//
//
//  private val createB2BOrdersScenario = scenario("B2BCreateOrder")
//    .feed(b2bMedsFeeder)
//    .exec(B2BOrderProcessingActions.createB2BOrders())
//    .exec(B2BOrderProcessingActions.fetchOrderId())
////    .exec(OrderProcessingActions.configureMultiPicking())
////    .exec(OrderProcessingActions.getPickerTaskFromEpicenter())
////    .exec(session => {
////      addToSession(
////        session,
////        ("pickerTaskId", session("pickerTaskId").as[String])
////      )
////    })
////    .exec(OrderProcessingActions.prioritisePickerTask())
////    .exec(OrderProcessingActions.signInToPickerApp())
////    .exec(session => {
////      addToSession(
////        session,
////        ("token", session("token").as[String])
////      )
////    })
////    .exec(session => {
////      addToSession(
////        session,
////        ("pickerId", session("pickerId").as[String])
////      )
////    })
////    .pause(5, 10)
////    .exec(OrderProcessingActions.aggregateAssignedPickerTasks())
////    .exec(OrderProcessingActions.pickTray())
////    .exec(session => addToSession(session, ("aggregatedPickerTaskId", session("aggregatePickerTaskId").as[String])))
////    .exec(OrderProcessingActions.aggregatePickerTaskPicked())
////    .exec(session => addToSession(session, ("ucode", session("ucode").as[String])))
////    .exec(session => addToSession(session, ("bin", session("bin").as[String])))
////    .exec(OrderProcessingActions.searchInventoryPostTaskPicked())
////    .exec(session => addToSession(session, ("batch", session("batch").as[String])))
////    .exec(session => addToSession(session,("barcodes", session("barcodes").as[Seq[String]])))
////    .exec(OrderProcessingActions.pickedItems())
////    .exec(OrderProcessingActions.completePickedItems())
////    .exec(OrderProcessingActions.scanZone())
////    .exec(OrderProcessingActions.billingInProgress())
////    .exec(OrderProcessingActions.generateStoreInvoice())
////    .exec(OrderProcessingActions.generateDispatchNote())
////    .exec(OrderProcessingActions.recievedAtStore())
////    .exec(OrderProcessingActions.generateCustomerInvoice())
//
//
//
//  setUp(
//    createB2BOrdersScenario.inject(rampUsers(System.getProperty("b2bRampUpUsers", "1").toInt) during (System.getProperty("b2bRampUpDuration", "2").toInt seconds))
//  ).protocols(httpProtocol)
//
//}
//
