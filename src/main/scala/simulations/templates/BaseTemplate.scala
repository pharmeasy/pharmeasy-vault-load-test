package simulations.templates

import io.gatling.core.controller.inject.open.OpenInjectionStep
import io.gatling.core.session.Session
import utils.Utilities._;

trait BaseTemplate {

  private val queryFeeder = readCSV("medicines_subset.csv").map(_ (0).trim) filter (!_.isEmpty) toArray

  protected def randomMedicineQuery() = queryFeeder(randomNumberBetweenRange(0, queryFeeder.size - 1))

  protected def randomPageNumber() = randomNumberBetweenRange(1, 1).toString

  private val paymentModes = Array(
    Map("paymentId" -> -1, "paymentSubId" -> -1, "name" -> "Cash On Delivery"),
    Map("paymentId" -> 3, "paymentSubId" -> 60, "name" -> "Paytm"),
    Map("paymentId" -> 3, "paymentSubId" -> 62, "name" -> "PhonePe/BHIM UPI"),
    Map("paymentId" -> 2, "paymentSubId" -> 21, "name" -> "HDFC Bank"),
    Map("paymentId" -> 2, "paymentSubId" -> 22, "name" -> "ICICI Bank")
  )

  private val invokePaymentModes = paymentModes filter (e => e("paymentId") != -1 || e("paymentSubId") != -1) toArray

  protected def isCODPayment(session: Session) = session("paymentId").asOption[Int].getOrElse(-1) == -1 || session("paymentSubId").asOption[Int].getOrElse(-1) == -1

  protected def randomPaymentMode() = paymentModes(randomNumberBetweenRange(0, paymentModes.size - 1))

  protected def randomInvokePaymentMode() = invokePaymentModes(randomNumberBetweenRange(0, invokePaymentModes.size - 1))

  def step(): OpenInjectionStep

  def name(): String

  def enabled() = true
}
