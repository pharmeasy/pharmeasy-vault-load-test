package simulations.templates.consumer

import actions.ConsumerActions
import actions.ConsumerActions._
import io.gatling.core.Predef._
import io.gatling.core.controller.inject.open.OpenInjectionStep
import io.gatling.http.Predef._
import simulations.templates.BaseTemplate
import utils.Utilities.randomMobileNumber

import scala.concurrent.duration._

class TemplateSearchToCheckout extends BaseTemplate {

  override def name() = "search - checkout"

  val scn = scenario(name)
    .exec(session => {
      val randomPayment = randomPaymentMode
      val randomInvokePayment = randomInvokePaymentMode
      addToSession(session, ("pageNumber", "1"), ("pageNumberMax", randomPageNumber), ("query", randomMedicineQuery), ("contactNumber", randomMobileNumber), ("paymentId", randomPayment("paymentId")), ("paymentSubId", randomPayment("paymentSubId")), ("invokePaymentId", randomInvokePayment("paymentId")), ("invokePaymentSubId", randomInvokePayment("paymentSubId")))
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
    .pause(1 seconds)
    .exec(cartItemsCountAfterLogin()).exitHereIfFailed
    .exec(fetchAddressOrAddIfNotExist()).exitHereIfFailed
    .exec(estimateDelivery()).exitHereIfFailed
    .exec(deliveryPreference()).exitHereIfFailed
    .exec(paymentInstrumentationBeforeOrderPlacement()).exitHereIfFailed
    .exec(cartItems()).exitHereIfFailed

  override def step(): OpenInjectionStep = rampUsers(1) during (2 seconds);

  override def enabled(): Boolean = false
}
