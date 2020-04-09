package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef.{http, _}

import scala.concurrent.duration._
import newUtilities.newUtilities

class SCMOrders extends io.gatling.core.Predef.Simulation {

  private val random = scala.util.Random

  private val httpProtocol: io.gatling.http.protocol.HttpProtocolBuilder = http
    .baseUrl("")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .header("source", "web")
    .header("version", "1.2.3")
    .disableWarmUp
    .disableCaching


  val retailerFeeder = csv("retailers.csv").eager.random.circular

  val alpha = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
  val size = alpha.size
  def randStr(n: Int) = (1 to n).map(x => alpha(scala.util.Random.nextInt.abs % size)).mkString

  private val medicinesData: List[Array[String]] = newUtilities.readCSV("orderItemId.csv")

  private def getPayload(num: Int = 3): String = {
    val shuffled = random.shuffle(medicinesData)
    val name = randStr(20);
    val qty = 10
    0.to(num - 1).map(index => shuffled(index)).map(e =>
      s"""
         |  {
         |    "id":${e(0)},
         |    "quantity": ${qty},
         |    "name": "${name}"
         |  }""".stripMargin).mkString(",\n")
  }

  private val B2BMedsFeeder = Iterator.continually(Map("items" -> getPayload(50)))

  private val b2bPayload: String =
    """
      |{
      |  "orderItems": [
      |    ${items}
      |  ]
      |}""".stripMargin

  private val externalOrderIdfeeder = Iterator.continually(Map("externalOrder" -> s"AutoLoad-${scala.math.abs(java.util.UUID.randomUUID.getMostSignificantBits)}"))

  private val b2cMedicinesData: List[Array[String]] = newUtilities.readCSV("b2c_meds.csv")

  private def getB2CPayload(max: Int = 3): String = {
    val shuffled = random.shuffle(b2cMedicinesData)
    val num = newUtilities.randomNumberBetweenRange(1, max)
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

  private val b2CPayload: String =
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
      |    "storeId":"2800af35-0b7f-4324-9cf8-76143baceb72",
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

  private val createB2BOrders = scenario("B2BTest")
    .feed(retailerFeeder)
    .feed(B2BMedsFeeder)
    .exec(http("AsynchronousAPIs")
      .post("https://staging.retailio.in/api/Distributors/1670/Retailers/${retailerId}/placeOrder")
      .header("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vb21zLmNvbSIsIm9tc1JvbGVJZCI6Niwib21zUm9sZSI6IkRJU1RSSUJVVE9SU1VQRVJTQUxFU01BTiIsIm9tc1VzZXJJZCI6Mjg3NCwianRpIjoiODczZTlhOGQtYTcyYy00MTIyLWJjOWYtODE3Y2Q4ZDgxMDFjIiwiaWF0IjoxNTgwODIwMDEwLCJleHAiOjE2MTE5MjQwMTB9.u_JWKY4QxbjbYCUV0zB-e-qFz6gsLSm-pyA-Ehzyzbc\ncookie: mp_67769c00e598aec9b31e7e6024ff0a0b_mixpanel=%7B%22distinct_id%22%3A%20%221700a885b5997c-0e29edf2d8ff75-39617b0f-13c680-1700a885b5a559%22%2C%22%24device_id%22%3A%20%221700a885b5997c-0e29edf2d8ff75-39617b0f-13c680-1700a885b5a559%22%2C%22%24initial_referrer%22%3A%20%22%24direct%22%2C%22%24initial_referring_domain%22%3A%20%22%24direct%22%7D; mp_130bd02cca41cfbfe0e1a114e6dee2b1_mixpanel=%7B%22distinct_id%22%3A%20%221700a885b6612f-0a4815416b1751-39617b0f-13c680-1700a885b6790f%22%2C%22%24device_id%22%3A%20%221700a885b6612f-0a4815416b1751-39617b0f-13c680-1700a885b6790f%22%2C%22%24initial_referrer%22%3A%20%22%24direct%22%2C%22%24initial_referring_domain%22%3A%20%22%24direct%22%7D; amplitude_id_3c27150f9653ce05775c537209a3ee40retailio.in=eyJkZXZpY2VJZCI6IjdlMDViYWZlLTc2NmItNGUyNi1iZGE4LTMxYzViM2ZiZjhjMlIiLCJ1c2VySWQiOiIyODc0Iiwib3B0T3V0IjpmYWxzZSwic2Vzc2lvbklkIjoxNTgxMDc3MTIzODA0LCJsYXN0RXZlbnRUaW1lIjoxNTgxMDc3MTI1MzI3LCJldmVudElkIjoxODIsImlkZW50aWZ5SWQiOjMwLCJzZXF1ZW5jZU51bWJlciI6MjEyfQ==")
      .body(StringBody(b2bPayload))
      .check(status.is(200), jsonPath("$.orderGroupId").notNull.saveAs("orderGroupId")))
    .exec(session => {
      newUtilities.writeFile("orderGroupId.csv", session("orderGroupId").as[String] + "\n");
      session
    })

  private val createB2COrders = scenario("B2CTest")
    .feed(externalOrderIdfeeder)
    .feed(b2cMedsFeeder)
    .exec(http("AsynchronousAPIs")
      .post("https://staging.thea.gomercury.in/api/outward/orders")
      .header("Authorization", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJhcHAiOiJuZWJ1bGEiLCJhdWQiOiJtZXJjdXJ5IiwidWlkIjoiOWVmNjY0NjUtNDc0Yi00ZmFhLWE1N2EtNDU1NTdhYWZiOTg3IiwiaXNzIjoiUGhhcm1FYXN5LmluIiwibmFtZSI6ImRocnV2Iiwic3RvcmUiOiIzNTRhMTNlYi1iZDlkLTRhNmMtYTAyYi04YWFjMGRjNTgxNWQiLCJzY29wZXMiOlsic3RvcmUtcGhhcm1hY2lzdCIsIndoLWdhdGUtcGFzcy11c2VyIiwid2gtc2lnbmF0b3J5Iiwid2gtc3VwZXItYWRtaW4iXSwidXNlciI6ImRocnV2LmNob3VkaGFyeUBwaGFybWVhc3kuaW4iLCJ0ZW5hbnQiOiJ0aDEyNCJ9.IvWuaiw1yilfDitbwKMjS0SEoOo3ZBDHdw8asA-BMfbzzKxkQITqKjdTXXRe4IYubYaTFSHW4Jp9xx6HcJk4hA")
      .body(StringBody(b2CPayload))
      .check(status.is(200),jsonPath("$..externalOrderId").notNull.saveAs("externalOrderId")))
    .exec(session => {
      newUtilities.writeFile("externalOrderId.csv", session("externalOrderId").as[String] + "\n");
      session
    })

  setUp(
    createB2COrders.inject(rampUsers(System.getProperty("b2cRampUpUsers", "1").toInt) during (System.getProperty("b2cRampUpDuration", "2").toInt seconds)),
    createB2BOrders.inject(rampUsers(System.getProperty("b2bRampUpUsers", "1").toInt) during (System.getProperty("b2bRampUpDuration", "2").toInt seconds))
  ).protocols(httpProtocol)
}
