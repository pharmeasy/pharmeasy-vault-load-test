package simulations.templates.consumer

import java.lang.System._

import actions.ConsumerActions._
import io.gatling.core.Predef._
import io.gatling.core.controller.inject.open.OpenInjectionStep
import simulations.templates.BaseTemplate
import utils.Utilities._

import scala.concurrent.duration._

class TemplateSearch extends BaseTemplate {

  private val rampUpUsers = getProperty("rampUpUsers", "10").trim.toInt
  private val rampUpDuration = getProperty("rampUpDuration", "10").trim.toInt

  override def name() = "search"

  val scn = scenario(name)
    .exec(session => {
      val randomPayment = randomPaymentMode()
      addToSession(
        session,
        ("pageNumber", "1"),
        ("pageNumberMax", randomPageNumber),
        ("query", randomMedicineQuery),
        ("contactNumber", randomMobileNumber),
        ("paymentId", randomPayment("paymentId")),
        ("paymentSubId", randomPayment("paymentSubId")))
    })
    .exec(omniSearchCharByChar(query = randomMedicineQuery))
    .exitHereIfFailed
    .exec(viewPages())
    .exitHereIfFailed

  override def step(): OpenInjectionStep =
    rampUsers(rampUpUsers) during (rampUpDuration seconds);

  override def enabled() = true

}
