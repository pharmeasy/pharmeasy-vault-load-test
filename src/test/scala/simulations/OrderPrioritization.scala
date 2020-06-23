package simulations

import actions.OrderProcessingActions.addToSession
import actions.scm.OrderPayloadCreation
import io.gatling.core.Predef._
import io.gatling.http.Predef.{http, _}
import newUtilities._

import scala.concurrent.duration._

class OrderPrioritization extends io.gatling.core.Predef.Simulation {

  private val random = scala.util.Random

  private val httpProtocol: io.gatling.http.protocol.HttpProtocolBuilder = http.baseUrl("https://qa2.thea.gomercury.in")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .authorizationHeader(TokenGeneration.getDefaultToken())
    .disableWarmUp.disableCaching

  private val addRule: String =
    """
      |{
      |  "days": [ 6,7],
      |  "timeFrom": "05:15:04",
      |  "timeTo": "12:36:04",
      |  "bucketSize": 100,
      |  "tree": [
      |    {
      |      "id": 1,
      |      "type": "ALL",
      |      "treeNode": [
      |        {
      |          "child": 2,
      |          "weight": 10
      |        },
      |        {
      |          "child": 3,
      |          "weight": 90
      |        }
      |      ],
      |      "hardRule": true
      |    },
      |    {
      |      "id": 2,
      |      "type": "COURIER",
      |      "treeNode": []
      |      ,
      |      "hardRule": false
      |    },
      |    {
      |      "id": 3,
      |      "type": "N_COURIER",
      |      "treeNode": [
      |      	{
      |          "child": 4,
      |          "weight": 60
      |        },
      |        {
      |          "child": 5,
      |          "weight": 40
      |        }],
      |      "hardRule": false
      |    },
      |    {
      |      "id": 4,
      |      "type": "REFRIG",
      |      "treeNode": [],
      |      "hardRule": false
      |    },
      |    {
      |      "id": 5,
      |      "type": "N_REFRIG",
      |      "treeNode": [],
      |      "hardRule": false
      |    }
      |  ]
      |}
    """.stripMargin

  private val addOrderPrioritizationRule = scenario("Order Prioritization")

    .exec(http("Add Order Prioritization Config")
      .post("/api/outward/orderPriortisation/tree")
      .body(StringBody(addRule))
      .check(status.is(200))
   )



  setUp(
    addOrderPrioritizationRule.inject(rampUsers(System.getProperty("b2cRampUpUsers", "2").toInt) during (System.getProperty("b2cRampUpDuration", "2").toInt seconds)),
  ).protocols(httpProtocol)
}
