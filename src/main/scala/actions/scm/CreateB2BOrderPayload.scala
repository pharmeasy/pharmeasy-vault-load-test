package actions.scm

import newUtilities.newUtilities.{randomNumberBetweenRange, _}
import org.json4s.native.Serialization.write
import io.gatling.core.session.Session


object CreateB2BOrderPayload {
  protected def getFromSession(session: Session, key: String, defaultValue: String = null): String = session(key).asOption[String].getOrElse(defaultValue)

  private val random = scala.util.Random

  private val b2bMedicinesData: List[Array[String]] = readCSV("b2c_meds.csv")

  private def getB2BPayload(max: Int = 3): List[B2BOrderItems]= {
    val shuffled = random.shuffle(b2bMedicinesData)
    val num = randomNumberBetweenRange(1, max)
    var items = List[B2BOrderItems]()
    for( a <- 0 to num-1){
      val data = shuffled(a);
      val item  = B2BOrderItems(data(0),data(1),data(2))
      items = item :: items
    }
    return items
  }

  def getJsonString():String = write(getB2BPayload())

  val b2bMedsFeeder = Iterator.continually(Map("customerId" -> randomNumberBetweenRange(100000000, 999999999), "orderItems" -> getB2BPayload()))


  val b2bOrderPayload  = B2BOrderPayload("${customerId}",
    "2020-03-19 12:51:16",
    "Pending",
    0,
    "2231",
    "StringReplace"
  )


  def getOrderPayload():String = {
    return write(b2bOrderPayload).replace("\"StringReplace\"","${orderItems}")
  }
}




