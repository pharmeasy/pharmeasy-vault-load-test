package actions

import actions.hydra.{HydraOrderCreation, HydraOrderUpdate}
import io.gatling.core.Predef.{jsonPath, _}
import newUtilities.{TokenGeneration, newConfigManager}
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.core.session.Session
import io.gatling.core.structure.ChainBuilder
import org.json4s.DefaultFormats

object HydraOrderProcessingActions {

  protected def getFromSession(session: Session, key: String, defaultValue: String = null): String = session(key).asOption[String].getOrElse(defaultValue)

  def addToSession(session: Session, attributes: (String, Any)*) = session.setAll(attributes)
  implicit val formats = DefaultFormats

  def CreateMarketPlaceOrder(baseUrl: String = newConfigManager.getString("hydra.base_url")) =
    http("Create Hydra Order")
      .post(session => baseUrl + "/orders")
      .header("accept","application/json")
      .header("contentType","application/json")
      .header("Authorization",TokenGeneration.getDefaultToken())
      .body(StringBody(HydraOrderCreation.getOrderPayload()))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("CREATED"),
        jsonPath("$.orderId").saveAs("orderId"),
        jsonPath("$.id").saveAs("id"))


  def GetByOrderId(baseUrl: String = newConfigManager.getString("hydra.base_url")) =
    http("Create Hydra Order")
      .get(session => baseUrl + "/orders/"+ getFromSession(session,"orderId"))
      .header("accept","application/json")
      .header("contentType","application/json")
      .header("Authorization",TokenGeneration.getDefaultToken())
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").saveAs("status"))

  def GetById(baseUrl: String = newConfigManager.getString("hydra.base_url")) =
    http("Create Hydra Order")
      .get(session => baseUrl + "/orders/gateway/"+ getFromSession(session,"id"))
      .header("accept","application/json")
      .header("contentType","application/json")
      .header("Authorization",TokenGeneration.getDefaultToken())
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").saveAs("status"))

  def UpdateOrder(baseUrl: String = newConfigManager.getString("hydra.base_url"),body:String) =
    http("Create Hydra Order")
      .post(session => baseUrl + "/orders/rb/"+getFromSession(session,"orderId")+"/status")
      .header("accept","application/json")
      .header("contentType","application/json")
      .header("Authorization",TokenGeneration.getDefaultToken())
      .body(StringBody(body))
      .asJson
      .check(
        status.is(200))

  def continue(baseUrl: String = newConfigManager.getString("hydra.base_url"))= {
    doWhile(session => !(getFromSession(session,"status").equals("CANCELLED")) &&
      !(getFromSession(session,"status").equals("DELIVERED")) )(
    doSwitch(session => getFromSession(session,"status"))(
      "CREATED" -> exec(UpdateOrder(baseUrl,HydraOrderUpdate.getRandomUpdateScenario())).exitHereIfFailed.exec(GetById()),
      "ACCEPTED" -> exec(UpdateOrder(baseUrl,HydraOrderUpdate.getRandomUpdateAfterAcceptScenario())).exitHereIfFailed.exec(GetById()),
      "BILLED" -> exec(UpdateOrder(baseUrl,HydraOrderUpdate.getRandomUpdateAfterBilledScenarios())).exitHereIfFailed.exec(GetById()),
      "ON_HOLD" -> exec(UpdateOrder(baseUrl,HydraOrderUpdate.getRandomUpdateAfterOnHoldScenarios())).exitHereIfFailed.exec(GetById()),
      "READY_FOR_DISPATCH" -> exec(UpdateOrder(baseUrl,HydraOrderUpdate.getDelivered))).exitHereIfFailed.exec(GetById())
    )
  }

  def Update(baseUrl: String = newConfigManager.getString("hydra.base_url"),updatePayload: String): ChainBuilder ={
    return exec(UpdateOrder(baseUrl,updatePayload)).exitHereIfFailed.exec(GetById())
  }

  def continueNew(baseUrl: String = newConfigManager.getString("hydra.base_url"))= {
    doWhile(session => !(getFromSession(session,"status").equals("CANCELLED")) &&
      !(getFromSession(session,"status").equals("DELIVERED")) )(
      doSwitch(session => getFromSession(session,"status"))(
        "CREATED" -> randomSwitch(
          70.0 -> Update(baseUrl,HydraOrderUpdate.getAccepted()),
          20.0 -> Update(baseUrl,HydraOrderUpdate.getRejected()),
          10.0 -> Update(baseUrl,HydraOrderUpdate.getCancelled()),
        ),
        "ACCEPTED" -> randomSwitch(
          70.0 -> Update(baseUrl,HydraOrderUpdate.getBilled()),
          20.0 -> Update(baseUrl,HydraOrderUpdate.getOnHold()),
          10.0 -> Update(baseUrl,HydraOrderUpdate.getCancelled()),
        ),
        "BILLED" -> randomSwitch(
          80.0 -> Update(baseUrl,HydraOrderUpdate.getRFD()),
          20.0 -> Update(baseUrl,HydraOrderUpdate.getCancelled()),
        ),
        "ON_HOLD" -> randomSwitch(
          80.0 -> Update(baseUrl,HydraOrderUpdate.getBilled()),
          20.0 -> Update(baseUrl,HydraOrderUpdate.getCancelled()),
        ),
        "READY_FOR_DISPATCH" -> Update(baseUrl,HydraOrderUpdate.getDelivered())
      )
    )
  }
}
