package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef.{http, _}
import utils.Utilities._

import scala.concurrent.duration._

class SCMOrders extends io.gatling.core.Predef.Simulation {

  private val random = scala.util.Random

  private val httpProtocol: io.gatling.http.protocol.HttpProtocolBuilder = http
    .baseUrl("")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .authorizationHeader("Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vb21zLmNvbSIsIm9tc1JvbGVJZCI6Niwib21zUm9sZSI6IkRJU1RSSUJVVE9SU1VQRVJTQUxFU01BTiIsIm9tc1VzZXJJZCI6MjU3MSwianRpIjoiNTlkOWUyNGUtMDk3OC00NjdkLWEyY2YtY2NkZGRkNzlmYjQyIiwiaWF0IjoxNTgwNzI1MTc0LCJleHAiOjE2MTE4MjkxNzR9.2uHTx4Nb_MBkTnbM5Bbm3m-oKVJRfgkyjIaTSxR_t-o")
    .header("source", "web")
    .header("version", "1.2.3")
    .disableWarmUp
    .disableCaching


  val retailerFeeder = csv("retailers.csv").eager.random.circular

  val alpha = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
  val size = alpha.size
  def randStr(n: Int) = (1 to n).map(x => alpha(scala.util.Random.nextInt.abs % size)).mkString

  private val medicinesData: List[Array[String]] = readCSV("orderItemId.csv")

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

  private val B2BMedsFeeder = Iterator.continually(Map("items" -> getPayload(1)))

  private val b2bPayload: String =
    """
      |{
      |  "orderItems": [
      |    ${items}
      |  ]
      |}""".stripMargin

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

  private val createB2BOrders = scenario("AsynchronousTest")
    .feed(retailerFeeder)
    .feed(B2BMedsFeeder)
    .exec(http("AsynchronousAPIs")
      .post("https://staging.retailio.in/api/Distributors/1670/Retailers/${retailerId}/placeOrder")
      .header("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vb21zLmNvbSIsIm9tc1JvbGVJZCI6Niwib21zUm9sZSI6IkRJU1RSSUJVVE9SU1VQRVJTQUxFU01BTiIsIm9tc1VzZXJJZCI6MjU3MSwianRpIjoiNTlkOWUyNGUtMDk3OC00NjdkLWEyY2YtY2NkZGRkNzlmYjQyIiwiaWF0IjoxNTgwNzI1MTc0LCJleHAiOjE2MTE4MjkxNzR9.2uHTx4Nb_MBkTnbM5Bbm3m-oKVJRfgkyjIaTSxR_t-o")
      .body(StringBody(b2bPayload))
      .check(status.is(200), jsonPath("$.orderGroupId").notNull.saveAs("orderGroupId")))
    .exec(session => {
      writeFile("orderGroupId.csv", session("orderGroupId").as[String] + "\n");
      session
    }
    )

  private val createB2COrders = scenario("AsynchronousTest")
    .feed(externalOrderIdfeeder)
    .feed(b2cMedsFeeder)
    .exec(http("AsynchronousAPIs")
      .post("/api/outward/orders")
      .body(StringBody(b2CPayload))
      .check(status.is(200),jsonPath("$..externalOrderId").notNull.saveAs("externalOrderId")))
    .exec(session => {
      writeFile("externalOrderId.csv", session("externalOrderId").as[String] + "\n");
      session
    })

  setUp(
    createB2COrders.inject(rampUsers(System.getProperty("b2cRampUpUsers", "1").toInt) during (System.getProperty("b2cRampUpDuration", "2").toInt seconds)),
    createB2BOrders.inject(rampUsers(System.getProperty("b2bRampUpUsers", "40").toInt) during (System.getProperty("b2bRampUpDuration", "2").toInt seconds))
  ).protocols(httpProtocol)
}
