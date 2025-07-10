package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import actions.PartnerActions
import utils.ConfigManager

class PartnerSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("https://tc-partner-master.private.thyrocare.com")
    .acceptHeader("application/json")

  val scn = scenario("Partner Load Test Scenario")
    .exec(PartnerActions.getPartnerDetailsByPartnerIdentity)
    .pause(1)
    .exec(PartnerActions.getPartnerDetailsById)
    .pause(1)
    .exec(PartnerActions.getEntityConfigById)
    .pause(1)
    .exec(PartnerActions.getEntityConfigByIdentity)

  setUp(
    scn.inject(
      rampUsersPerSec(1) to ConfigManager.getInt("multiple.testRampUpTargetRate", 1) during (ConfigManager.getInt("multiple.testRampUpDuration", 1).seconds),
      constantUsersPerSec(ConfigManager.getFloat("users", 1)) during (ConfigManager.getInt("duration", 3).seconds)
    )
  ).protocols(httpProtocol)
}
