package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import actions.PartnerActions

class PartnerSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("https://tc-partner-master.private.thyrocare.com")
    .acceptHeader("application/json")

  val scn = scenario("Partner Load Test Scenario")
    .exec(PartnerActions.getPartnerDetailsByPartnerId)
    .pause(1)
    .exec(PartnerActions.getPartnerDetailsByIdentity)
    .pause(1)
    .exec(PartnerActions.getEntityConfigById)
    .pause(1)
    .exec(PartnerActions.getEntityConfigByIdentity)

  setUp(
    scn.inject(
      constantUsersPerSec(5).during(30.seconds),
      constantUsersPerSec(10).during(30.seconds),
      constantUsersPerSec(15).during(30.seconds)
    ).protocols(httpProtocol)
  )
}
