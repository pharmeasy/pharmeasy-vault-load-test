package actions

import scm.OrderPayloadCreation
import scm.CreateB2BOrderPayload
import scm.CreateGenerateBillPayload
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder
import newUtilities.{TokenGeneration, newConfigManager}
import utils.ConfigManager.getString

object OrderProcessingActions extends BaseActions {
  def addToSession(session: Session, attributes: (String, Any)*): Session = session.setAll(attributes)

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
      .header("Authorization", TokenGeneration.getDefaultToken())
      .asJson
      .check(
        status.is(200),
        jsonPath("$.customerOrderId").is(session => getFromSession(session, "customerId", ""))
      )

  def CreateB2COrders(baseUrl: String = newConfigManager.getString("outward.base_url")) =
    http("Create B2C Order")
      .post(session => baseUrl + "/api/outward/orders")
      .header("accept", "application/json")
      .header("contentType", "application/json")
      .body(StringBody(OrderPayloadCreation.getOrderPayload()))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.orderGroupId").notNull)


  def configureMultiPicking(baseUrl: String = newConfigManager.getString("multi.picking.config")) =
    http("Configure Multi Picking")
      .put(session => baseUrl + "/multiPickingConfig")
      .body(StringBody(OrderPayloadCreation.getMultiPickingConfigPayload()))
      .asJson
      .check(status.is(200),
        jsonPath("$.id").notNull)

  def getPickerTaskFromEpicenter(baseUrl: String = newConfigManager.getString("outward.base_url")): HttpRequestBuilder =
    http("Get picker task from epicenter")
      .get(session => baseUrl + "/pickerTasks")
      .queryParam("referenceId", session => getFromSession(session, "externalOrderId"))
      .check(status.is(200),
        jsonPath("$.elementsCount").is("1"),
        jsonPath("$.data[0].id").notNull.saveAs("pickerTaskId"))


  def generateBill(baseUrl: String = newConfigManager.getString("outward.base_url")): HttpRequestBuilder =
    http("Change the status of external order id to BILLING  IN PROGRESS")
      .put(session => baseUrl + s"/orders/${getFromSession(session, "externalOrderId")}/status")
      .body(StringBody(session => OrderPayloadCreation.getGenerateBillPayload("BILLING_IN_PROGRESS")))
      .asJson
      .check(status.is(200),
        jsonPath("$.status").is("BILLING_IN_PROGRESS": String),
        jsonPath("$.pickerTaskId").is(session => getFromSession(session, "pickerTaskId")))


  def prioritisePickerTask(baseUrl: String = newConfigManager.getString("outward.base_url")): HttpRequestBuilder =
    http("Prioritise the picker task")
      .put(session => baseUrl + s"/pickerTasks/${getFromSession(session, "pickerTaskId")}/prioritise")
      .body(StringBody(session => OrderPayloadCreation.getPrioritisePickerTaskConfig(s"${getFromSession(session, "externalOrderId")}")))
      .asJson
      .check(status.is(200),
        jsonPath("$.message").is("success": String))


  def signInToPickerApp(baseUrl: String = newConfigManager.getString("picker.signin.url")): HttpRequestBuilder =
    http("Sign In to Picker App")
      .post(session => baseUrl)
      .body(StringBody(OrderPayloadCreation.getSignInAppPayload()))
      .asJson
      .check(status.is(200),
        jsonPath("$.token").notNull.saveAs("token"),
        jsonPath("$.id").notNull.saveAs("pickerId"))

  def aggregateAssignedPickerTasks(baseUrl: String = newConfigManager.getString("outward.base_url")): HttpRequestBuilder =
    http("Get Aggregated Assigned Picker Task")
      .get(session => baseUrl + "/aggregatePickerTasks/assigned")
      .header("Authorization", session => s"${getFromSession(session, "token")}")
      .check(status.is(200),
        jsonPath("$.aggregatePickerTask.pickerId").is(session => s"${getFromSession(session, "pickerId")}"))


  def pickTray(baseUrl: String = newConfigManager.getString("outward.base_url")): HttpRequestBuilder =
    http("Pick Tray for task")
      .put(session => baseUrl + newConfigManager.getString("outward.pick_tray"))
      .header("Authorization", session => s"${getFromSession(session, "token")}")
      .body(StringBody(session => OrderPayloadCreation.getPickTrayPayload(s"${getFromSession(session, "trayId")}")))
      .asJson
      .check(status.is(200),
        jsonPath("$.aggregatePickerTask.id").notNull.saveAs("aggregatedPickerTaskId"),
        jsonPath("$.aggregatePickerTask.status").is("PICKED"),
        jsonPath("$.aggregatePickerTask.pickerId").is(session => s"${getFromSession(session, "pickerId")}"))


