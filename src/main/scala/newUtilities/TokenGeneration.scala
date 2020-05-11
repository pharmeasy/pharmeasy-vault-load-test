package newUtilities

import java.util.{Arrays, UUID}

import com.auito.core.api.utils.{JWTAlgorithm, JavaWTGenerator}
import in.pharmeasy.utils.entry.JWTAuthHeaderPayload

object TokenGeneration {

  private val javaWTGenerator: JavaWTGenerator = JavaWTGenerator.getInstance
  private val HEADER: String = "{\"typ\":\"JWT\",\"alg\":\"HS512\"}"
  private val SECRET: String = "mercury@2018"
  private val DEFAULT_THEA: String = "th032"

  def generateToken(payload: JWTAuthHeaderPayload) = javaWTGenerator.encode(JWTAlgorithm.HMAC512, HEADER, payload, SECRET)

  def getDefaultToken(thea: String = DEFAULT_THEA) = javaWTGenerator.encode(JWTAlgorithm.HMAC512, HEADER, getMercuryDefaultAuthPayload(thea), SECRET)

  def getMercuryDefaultAuthPayload(thea: String = DEFAULT_THEA): JWTAuthHeaderPayload = {
    val payload: JWTAuthHeaderPayload = new JWTAuthHeaderPayload()
    payload.setApp("nebula")
    payload.setAudience("mercury")
    payload.setUid(UUID.randomUUID().toString)
    payload.setIssuer("PharmEasy.in")
    payload.setName("Automation Nebula User")
    payload.setStore("")
    payload.setScopes(
      Arrays.asList("wh-inventory-rectifier", "wh-super-admin"))
    payload.setUser("Automation.User@gmail.com")
    payload.setTenant(String.valueOf(thea))
    payload
  }


def getPickerDefaultAuthPayload(thea: String = DEFAULT_THEA): JWTAuthHeaderPayload = {
    val payload: JWTAuthHeaderPayload = new JWTAuthHeaderPayload
    payload.setApp("picker")
    payload.setAudience("mercury")
    payload.setUid(UUID.randomUUID.toString)
    payload.setIssuer("PharmEasy.in")
    payload.setName("Automation Picker User")
    payload.setStore("")
    payload.setScopes(Arrays.asList("wh-picker"))
    payload.setUser("Automation.User@gmail.com")
    payload.setTenant(String.valueOf(thea))
    payload
  }
}

