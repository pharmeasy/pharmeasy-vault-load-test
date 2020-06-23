package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.commons.validation._
import scala.concurrent.duration._
import utils.ConfigManager._

class Test12 extends Simulation {
//  private val httpProtocol: io.gatling.http.protocol.HttpProtocolBuilder = http.baseUrl("https://staging.thea.gomercury.in/api/data")
//    .authorizationHeader(TokenGeneration.getDefaultToken())
//
//  def searchInventory() =
//    http("Search Inventory")
//      .get("/inventory/v1?search=I32481&bin=L2-R15-S02-001")
//      .asJson
//      .check(
//        status.is(200),
//        jsonPath("$.data[*].ucode").is("I32481"))
//
//  def name(): String = getString("search - inventory")
//  val scn = scenario(name)
//    .exec(searchInventory()).exitHereIfFailed

//  val httpConf=http.baseUrl("https://staging.thea.gomercury.in/api/data")
//      .header("Authorization",TokenGeneration.getDefaultToken())
//
//  val scenario1=scenario("Execute Search Inventory-3 calls")
//      .exec(http("Execute multipicking").get("/inventory/v1?search=I32481&bin=L2-R15-S02-001")).pause(2)
//
//    .exec(http("Repeat Search Inventory - 2")
//    .get("/inventory/v1?search=I32481&bin=L2-R15-S02-001")).pause(1,20)
//
//    .exec(http("Repeat Search Inventory - 3")
//      .get("/inventory/v1?search=I32481&bin=L2-R15-S02-001")).pause(2000.milliseconds)
//
//  setUp(scenario1.inject(rampUsers(2)during(2 seconds))).protocols(httpConf)
}