  def aggregatePickerTaskPicked(baseUrl: String = newConfigManager.getString("outward.base_url")): HttpRequestBuilder =
    http("Pick Tray for task")
      .put(session => baseUrl + s"/aggregatePickerTasks/${getFromSession(session, "aggregatedPickerTaskId")}/picked")
      .header("Authorization", session => s"${getFromSession(session, "token")}")
      .body(StringBody(OrderPayloadCreation.getTaskPickedPayload()))
      .asJson
      .check(status.is(200),
        jsonPath("$.nextPickerTaskItem.ucode").notNull.saveAs("ucode"),
        jsonPath("$.nextPickerTaskItem.binId").notNull.saveAs("bin"),
        jsonPath("$.nextPickerTaskItem.status").is("CREATED"),
        jsonPath("$.nextPickerTaskItem.pickerTaskId").is(session => getFromSession(session, "pickerTaskId")))

  def searchInventoryPostTaskPicked(baseUrl: String = newConfigManager.getString("search_inventory_url")): HttpRequestBuilder =
    http("Search Inventory")
      .get(baseUrl)
      .header("Authorization", session => s"${getFromSession(session, "token")}")
      .queryParam("search", session => s"${getFromSession(session, "ucode")}")
      .queryParam("bin", session => s"${getFromSession(session, "bin")}")
      .asJson
      .check(status.is(200),
        jsonPath("$.elements").notNull,
        jsonPath("$.data[0].ucode").is(session => s"${getFromSession(session, "ucode")}"),
        jsonPath("$.data[0].bin").is(session => s"${getFromSession(session, "bin")}"),
        jsonPath("$.data[0].batch").notNull.saveAs("batch"))


  def getBarcodes(baseUrl: String = newConfigManager.getString("product_url")): HttpRequestBuilder =
    http("Get barcodes based on ucode and bin")
      .get(baseUrl)
      .queryParam("ucode", session => s"${getFromSession(session, "ucode")}")
      .queryParam("bin", session => s"${getFromSession(session, "bin")}")
      .queryParam("size", "1")
      .asJson
      .check(status.is(200),
        jsonPath("$.data[*].barcode").findAll.saveAs("barcodes"), jsonPath("$.data[*].batch").findAll.saveAs("batch"))


  def pickedItems(baseUrl: String = newConfigManager.getString("outward.base_url")): HttpRequestBuilder =
    http("Picked Items")
      .patch(session => baseUrl + s"/pickerTasks/${getFromSession(session, "pickerTaskId")}/pickedItems")
      .body(StringBody(session => OrderPayloadCreation.getPickedItemsPayload(s"${getFromSession(session, "bin")}", s"${getFromSession(session, "ucode")}", s"${getFromSession(session, "barcodes")}")))
      .asJson
      .check(status.is(200),
        jsonPath("$.message").is("success"))

  def completePickedItems(baseUrl: String = newConfigManager.getString("outward.base_url")): HttpRequestBuilder =
    http("Complete Picked Items")
      .put(session => baseUrl + s"/aggregatePickerTasks/${getFromSession(session, "aggregatedPickerTaskId")}/completePickedItems")
      .header("Authorization", session => getFromSession(session, "token"))
      .body(StringBody(session => OrderPayloadCreation.getCompletePickedItemsPayload(s"${getFromSession(session, "bin")}",
        s"${getFromSession(session, "ucode")}",
        s"${getFromSession(session, "pickerTaskId")}")))
      .asJson
      .check(status.is(200),
        jsonPath("$.aggregatePickerTask.id").is(session => s"${getFromSession(session, "aggregatedPickerTaskId")}"),
        jsonPath("$.aggregatePickerTask.pickerId").is(session => s"${getFromSession(session, "pickerId")}"),
        jsonPath("$.aggregatePickerTask.status").is("ZONE_SCANNING"),
        jsonPath("$.aggregatePickerTask.taskCount").is("1"),
        jsonPath("$.aggregatePickerTask.pickerTaskZones[0].pickerTaskId").is(session => s"${getFromSession(session, "pickerTaskId")}"),
        jsonPath("$.aggregatePickerTask.pickerTaskZones[0].status").is("PENDING_FOR_BILLING"))

  def scanZone(baseUrl: String = newConfigManager.getString("outward.base_url")): HttpRequestBuilder =
    http("Scan Zone post completion of picking")
      .put(session => baseUrl + s"aggregatePickerTasks/${getFromSession(session, "aggregatedPickerTaskId")}/scanZone")
      .header("Authorization", session => getFromSession(session, "token"))
      .body(StringBody(session => OrderPayloadCreation.getScanZonePayload(s"${getFromSession(session, "pickerTaskId")}")))
      .asJson
      .check(status.is(200),
        jsonPath("$.aggregatePickerTask.id").is(session => s"${getFromSession(session, "aggregatedPickerTaskId")}"),
        jsonPath("$.aggregatePickerTask.pickerId").is(session => s"${getFromSession(session, "pickerId")}"),
        jsonPath("$.aggregatePickerTask.status").is("COMPLETED"),
        jsonPath("$.aggregatePickerTask.taskCount").is("1"))

