package simulations

import java.util.Random

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
    .authorizationHeader("Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vb21zLmNvbSIsIm9tc1JvbGVJZCI6Niwib21zUm9sZSI6IkRJU1RSSUJVVE9SU1VQRVJTQUxFU01BTiIsIm9tc1VzZXJJZCI6MjU3MSwianRpIjoiYzIzNTI0YWQtNTQwYy00M2RlLWFjZmQtNzc0OTE3NjZkNGFlIiwiaWF0IjoxNTgwMjA1MDc5LCJleHAiOjE2MTEzMDkwNzl9.tXEV9Qy7kHQJfiMORtGpT49TyRywaRQC0UHg5hmoDbU")
    .header("source","web")
    .header("version","1.2.3")
    .disableWarmUp
    .disableCaching

  val csvFeeder = csv("orderItemId.csv").eager.random
  val randomNumberFeeder = Iterator.continually(Map("qty"->new Random().nextInt(100)))
  val retailer = csv("retailers.csv")

  private val payload: String =
    """
      |{
      |  "orderItems": [
      |    {
      |      "id":${medId},
      |      "quantity": 3
      |    }
      |  ]
      |}""".stripMargin



  private val createB2BOrders = scenario("AsynchronousTest")
    .feed(csvFeeder)
    .feed(randomNumberFeeder)
    .feed(retailer)
    .exec(http("AsynchronousAPIs")
      .post("/api/Distributors/25/Retailers/${retailerId}/placeOrder")
      .body(StringBody(payload))
      .check(status.is(200)))

  setUp(
    createB2BOrders.inject(rampUsers(System.getProperty("b2bRampUpUsers", "1").toInt) during (System.getProperty("b2bRampUpDuration", "2").toInt seconds))
  ).protocols(httpProtocol)
}
