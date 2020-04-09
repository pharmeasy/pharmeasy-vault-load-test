package actions.scm

import org.json4s.DefaultFormats
import org.json4s.native.Serialization.write
import newUtilities.newUtilities._

object OrderPayloadCreation {

  private val random = scala.util.Random
  implicit val formats = DefaultFormats

  private val b2cMedicinesData: List[Array[String]] = readCSV("b2c_meds.csv")

  private def getB2CPayload(max: Int = 3): List[Item] = {
    val shuffled = random.shuffle(b2cMedicinesData)
    val num = randomNumberBetweenRange(1, max)
    var items = List[Item]()
    for( a <- 0 to num-1){
      val data = shuffled(a);
      val item  = Item(77,data(0),data(1),data(2))
      items = item :: items
    }
    return items
  }

  def getJsonString():String = write(getB2CPayload())

  val address = Address("89 6th cross",
    "RPS Road",
    "Indiranagar",
    "kiran",
    "Bangalore",
    "8989898989",
    "8989898989")

  val orderPayload = OrderPayload(
    null,
    "${externalOrder}",
    "1000577",
    "2019-07-09T06:19:40",
    "2019-07-09T11:23:24",
    null,
    "Pankaj",
    "Test RP",
    "cankit",
    "1234567890",
    "qwerty@gmail.com",
    "Kiran",
    "Deep",
    "065b642a-26e5-4be3-ac29-01d7ee605a46",
    "Laxmi  (Mahadevapura)",
    2,
    null,
    null,
    address,
    "StringReplace",
    List(),
    10,
    10,
    0,
    "sr",
    "cr",
    false,
    null,
    0,
    0,
    157,
    174,
    false,
    true,
    null,
    null,
    "08030752800",
    "care@pharmeasy.in",
    "Kiran",
    null,
    null,
    null,
    true
  )

  def getOrderPayload():String = {
    return write(orderPayload).replace("\"StringReplace\"","${items}")
  }

}
