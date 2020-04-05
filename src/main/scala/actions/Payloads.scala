package actions

import io.gatling.core.session.Session
import utils.Utilities.{randomNumberBetweenRange, readCSV}

trait Payloads {
  protected def getFromSession(session: Session, key: String, defaultValue: String = null): String = session(key).asOption[String].getOrElse(defaultValue)

  private val random = scala.util.Random

  private val b2cMedicinesData: List[Array[String]] = readCSV("b2c_meds.csv")

  private def getB2CPayload(max: Int = 3): String = {
    val shuffled = random.shuffle(b2cMedicinesData)
    val num = randomNumberBetweenRange(1, max)
    0.to(num - 1).map(index => shuffled(index)).map(e =>
      s"""
         |      {
         |        "medicineId":77,
         |        "name":"${e(0)}",
         |        "ucode":"${e(1)}",
         |        "orderedQuantity":${e(2)}
         |      }""".stripMargin).mkString(",\n")
  }

   val payload: String =
    """
      |{
      |    "id":null,
      |    "externalOrderId":"${externalOrder}",
      |    "customerOrderId":"1000577",
      |    "customerOrderCreatedOn":"2019-07-09T06:19:40",
      |    "promisedDeliveryDate":"2019-07-09T11:23:24",
      |    "source":null,
      |    "pharmacistName":"Pankaj",
      |    "rpNumber":"Test RP",
      |    "customerName":"cankit",
      |    "customerMobile":"1234567890",
      |    "customerEmail":"qwerty@gmail.com",
      |    "patientName":"Kiran",
      |    "doctorName":"Deep",
      |    "storeId":"065b642a-26e5-4be3-ac29-01d7ee605a46",
      |    "storeName":"Laxmi  (Mahadevapura)",
      |    "warehouseId":2,
      |    "pickerTaskId":null,
      |    "trayId":null,
      |    "address":{
      |      "name":"kiran",
      |      "address1":"89 6th cross",
      |      "address2":"RPS Road",
      |      "address3":"Indiranagar",
      |      "city":"Jaipur",
      |      "pincode":"444440",
      |      "contactNumber":"8989898989"
      |    },
      |    "items": [
      |      ${items}
      |    ],
      |    "pickedItems":[],
      |    "discountPercentage":10,
      |    "storeDiscountPercentage":10,
      |    "priority":0,
      |    "storeRemark":"sr",
      |    "customerRemark":"cr",
      |    "validateWithCMS":false,
      |    "status":null,
      |    "deliveryCharge":0,
      |    "cashHandlingCharge":0,
      |    "payableAmount":157,
      |    "totalAmount":174,
      |    "interStateOrder":false,
      |    "courierOrder":true,
      |    "issue":null,
      |    "thirdPartyOrder":null,
      |    "customerCareNumber":"08030752800",
      |    "customerCareEmail":"care@pharmeasy.in",
      |    "updatedByName":"Kiran",
      |    "retailerName":null,
      |    "deltaReportedOn":null,
      |    "pharmacistSignatureId":null,
      |    "ref":true
      |}
      |""".stripMargin


}
