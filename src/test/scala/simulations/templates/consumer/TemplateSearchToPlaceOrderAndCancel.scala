package simulations.templates.consumer

import actions.ConsumerActions._
import io.gatling.core.Predef._
import io.gatling.core.controller.inject.open.OpenInjectionStep
import io.gatling.http.Predef._
import simulations.templates.BaseTemplate
import utils.Utilities._

import scala.concurrent.duration._

class TemplateSearchToPlaceOrderAndCancel extends BaseTemplate {

  override def name() = "search - place order - cancel"

  val scn = scenario(name)
    .exec(session => {
      val randomPayment = randomPaymentMode()
      addToSession(session, ("pageNumber", "1"), ("pageNumberMax", randomPageNumber), ("query", randomMedicineQuery), ("contactNumber", randomMobileNumber), ("paymentId", randomPayment("paymentId")), ("paymentSubId", randomPayment("paymentSubId")))
    })
    .exec(recommendations()).exitHereIfFailed
    .exec(home()).exitHereIfFailed
    .exec(omniSearchCharByChar(query = randomMedicineQuery)).exitHereIfFailed
    .exec(viewPages()).exitHereIfFailed
    .exec(session => randomProductId(session))
    .exec(productInformation()).exitHereIfFailed
    .exec(productDescription()).exitHereIfFailed
    .exec(addToCart()).exitHereIfFailed
    .exec(recommendedOTCProducts()).exitHereIfFailed
    .exec(savingsOnCart()).exitHereIfFailed
    .exec(sendOTP()).exitHereIfFailed
    .exec(login()).exitHereIfFailed
    .exec(cartSyncAfterLogin()).exitHereIfFailed
    .pause(2 seconds)
    .exec(cartItemsCountAfterLogin()).exitHereIfFailed
    .exec(fetchAddressOrAddIfNotExist()).exitHereIfFailed
    .exec(estimateDelivery()).exitHereIfFailed
    .exec(deliveryPreference()).exitHereIfFailed
    .exec(paymentInstrumentationBeforeOrderPlacement()).exitHereIfFailed
    .exec(cartItems()).exitHereIfFailed
    .exec(placeOrder()).exitHereIfFailed
    .exec(fetchOrders()).exitHereIfFailed
    .exec(orderDetails()).exitHereIfFailed
    .exec(orderItems()).exitHereIfFailed
    .exec(orderPriceFluctuation()).exitHereIfFailed
    .exec(paymentInstrumentationAfterOrderPlacement()).exitHereIfFailed
    .exec(doIf(session => isCODPayment(session)) {
      exec(paymentInvocationDetails()).exitHereIfFailed
        .exec(codPayment()).exitHereIfFailed
        .exec(onlinePaymentStatusVerification()).exitHereIfFailed
    }).exitHereIfFailed
    .exec(cancelOrder()).exitHereIfFailed
    .exec(orderDetails())


  override def step(): OpenInjectionStep = rampUsers(1) during (2 seconds);

  override def enabled() = true

}
