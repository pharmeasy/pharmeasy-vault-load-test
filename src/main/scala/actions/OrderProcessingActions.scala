package actions

import scm.OrderPayloadCreation
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.builder.HttpRequestBuilder
import newUtilities.{TokenGeneration, newConfigManager}

object OrderProcessingActions extends BaseActions {
  def addToSession(session: Session, attributes: (String, Any)*): Session = session.setAll(attributes)

  def CreateB2BOrders(baseUrl: String = newConfigManager.getString("outward.base_url")) =
    http("Create B2C Order")
      .post(session => baseUrl + "/api/outward/orders")
      .header("accept", "application/json")
      .header("contentType", "application/json")
      .header("Authorization", TokenGeneration.getDefaultToken())
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


  def prioritisePickerTask(baseUrl: String = newConfigManager.getString("outward.base_url")): HttpRequestBuilder =
    http("Prioritise the picker task")
      .put(session => baseUrl + s"/pickerTasks/${getFromSession(session, "pickerTaskId")}/prioritise")
      .body(StringBody(session =>OrderPayloadCreation.getPrioritisePickerTaskConfig(s"${ getFromSession(session, "externalOrderId")}")))
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
        jsonPath("$.aggregatePickerTask.pickerId").is(session => s"${getFromSession(session, "pickerId")}"),
        jsonPath("$.aggregatePickerTask.taskCount").is("1"))

}
