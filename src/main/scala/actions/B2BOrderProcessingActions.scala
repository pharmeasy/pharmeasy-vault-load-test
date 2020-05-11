package actions

import scm.CreateB2BOrderPayload
import actions.ConsumerActions.getFromSession
import io.gatling.core.Predef.{jsonPath, _}
import io.gatling.http.Predef._
import utils.ConfigManager._
import newUtilities.TokenGeneration

object B2BOrderProcessingActions extends BaseActions {

  def addToSession(session: Session, attributes: (String, Any)*) = session.setAll(attributes)

  def createB2BOrders(baseUrl: String = getString("outward.base_url"), thea: String = getString("outward.thea")) =
    http("Create B2B Order")
      .post(session => baseUrl + "/rioOrder?tenant=" + thea)
      .body(StringBody(CreateB2BOrderPayload.getOrderPayload()))
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

  def billingInProgress(baseUrl: String = getString("outward.base_url"), thea: String = getString("outward.thea")) =
    http("updating the order status to 'Billing In Progress'")
      .put(session => baseUrl + s"outward/orders/${getFromSession(session, "externalOrderId", "")}/status/")
      .header("Authorization", TokenGeneration.getDefaultToken(thea))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("BILLING_IN_PROGRESS"),
        jsonPath("$.externalOrderId").is(session => getFromSession(session, "externalOrderId", ""))

      )

  def generateStoreInvoice(baseUrl: String = getString("outward.base_url"), thea: String = getString("outward.thea")) =
    http("generating Store invoice")
      .put(session => baseUrl + s"outward/orders/${getFromSession(session, "externalOrderId", "")}/status/storeInvoice")
      .header("Authorization", TokenGeneration.getDefaultToken(thea))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("STORE_INVOICE_GENERATED"),
        jsonPath("$.externalOrderId").is(session => getFromSession(session, "externalOrderId", ""))

      )

  def generateDispatchNote(baseUrl: String = getString("outward.base_url"), thea: String = getString("outward.thea")) =
    http("generating Dispatch Note")
      .get(session => baseUrl + s"outward/orders/${getFromSession(session, "externalOrderId", "")}/customer/dispatchNote")
      .header("Authorization", TokenGeneration.getDefaultToken(thea))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.externalOrderId").is(session => getFromSession(session, "externalOrderId", ""))

      )

  def recievedAtStore(baseUrl: String = getString("outward.base_url"), thea: String = getString("outward.thea")) =
    http("updating the order status to 'Recieved At Store'")
      .put(session => baseUrl + s"outward/orders/${getFromSession(session, "externalOrderId", "")}/status")
      .header("Authorization", TokenGeneration.getDefaultToken(thea))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("RECEIVED_AT_STORE"),
        jsonPath("$.externalOrderId").is(session => getFromSession(session, "externalOrderId", ""))

      )

  def generateCustomerInvoice(baseUrl: String = getString("outward.base_url"), storeId: String = getString("outward.storeId"), thea: String = getString("outward.thea")) =
    http("generating Customer invoice")
      .put(session => baseUrl + s"outward/orders/${getFromSession(session, "externalOrderId", "")}/status/customerInvoice?storeId="+storeId)
      .header("Authorization", TokenGeneration.getDefaultToken(thea))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("CUSTOMER_INVOICE_GENERATED"),
        jsonPath("$.externalOrderId").is(session => getFromSession(session, "externalOrderId", "")),


      )

}

