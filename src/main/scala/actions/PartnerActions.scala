package actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object PartnerActions {

  val headers = Map(
    "tc-partner-service-token" -> "aZ4K9tXfY6PQ1mUnRb7LoJcTzvA3NeM82HDqk5gWRpFYVbMJ0x"
  )

  def scenarioWithCsv(csvFile: String, label: String) = {
    val feeder = csv(csvFile).circular

    scenario(s"Load Test - $label")
      .feed(feeder)
      .exec(
        http("GET Partner Details by identity")
          .get("/v1/partner/details?partner_identity=${entity_identity}")
          .headers(headers)
          .check(status.is(200))
      )
      .pause(1)
      .exec(
        http("GET Partner Details by ID")
          .get("/v1/partner/details?id=${entity_id}")
          .headers(headers)
          .check(status.is(200))
      )
      .pause(1)
      .exec(
        http("GET Configs by ID")
          .get("/v1/config-master/get-entity-configs?entity_id=${entity_id}&entity_type=PARTNER")
          .headers(headers)
          .check(status.is(200))
      )
      .pause(1)
      .exec(
        http("GET Configs by identity")
          .get("/v1/config-master/get-entity-configs?entity_identity=${entity_identity}&entity_type=PARTNER")
          .headers(headers)
          .check(status.is(200))
      )
  }
}