package actions

import scm.CreateB2BOrderPayload
import actions.ConsumerActions.getFromSession
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import utils.ConfigManager._
import newUtilities.TokenGeneration

object B2BOrderProcessingActions extends BaseActions {

  def addToSession(session: Session, attributes: (String, Any)*) = session.setAll(attributes)

  def createB2BOrders(baseUrl: String = getString("outward.base_url"), thea: String = getString("outward.thea")) =
    http("Create B2B Order")
      .post(session => baseUrl + "/rioOrder?tenant=" + thea)
      .body(StringBody(CreateB2BOrderPayload.createOrder()))
      .asJson
      .check(
        status.is(200))


  def fetchOrderId(baseUrl: String = getString("outward.base_url"), thea: String = getString("outward.thea")) =
    http("Fetch Order Id")
      .get(session => baseUrl + s"/omsOrder/customerOrder/${getFromSession(session, "customerId", "")}")
      .header("Authorization", TokenGeneration.getDefaultToken(thea))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.customerOrderId").is(session => getFromSession(session, "customerId", ""))
      )

}

