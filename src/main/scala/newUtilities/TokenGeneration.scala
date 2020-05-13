package newUtilities

import java.util.{Arrays, UUID}

import com.auito.core.api.utils.{JWTAlgorithm, JavaWTGenerator}
import in.pharmeasy.utils.entry.JWTAuthHeaderPayload

object TokenGeneration {


  private var javaWTGenerator: JavaWTGenerator = JavaWTGenerator.getInstance
  private var HEADER: String = "{\"typ\":\"JWT\",\"alg\":\"HS512\"}"
  private var SECRET: String = "mercury@2018"
  private var DEFAULT_THEA: String = "th124"

  //  def generateToken(payload: JWTAuthHeaderPayload): String = {
  //    val token =
  //      javaWTGenerator.encode(JWTAlgorithm.HMAC512, HEADER, payload, SECRET)
  //    token
  //  }

  def getDefaultToken(): String = {
    val token: String = javaWTGenerator.encode(JWTAlgorithm.HMAC512,
      HEADER,
      getMercuryDefaultAuthPayload(DEFAULT_THEA),
      SECRET)
    token
  }

  private var pickJWT: JWTAuthHeaderPayload = getPickerDefaultAuthPayload(DEFAULT_THEA)

  def getPickerId(): String = {
    val pickerId: String = pickJWT.getUid()
    pickerId
  }

  def getPickerToken(): String = {
    val token: String = javaWTGenerator.encode(JWTAlgorithm.HMAC512,
      HEADER,
      pickJWT,
      SECRET)
    token
  }

  def getBillerToken(): String = {
    val token: String = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJhcHAiOiJuZWJ1bGEiLCJhdWQiOiJtZXJjdXJ5IiwidWlkIjoiMjRhYjIxY2EtMWYwMC00NzllLTgxOTUtNDJjZjI1ZmUwZWU5IiwiaXNzIjoiUGhhcm1FYXN5LmluIiwibmFtZSI6IkFrYW5zaGEiLCJzdG9yZSI6IjI4MDBhZjM1LTBiN2YtNDMyNC05Y2Y4LTc2MTQzYmFjZWI3MiIsInNjb3BlcyI6WyJwYXJ0bmVyLWNoZWNrZXIiLCJwYXJ0bmVyLW1ha2VyIiwicGFydG5lci1zdXBlcnVzZXIiLCJwcm9jdXJlbWVudF9kZW1hbmRfcGxhbm5lciIsInByb2N1cmVtZW50X29yZGVyaW5nX2Fzc29jaWF0ZSIsInByb2N1cmVtZW50X29yZGVyaW5nX21hbmFnZXIiLCJwcm9jdXJlbWVudF9wdXJjaGFzZV9yYXRlX3BsYW5uZXIiLCJzdG9yZS1iaWxsaW5nLXVzZXIiLCJzdG9yZS1waGFybWFjaXN0Iiwid2gtYmlsbGluZy11c2VyIiwid2gtZ2F0ZS1wYXNzLXVzZXIiLCJ3aC1pbnZlbnRvcnktcmVjdGlmaWVyIiwid2gtcHJvY2Vzcy1vd25lci1wcm9jdXJlbWVudCIsIndoLXByb2N1cmVtZW50LWFkbWluIiwid2gtcHJvZHVjdC1tYW5hZ2VyLXByb2N1cmVtZW50Iiwid2gtc2lnbmF0b3J5Iiwid2gtc3VwZXItYWRtaW4iLCJ3aC12ZXJpZmllciJdLCJleHAiOjE1OTA1NzM3ODEsInVzZXIiOiJha2Fuc2hhLmdhb25rYXJAcGhhcm1lYXN5LmluIiwidGVuYW50IjoidGgxMjQifQ.13SJrAYgbfe75sDiDvNaxnKSMRL5JkDtc2Ye-q7_kMe2I5C9F9XjXF33Q5w_5IQAphj33kcd8R7VhLYPhae8sw"
    token
  }

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

