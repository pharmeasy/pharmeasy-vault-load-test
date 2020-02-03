package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef.{http, _}

import scala.concurrent.duration._

class B2BOrders extends io.gatling.core.Predef.Simulation {

  private val random = scala.util.Random

  private val httpProtocol: io.gatling.http.protocol.HttpProtocolBuilder = http
    .baseUrl("https://staging.retailio.in")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .authorizationHeader("Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vb21zLmNvbSIsIm9tc1JvbGVJZCI6Niwib21zUm9sZSI6IkRJU1RSSUJVVE9SU1VQRVJTQUxFU01BTiIsIm9tc1VzZXJJZCI6MjU3MSwianRpIjoiNTlkOWUyNGUtMDk3OC00NjdkLWEyY2YtY2NkZGRkNzlmYjQyIiwiaWF0IjoxNTgwNzI1MTc0LCJleHAiOjE2MTE4MjkxNzR9.2uHTx4Nb_MBkTnbM5Bbm3m-oKVJRfgkyjIaTSxR_t-o")
    .header("source","web")
    .header("version","1.2.3")
    .disableWarmUp
    .disableCaching

  val csvFeeder = csv("orderItemId.csv").eager.random.circular
  val start = 1
  val end   = 30
  val rnd = new scala.util.Random
  val randomFeeder = Iterator.continually(Map("qty"->(start + rnd.nextInt( (end - start) + 1 ))))
  val retailer = csv("retailers.csv").eager.random.circular

  val alpha = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
  val size = alpha.size
  def randStr(n:Int) = (1 to n).map(x => alpha(scala.util.Random.nextInt.abs % size)).mkString

  val nameFeeder = Iterator.continually(Map("name"->randStr(20)))


  private val payload: String =
    """
      |{
      |  "orderItems": [
      |    {
      |      "id":${medId},
      |      "quantity": ${qty},
      |      "name": "${name}"
      |    }
      |  ]
      |}""".stripMargin

  private val createB2BOrders = scenario("AsynchronousTest")
    .feed(csvFeeder)
    .feed(randomFeeder)
    .feed(retailer)
    .feed(nameFeeder)
    .exec(http("AsynchronousAPIs")
      .post("/api/Distributors/405/Retailers/${retailerId}/placeOrder")
      .body(StringBody(payload))
      .check(status.is(200)))

  setUp(
    createB2BOrders.inject(rampUsers(System.getProperty("b2bRampUpUsers", "3").toInt) during (System.getProperty("b2bRampUpDuration", "10").toInt seconds))
  ).protocols(httpProtocol)
}