  //
  //  def billingInProgress(baseUrl: String = getString("outward.base_url"), thea: String = getString("outward.thea")) =
  //    http("updating the order status to 'Billing In Progress'")
  //      .put(session => baseUrl + s"outward/orders/${getFromSession(session, "externalOrderId", "")}/status/")
  //      .header("Authorization", TokenGeneration.getDefaultToken(thea))
  //      .asJson
  //      .check(
  //        status.is(200),
  //        jsonPath("$.status").is("BILLING_IN_PROGRESS"),
  //        jsonPath("$.externalOrderId").is(session => getFromSession(session, "externalOrderId", ""))
  //
  //      )
  //
  //  def generateStoreInvoice(baseUrl: String = getString("outward.base_url"), thea: String = getString("outward.thea")) =
  //    http("generating Store invoice")
  //      .put(session => baseUrl + s"outward/orders/${getFromSession(session, "externalOrderId", "")}/status/storeInvoice")
  //      .header("Authorization", TokenGeneration.getDefaultToken(thea))
  //      .asJson
  //      .check(
  //        status.is(200),
  //        jsonPath("$.status").is("STORE_INVOICE_GENERATED"),
  //        jsonPath("$.externalOrderId").is(session => getFromSession(session, "externalOrderId", ""))
  //
  //      )
  //
  //  def generateDispatchNote(baseUrl: String = getString("outward.base_url"), thea: String = getString("outward.thea")) =
  //    http("generating Dispatch Note")
  //      .get(session => baseUrl + s"outward/orders/${getFromSession(session, "externalOrderId", "")}/customer/dispatchNote")
  //      .header("Authorization", TokenGeneration.getDefaultToken(thea))
  //      .asJson
  //      .check(
  //        status.is(200),
  //        jsonPath("$.externalOrderId").is(session => getFromSession(session, "externalOrderId", ""))
  //
  //      )
  //
  //  def recievedAtStore(baseUrl: String = getString("outward.base_url"), thea: String = getString("outward.thea")) =
  //    http("updating the order status to 'Recieved At Store'")
  //      .put(session => baseUrl + s"outward/orders/${getFromSession(session, "externalOrderId", "")}/status")
  //      .header("Authorization", TokenGeneration.getDefaultToken(thea))
  //
  //      .asJson
  //      .check(
  //        status.is(200),
  //        jsonPath("$.status").is("RECEIVED_AT_STORE"),
  //        jsonPath("$.externalOrderId").is(session => getFromSession(session, "externalOrderId", ""))
  //
  //      )
  //
  //  def generateCustomerInvoice(baseUrl: String = getString("outward.base_url"), storeId: String = getString("outward.storeId"), thea: String = getString("outward.thea")) =
  //    http("generating Customer invoice")
  //      .put(session => baseUrl + s"outward/orders/${getFromSession(session, "externalOrderId", "")}/status/customerInvoice?storeId=" + storeId)
  //      .header("Authorization", TokenGeneration.getDefaultToken(thea))
  //      .asJson
  //      .check(
  //        status.is(200),
  //        jsonPath("$.status").is("CUSTOMER_INVOICE_GENERATED"),
  //        jsonPath("$.externalOrderId").is(session => getFromSession(session, "externalOrderId", "")))

  def generateStoreInvoice(baseUrl: String = getString("outward.base_url"), thea: String = getString("outward.thea")) =
    http("generating Store invoice")
      .put(session => baseUrl + s"/orders/${getFromSession(session, "externalOrderId", "")}/status/storeInvoice")
      .header("Authorization", session => s"${getFromSession(session, "biller_token")}")
      .body(StringBody(CreateGenerateBillPayload.getInvoicePayload("STORE_INVOICE_GENERATED")))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"),
        jsonPath("$.urls").notNull

      )

  def generateDispatchNote(baseUrl: String = getString("outward.base_url"), thea: String = getString("outward.thea")) =
    http("generating Dispatch Note")
      .get(session => baseUrl + s"/orders/${getFromSession(session, "externalOrderId", "")}/customer/dispatchNote")
      .header("Authorization", session => s"${getFromSession(session, "biller_token")}")
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("0"), jsonPath("$.url").notNull

      )

  def recievedAtStore(baseUrl: String = getString("outward.base_url"), thea: String = getString("outward.thea")) =
    http("updating the order status to 'Recieved At Store'")
      .put(session => baseUrl + s"/orders/${getFromSession(session, "externalOrderId", "")}/status")
      .header("Authorization", session => s"${getFromSession(session, "biller_token")}")
      .body(StringBody(CreateGenerateBillPayload.setOrderStatus("RECEIVED_AT_STORE")))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("RECEIVED_AT_STORE"),
        jsonPath("$.externalOrderId").is(session => getFromSession(session, "externalOrderId", ""))

      )

  def generateCustomerInvoice(baseUrl: String = getString("outward.base_url"), storeId: String = getString("outward.storeId"), thea: String = getString("outward.thea")) =
    http("generating Customer invoice")
      .put(session => baseUrl + s"/orders/${getFromSession(session, "externalOrderId", "")}/status/customerInvoice?storeId=" + storeId)
      .header("Authorization", session => s"${getFromSession(session, "biller_token")}")
      .body(StringBody(CreateGenerateBillPayload.getInvoicePayload("CUSTOMER_INVOICE_GENERATED")))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("CUSTOMER_INVOICE_GENERATED"),
        jsonPath("$.externalOrderId").is(session => getFromSession(session, "externalOrderId", "")),


      )

}