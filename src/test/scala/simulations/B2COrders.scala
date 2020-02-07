package simulations

import io.gatling.core.Predef._
import io.gatling.core.feeder.Feeder
import io.gatling.http.Predef.{http, _}
import io.gatling.jsonpath.JsonPath
import utils.Utilities._

import scala.concurrent.duration._

class B2COrders extends io.gatling.core.Predef.Simulation {

  private val random = scala.util.Random

  private val httpProtocol: io.gatling.http.protocol.HttpProtocolBuilder = http.baseUrl("https://staging.thea.gomercury.in")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .authorizationHeader("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJhcHAiOiJuZWJ1bGEiLCJhdWQiOiJtZXJjdXJ5IiwidWlkIjoiOWVmNjY0NjUtNDc0Yi00ZmFhLWE1N2EtNDU1NTdhYWZiOTg3IiwiaXNzIjoiUGhhcm1FYXN5LmluIiwibmFtZSI6ImRocnV2Iiwic3RvcmUiOiIzNTRhMTNlYi1iZDlkLTRhNmMtYTAyYi04YWFjMGRjNTgxNWQiLCJzY29wZXMiOlsic3RvcmUtcGhhcm1hY2lzdCIsIndoLWdhdGUtcGFzcy11c2VyIiwid2gtc2lnbmF0b3J5Iiwid2gtc3VwZXItYWRtaW4iXSwidXNlciI6ImRocnV2LmNob3VkaGFyeUBwaGFybWVhc3kuaW4iLCJ0ZW5hbnQiOiJ0aDAxNCJ9.d2UkKMP5dahsfX3pf_YNuOhakDzfqMVv_dwrwPehKMiuVQwFnx5EqIn3O9GGMgapnbnSGkUu06YqX4o3__6Khw")
    .disableWarmUp.disableCaching

  private val externalOrderIdfeeder = Iterator.continually(Map("externalOrder" -> s"AutoLoad-${scala.math.abs(java.util.UUID.randomUUID.getMostSignificantBits)}"))

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

  private val b2cMedsFeeder = Iterator.continually(Map("items" -> getB2CPayload()))

  private val payload: String =
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


  private val createB2COrders = scenario("AsynchronousTest")
    .feed(externalOrderIdfeeder)
    .feed(b2cMedsFeeder)
    .exec(http("AsynchronousAPIs")
      .post("/api/outward/orders")
      .body(StringBody(payload))
      .check(status.is(200),jsonPath("$..externalOrderId").notNull.saveAs("externalOrderId")))
    .exec(session => {
      writeFile("externalOrderId.csv", session("externalOrderId").as[String] + "\n");
      session
    })

  setUp(
    createB2COrders.inject(rampUsers(System.getProperty("b2cRampUpUsers", "1").toInt) during (System.getProperty("b2cRampUpDuration", "2").toInt seconds))
  ).protocols(httpProtocol)
}
