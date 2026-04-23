package simulations

import in.pharmeasy.outward.api.helpers.TokenGeneration
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.commons.validation._

import scala.concurrent.duration._
import utils.ConfigManager._

class Test12 extends Simulation {
  private val httpProtocol: io.gatling.http.protocol.HttpProtocolBuilder = http.baseUrl("https://staging.thea.onprem.scm.gomercury.in/api/ledger")
    .authorizationHeader(TokenGeneration.getDefaultToken())

  def getOutstanding() =
    http("Get Outstanding")
      .get("/ledgers/outstanding?distributorId=1891&retailerId=40871")
      .asJson
      .check(
        status.is(200),
        jsonPath("$.data[*].ucode").is("I32481"))

  def name(): String = getString("Get Outstanding")
  val scn = scenario(name)
    .exec(getOutstanding()).exitHereIfFailed

  val httpConf = http.baseUrl("https://staging.thea.onprem.scm.gomercury.in/api/ledger")
    .header("Authorization", TokenGeneration.getDefaultToken())

  val scenario1 = scenario("Execute GetOutstanding")
    .exec(http("Execute GetOutstanding").get("/ledgers/outstanding?distributorId=1891&retailerId=40871")).pause(2)

  setUp(scenario1.inject(
      rampUsersPerSec(1).to(System.getProperty("constantUsers", "20").toInt).during(30.seconds), // warmup
      constantUsersPerSec(System.getProperty("constantUsers", "20").toInt).during(System.getProperty("duration", "200").toInt.seconds)
    )).protocols(httpConf)}