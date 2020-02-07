package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef.{http, _}
import utils.Utilities._

import scala.concurrent.duration._

class B2BOrders extends io.gatling.core.Predef.Simulation {

  private val random = scala.util.Random

  private val httpProtocol: io.gatling.http.protocol.HttpProtocolBuilder = http
    .baseUrl("https://staging.retailio.in")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .authorizationHeader("Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vb21zLmNvbSIsIm9tc1JvbGVJZCI6Niwib21zUm9sZSI6IkRJU1RSSUJVVE9SU1VQRVJTQUxFU01BTiIsIm9tc1VzZXJJZCI6Mjg3NCwianRpIjoiODczZTlhOGQtYTcyYy00MTIyLWJjOWYtODE3Y2Q4ZDgxMDFjIiwiaWF0IjoxNTgwODIwMDEwLCJleHAiOjE2MTE5MjQwMTB9.u_JWKY4QxbjbYCUV0zB-e-qFz6gsLSm-pyA-Ehzyzbc\ncookie: mp_67769c00e598aec9b31e7e6024ff0a0b_mixpanel=%7B%22distinct_id%22%3A%20%221700a885b5997c-0e29edf2d8ff75-39617b0f-13c680-1700a885b5a559%22%2C%22%24device_id%22%3A%20%221700a885b5997c-0e29edf2d8ff75-39617b0f-13c680-1700a885b5a559%22%2C%22%24initial_referrer%22%3A%20%22%24direct%22%2C%22%24initial_referring_domain%22%3A%20%22%24direct%22%7D; mp_130bd02cca41cfbfe0e1a114e6dee2b1_mixpanel=%7B%22distinct_id%22%3A%20%221700a885b6612f-0a4815416b1751-39617b0f-13c680-1700a885b6790f%22%2C%22%24device_id%22%3A%20%221700a885b6612f-0a4815416b1751-39617b0f-13c680-1700a885b6790f%22%2C%22%24initial_referrer%22%3A%20%22%24direct%22%2C%22%24initial_referring_domain%22%3A%20%22%24direct%22%7D; amplitude_id_3c27150f9653ce05775c537209a3ee40retailio.in=eyJkZXZpY2VJZCI6IjdlMDViYWZlLTc2NmItNGUyNi1iZGE4LTMxYzViM2ZiZjhjMlIiLCJ1c2VySWQiOiIyODc0Iiwib3B0T3V0IjpmYWxzZSwic2Vzc2lvbklkIjoxNTgxMDc3MTIzODA0LCJsYXN0RXZlbnRUaW1lIjoxNTgxMDc3MTI1MzI3LCJldmVudElkIjoxODIsImlkZW50aWZ5SWQiOjMwLCJzZXF1ZW5jZU51bWJlciI6MjEyfQ==")
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
    val qty = 100
    0.to(num - 1).map(index => shuffled(index)).map(e =>
      s"""
         |  {
         |    "id":${e(0)},
         |    "quantity": ${qty},
         |    "name": "${name}"
         |  }""".stripMargin).mkString(",\n")
  }

  private val medsFeeder = Iterator.continually(Map("items" -> getPayload(1)))

  private val payload: String =
    """
      |{
      |  "orderItems": [
      |    ${items}
      |  ]
      |}""".stripMargin


  private val createB2BOrders = scenario("AsynchronousTest")
    .feed(retailerFeeder)
    .feed(medsFeeder)
    .exec(http("AsynchronousAPIs")
      .post("/api/Distributors/1670/Retailers/${retailerId}/placeOrder")
      .body(StringBody(payload))
      .check(status.is(200), jsonPath("$.orderGroupId").notNull.saveAs("orderGroupId")))
    .exec(session => {
      writeFile("orderGroupId.csv", session("orderGroupId").as[String] + "\n");
      session
    }
    )

  setUp(
    createB2BOrders.inject(rampUsers(System.getProperty("b2bRampUpUsers", "40").toInt) during (System.getProperty("b2bRampUpDuration", "2").toInt seconds))
  ).protocols(httpProtocol)
}
