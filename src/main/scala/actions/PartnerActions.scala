package actions

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object PartnerActions {

  val feeder = csv("partner_detail.csv").random

  val headers = Map(
    "tc-partner-service-token" -> "aZ4K9tXfY6PQ1mUnRb7LoJcTzvA3NeM82HDqk5gWRpFYVbMJ0x"
  )

  val getPartnerDetailsByPartnerId = feed(feeder)
    .exec(
      http("GET Partner Details by partner_id")
        .get("/v1/partner/details?partner_id=${entity_id}")
        .headers(headers)
        .check(status.is(200))
    )

  val getPartnerDetailsByIdentity = feed(feeder)
    .exec(
      http("GET Partner Details by id")
        .get("/v1/partner/details?id=${entity_identity}")
        .headers(headers)
        .check(status.is(200))
    )

  val getEntityConfigById = feed(feeder)
    .exec(
      http("GET Entity Configs by entity_id")
        .get("/v1/config-master/get-entity-configs?entity_id=${entity_id}&entity_type=PARTNER")
        .headers(headers)
        .check(status.is(200))
    )

  val getEntityConfigByIdentity = feed(feeder)
    .exec(
      http("GET Entity Configs by entity_identity")
        .get("/v1/config-master/get-entity-configs?entity_identity=${entity_identity}&entity_type=PARTNER")
        .headers(headers)
        .check(status.is(200))
    )
}
