package actions.scm

import newUtilities.newUtilities._
import org.json4s.DefaultFormats
import org.json4s.native.Serialization.write

import io.gatling.core.session.Session
import io.gatling.http.Predef.http
import newUtilities.TokenGeneration
import utils.Utilities.{randomNumberBetweenRange, readCSV}

object CreateB2BOrderPayload {
  protected def getFromSession(session: Session, key: String, defaultValue: String = null): String = session(key).asOption[String].getOrElse(defaultValue)

  private val random = scala.util.Random

  private val b2bMedicinesData: List[Array[String]] = readCSV("b2c_meds.csv")

  private def getB2BPayload(max: Int = 3): String = {
    val shuffled = random.shuffle(b2bMedicinesData)
    val num = randomNumberBetweenRange(1, max)
    0.to(num - 1).map(index => shuffled(index)).map(e =>
      s"""
         |      {
         |        "name":"${e(0)}",
         |        "distributorItemCode":"${e(1)}",
         |        "orderedQuantity":${e(2)}
         |      }""".stripMargin).mkString(",\n")
  }

  val b2bMedsFeeder = Iterator.continually(Map("customerId" -> randomNumberBetweenRange(100000000, 999999999), "orderItems" -> getB2BPayload()))

  //  def b2bMedsFeeder() :  = {
  //
  //     val b2bMedsFeeder = Iterator.continually(Map("orderItems" -> getB2BPayload()))
  //
  //    b2bMedsFeeder
  //  }


  def createOrder() =
    """{
      |  "uniqueId": ${customerId},
      |  "orderDate": "2020-03-19 12:51:16",
      |  "orderStatus": "Pending",
      |  "orderPriority": 0,
      |  "distributorRetailerCode": "2231",
      |  "orderItems": [
      |         {
      |      "name": "GLYCIPHAGE 850MG TAB",
      |      "distributorItemCode": "085670",
      |      "orderedQuantity": 1
      |    }
      |
      |  ]
      |}""".stripMargin


}




