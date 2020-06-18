package actions.scm

import in.pharmeasy.utils.entry.JWTAuthHeaderPayload
import io.gatling.core.session.Session
import newUtilities.newUtilities.{randomNumberBetweenRange, _}
import org.json4s.native.Serialization.write
import newUtilities.TokenGeneration
import utils.ConfigManager.getString


object CreateGenerateBillPayload {
  protected def getFromSession(session: Session, key: String, defaultValue: String = null): String = session(key).asOption[String].getOrElse(defaultValue)

  def getPharmacist(thea: String = getString("outward.thea")) = {
    val payload: JWTAuthHeaderPayload = TokenGeneration.getPharmacistDefaultAuthPayload(thea);
    val pharmacist: String = payload.getUser
    pharmacist
  }

  val generateInvoice = GenerateBillPayload("StringReplace",
    getPharmacist()
  )

  val orderStatus = GenerateBillPayload("\"StringReplace\"",
    null)


  def getInvoicePayload(status : String): String = {
    return write(generateInvoice).replace("StringReplace", status)
  }

  def setOrderStatus(status : String): String = {
    return write(generateInvoice).replace("StringReplace", status)
  }
}



