package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import actions.PartnerActions

class PartnerSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("https://staging.thea.onprem.scm.gomercury.in/api/ledger/ledgers/outstanding?distributorId=1891&retailerId=40871")
    .acceptHeader("application/json")

  val scn5  = PartnerActions.scenarioWithCsv("partner_detail_5rps.csv", "5 RPS")
  val scn10 = PartnerActions.scenarioWithCsv("partner_detail_10rps.csv", "10 RPS")
  val scn15 = PartnerActions.scenarioWithCsv("partner_detail_15rps.csv", "15 RPS")
  val rampDuration = System.getProperty("rampDuration", "60").toInt
  val constantDuration = System.getProperty("constantDuration", "240").toInt
  val delayBetweenScenarios = System.getProperty("delay", "300").toInt

  val rampUsers5 = System.getProperty("rampUsers5", "5").toInt
  val constantUsers5 = System.getProperty("constantUsers5", "5").toInt

  val rampUsers10 = System.getProperty("rampUsers10", "10").toInt
  val constantUsers10 = System.getProperty("constantUsers10", "2").toInt

  val rampUsers15 = System.getProperty("rampUsers15", "15").toInt
  val constantUsers15 = System.getProperty("constantUsers15", "3").toInt

 setUp(
   scn5.inject(
     rampUsersPerSec(1) to rampUsers5 during (rampDuration.seconds),
     constantUsersPerSec(constantUsers5) during (constantDuration.seconds)
   ),
   scn10.inject(
     nothingFor(delayBetweenScenarios.seconds),
     rampUsersPerSec(1) to rampUsers10 during (rampDuration.seconds),
     constantUsersPerSec(constantUsers10) during (constantDuration.seconds)
   ),
   scn15.inject(
     nothingFor((delayBetweenScenarios * 2).seconds),
     rampUsersPerSec(10) to rampUsers15 during (rampDuration.seconds),
     constantUsersPerSec(constantUsers15) during (constantDuration.seconds)
   )
 ).protocols(httpProtocol)

  System.out.println("jdk.tls.client.protocols=" + System.getProperty("jdk.tls.client.protocols"))
}