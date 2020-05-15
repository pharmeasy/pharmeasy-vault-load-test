package actions

import actions.redbook.{Medicines, RedbookBillOrder, RedbookOrderCreate, RedbookOrderUpdate}
import io.gatling.http.request.builder.HttpRequestBuilder
import io.gatling.core.Predef.{jsonPath, _}
import io.gatling.core.session.Session
import io.gatling.core.structure._
import io.gatling.http.Predef._
import newUtilities.newConfigManager
import org.json4s.DefaultFormats

object RedbookOrderProcessingActions {

  protected def getFromSession(session: Session, key: String, defaultValue: String = null): String
  = session(key).asOption[String].getOrElse(defaultValue)

  def addToSession(session: Session, attributes: (String, Any)*) = session.setAll(attributes)

  implicit val formats = DefaultFormats


  def createOrderInRedBook(baseUrl: String = newConfigManager.getString("redbook.base_url")) = {
    http("create redbook order")
      .post(baseUrl + "/service/v1/pe/${retailerRedbookId}/order")
      .body(StringBody(RedbookOrderCreate.getOrderPayload()))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("PLACED"),
        jsonPath("$.reference_order_id").saveAs("reference_order_id"),
        jsonPath("$._id").saveAs("redbook_id"),
        jsonPath("$.app_id").saveAs("app_id"),
        jsonPath("$.meta").saveAs("meta"),
        jsonPath("$.medicines").saveAs("meds"),
        bodyString.saveAs("create_response"))
  }




  def getOrderByPEId(baseUrl: String = newConfigManager.getString("redbook.base_url")) =
    http("Get order details by PE Id")
    .get(baseUrl + "/service/v0/order")
    .queryParam("reference_order_id",session=>getFromSession(session,"fetch_reference_order_id"))
    .asJson
    .check(status.is(200),
      jsonPath("$.status").saveAs("status"))

  def updateStatus(baseUrl: String = newConfigManager.getString("redbook.base_url")
                              ,updateStatus: String,updatePayload:String):HttpRequestBuilder =
    http("update redbook order")
      .patch( baseUrl+"/service/v0/pe/order/"+updateStatus)
      .queryParam("reference_order_id",session=>getFromSession(session,"fetch_reference_order_id"))
      .body(StringBody(updatePayload))
      .asJson
      .check(status.is(200))

  def update(baseUrl: String = newConfigManager.getString("redbook.base_url"),
             updateString: String):ChainBuilder = {
    var updatePayload:String = null

    updateString match {
      case "ACCEPTED" => updatePayload = RedbookOrderUpdate.getAccepted()
      case "REJECTED" => updatePayload = RedbookOrderUpdate.getRejected()
      case "ON_HOLD" => updatePayload = RedbookOrderUpdate.getOnHold()
      case "CANCELLED" => updatePayload = RedbookOrderUpdate.getCancelled()
      case "READY_FOR_DISPATCH" => updatePayload = RedbookOrderUpdate.getRFD()
      case "DELIVERED" => updatePayload = RedbookOrderUpdate.getDelivered()
    }

    exec(updateStatus(baseUrl,updateString,updatePayload))
     .exitHereIfFailed
     .exec(getOrderByPEId())
  }

  def statusUpdates(baseUrl: String = newConfigManager.getString("redbook.base_url")): ChainBuilder = {
    doWhile(session => !(getFromSession(session, "status").equals("CANCELLED")) &&
      !(getFromSession(session, "status").equals("DELIVERED")))(
      doSwitch(session => getFromSession(session, "status"))(
        "PLACED" -> randomSwitch(
          90.0 -> update(baseUrl, "ACCEPTED"),
          5.0 -> update(baseUrl, "REJECTED"),
          5.0 -> update(baseUrl, "CANCELLED")
        ),
        "ACCEPTED" -> randomSwitch(
          90.0 -> exec(billTheOrder()).exitHereIfFailed.exec(getOrderByPEId()),
          5.0 -> update(baseUrl, "ON_HOLD"),
          5.0 -> update(baseUrl, "CANCELLED")
        ),
        "BILLED" -> randomSwitch(
          90.0 -> update(baseUrl, "READY_FOR_DISPATCH"),
          10.0 -> update(baseUrl, "CANCELLED")
        ),
        "ON_HOLD" -> randomSwitch(
          90.0 -> exec(billTheOrder()).exitHereIfFailed.exec(getOrderByPEId()),
          10.0 -> update(baseUrl, "CANCELLED")
        ),
        "READY_FOR_DISPATCH" -> update(baseUrl, "DELIVERED")

      )
    )
  }

  def billTheOrder(baseUrl: String = newConfigManager.getString("redbook.base_url")):HttpRequestBuilder =
    http("Bill the Redbook Order")
      .post(session =>baseUrl + "/service/v1/pe/"+getFromSession(session,"fetch_app_id")+"/order")
      .queryParam("reference_order_id",session=>getFromSession(session,"fetch_reference_order_id"))
      .body( StringBody(session => RedbookBillOrder
        .getBillPayload(session("fetch_meds").as[Array[Medicines]],
          session("fetch_app_id").as[String],
          session("fetch_reference_order_id").as[String])))
      .asJson
      .check(status.is(200),
        jsonPath("$.status").is("BILLED"))


}
