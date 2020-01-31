package simulations

import io.gatling.core.Predef._
import io.gatling.core.feeder.Feeder
import io.gatling.http.Predef.{http, _}
import utils.Utilities._

import scala.concurrent.duration._

class B2COrders extends io.gatling.core.Predef.Simulation {

  val httpProtocol: io.gatling.http.protocol.HttpProtocolBuilder = http.baseUrl("https://qa2.thea.gomercury.in/")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .authorizationHeader("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJhcHAiOiJuZWJ1bGEiLCJhdWQiOiJtZXJjdXJ5IiwidWlkIjoiOWVmNjY0NjUtNDc0Yi00ZmFhLWE1N2EtNDU1NTdhYWZiOTg3IiwiaXNzIjoiUGhhcm1FYXN5LmluIiwibmFtZSI6ImRocnV2Iiwic3RvcmUiOiIzNTRhMTNlYi1iZDlkLTRhNmMtYTAyYi04YWFjMGRjNTgxNWQiLCJzY29wZXMiOlsic3RvcmUtcGhhcm1hY2lzdCIsIndoLWdhdGUtcGFzcy11c2VyIiwid2gtc2lnbmF0b3J5Iiwid2gtc3VwZXItYWRtaW4iXSwiZXhwIjoxNTgxMDczODg0LCJ1c2VyIjoiZGhydXYuY2hvdWRoYXJ5QHBoYXJtZWFzeS5pbiIsInRlbmFudCI6InRoMDE0In0.6x7bapjGARFb-0VbPfNQgf-Mjp98YaHif7-EIsSxWsjG2DmFSTL4JWAtaL2N37Wb_rT7OrGZ5P9JxMhWXw0DFw")
    .disableWarmUp.disableCaching

  val externalOrderfeeder = new Feeder[String] {
    override def hasNext = true

    override def next: Map[String, String] = {
      val externalOrder = "AutoLoad-" + scala.math.abs(java.util.UUID.randomUUID.getMostSignificantBits)
      Map("externalOrder" -> externalOrder)
    }
  }

  val temp1: String = """{ "medicineId":77, "name":"${name}", "ucode":"${ucode}", "orderedQuantity":${orderedQuantity} }"""

  val medicinesData: List[Array[String]] = readCSV("b2c_meds.csv")

  def getPayload(): String = {


    val num = randomNumberBetweenRange(1, 3)

    var payload = "";

    if (num == 1) {
      val data = medicinesData(randomNumberBetweenRange(0, medicinesData.size))

      println("************************************>>> " + data)



      payload =
        s"""{
           |  "medicineId":77,
           |  "name":"${data(0)}",
           |  "ucode":"${data(1)}",
           |  "orderedQuantity":${data(2)}
           |}""".stripMargin
    } else if (num == 2) {
      val shuffled = scala.util.Random.shuffle(medicinesData)
      payload =
        s"""{
           |  "medicineId":77,
           |  "name":"${shuffled(0)(0)}",
           |  "ucode":"${shuffled(0)(1)}",
           |  "orderedQuantity":${shuffled(0)(2)}
           |},
           |{ "medicineId":77,
           |  "name":"${shuffled(1)(0)}",
           |  "ucode":"${shuffled(1)(1)}",
           |  "orderedQuantity":${shuffled(1)(2)}
           |}""".stripMargin
    } else if (num == 3) {
      val shuffled = scala.util.Random.shuffle(medicinesData)
      payload =
        s"""{
           |  "medicineId":77,
           |  "name":"${shuffled(0)(0)}",
           |  "ucode":"${shuffled(0)(1)}",
           |  "orderedQuantity":${shuffled(0)(2)}
           |},
           |{
           |  "medicineId":77,
           |  "name":"${shuffled(1)(0)}",
           |  "ucode":"${shuffled(1)(1)}",
           |  "orderedQuantity":${shuffled(1)(2)}
           |},
           |{
           |  "medicineId":77,
           |  "name":"${shuffled(2)(0)}",
           |  "ucode":"${shuffled(2)(1)}",
           |  "orderedQuantity":${shuffled(2)(2)}
           |}""".stripMargin
    }
    return payload
  }

  val continuallyFeeder = Iterator.continually(Map("items" -> getPayload))

  val jsonFeederFileParser: io.gatling.core.feeder.FileBasedFeederBuilder[Any]#F = jsonFile("src/test/resources/meds.json").circular
  val payload: String =
    """{
                        "id":null,
                        "externalOrderId":"${externalOrder}",
                        "customerOrderId":"1000577",
                        "customerOrderCreatedOn":"2019-07-09T06:19:40",
                        "promisedDeliveryDate":"2019-07-09T11:23:24",
                        "source":null,
                        "pharmacistName":"Pankaj",
                        "rpNumber":"Test RP",
                        "customerName":"cankit",
                        "customerMobile":"1234567890",
                        "customerEmail":"qwerty@gmail.com",
                        "patientName":"Kiran",
                        "doctorName":"Deep",
                        "storeId":"065b642a-26e5-4be3-ac29-01d7ee605a46",
                        "storeName":"Laxmi  (Mahadevapura)",
                        "warehouseId":2,
                        "pickerTaskId":null,
                        "trayId":null,
                        "address":{
                          "name":"kiran",
                          "address1":"89 6th cross",
                          "address2":"RPS Road",
                          "address3":"Indiranagar",
                          "city":"Jaipur",
                          "pincode":"444440",
                          "contactNumber":"8989898989"
                        },
                        "items": [
                          ${items}
                        ],
                        "pickedItems":[

                        ],
                        "discountPercentage":10,
                        "storeDiscountPercentage":10,
                        "priority":0,
                        "storeRemark":"sr",
                        "customerRemark":"cr",
                        "validateWithCMS":false,
                        "status":null,
                        "deliveryCharge":0,
                        "cashHandlingCharge":0,
                        "payableAmount":157,
                        "totalAmount":174,
                        "interStateOrder":false,
                        "courierOrder":true,
                        "issue":null,
                        "thirdPartyOrder":null,
                        "customerCareNumber":"08030752800",
                        "customerCareEmail":"care@pharmeasy.in",
                        "updatedByName":"Kiran",
                        "retailerName":null,
                        "deltaReportedOn":null,
                        "pharmacistSignatureId":null,
                        "ref":true
                      }"""

  val scenarioFusion = scenario("AsynchronousTest")
    .feed(externalOrderfeeder)
    .feed(continuallyFeeder)
    .exec(http("AsynchronousAPIs")
      .post("api/outward/orders/")
      .body(StringBody(payload))
      .check(status.is(200)))

  setUp(
    scenarioFusion.inject(rampUsers(1) during (2 seconds))).protocols(httpProtocol)
}
