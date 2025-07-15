package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import actions.PartnerActions

class PartnerSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("https://tc-partner-master.private.thyrocare.com")
    .acceptHeader("application/json")

  val scn5  = PartnerActions.scenarioWithCsv("partner_detail_5rps.csv", "5 RPS")
  val scn10 = PartnerActions.scenarioWithCsv("partner_detail_10rps.csv", "10 RPS")
  val scn15 = PartnerActions.scenarioWithCsv("partner_detail_15rps.csv", "15 RPS")

 setUp(

   scn5.inject(rampUsersPerSec(1) to 5 during (60.seconds), constantUsersPerSec(5) during (240.seconds)),
   scn10.inject(rampUsersPerSec(5) to 10 during (60.seconds), constantUsersPerSec(2) during (240.seconds)),
   scn15.inject(rampUsersPerSec(10) to 15 during (60.seconds), constantUsersPerSec(3) during (240.seconds))
 ).protocols(httpProtocol)

}
