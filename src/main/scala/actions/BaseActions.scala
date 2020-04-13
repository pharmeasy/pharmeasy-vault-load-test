package actions

import io.gatling.core.session.Session
import utils.Utilities._

trait BaseActions {

  protected def getFromSession(session: Session, key: String, defaultValue: String = null): String = session(key).asOption[String].getOrElse(defaultValue)
  protected def getSeqFromSession(session: Session, key: String, defaultValue: Seq[String] = null): Seq[String] = session(key).asOption[Seq[String]].getOrElse(defaultValue)

  private val deliveryInterval = Array(Map("interval" -> 0), Map("interval" -> 30), Map("interval" -> 45), Map("interval" -> 60))

  protected def randomInterval() = deliveryInterval(randomNumberBetweenRange(0, deliveryInterval.length - 1))("interval")

  private val paymentModes = Array(
    Map("paymentId" -> 3, "paymentSubId" -> 60, "name" -> "Paytm"),
    Map("paymentId" -> 3, "paymentSubId" -> 62, "name" -> "PhonePe/BHIM UPI"),
    Map("paymentId" -> 2, "paymentSubId" -> 21, "name" -> "HDFC Bank"),
    Map("paymentId" -> 2, "paymentSubId" -> 22, "name" -> "ICICI Bank"))

  protected def randomPaymentMode() = paymentModes(randomNumberBetweenRange(0, paymentModes.length - 1))
}
