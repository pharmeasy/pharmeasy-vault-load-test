package newUtilities

import java.util
import java.util.{Arrays, UUID}

import com.auito.core.api.utils.{JWTAlgorithm, JavaWTGenerator}
import in.pharmeasy.utils.entry.JWTAuthHeaderPayload

object TokenGeneration {

  private var javaWTGenerator: JavaWTGenerator = JavaWTGenerator.getInstance
  private var HEADER: String = "{\"typ\":\"JWT\",\"alg\":\"HS512\"}"
  private var SECRET: String = "mercury@2018"
  private var THEA:String = "th008"

  def generateToken(payload: JWTAuthHeaderPayload): String = {
    val token =
      javaWTGenerator.encode(JWTAlgorithm.HMAC512, HEADER, payload, SECRET)
    token
  }

  def getDefaultToken(): String = {
    val token: String = javaWTGenerator.encode(JWTAlgorithm.HMAC512,
      HEADER,
      getMercuryDefaultAuthPayload,
      SECRET)
    token
  }



  def getMercuryDefaultAuthPayload(): JWTAuthHeaderPayload = {
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
    payload.setTenant(String.valueOf(THEA))
    payload
  }


  def getPickerDefaultAuthPayload: JWTAuthHeaderPayload = {
    val payload: JWTAuthHeaderPayload = new JWTAuthHeaderPayload
    payload.setApp("picker")
    payload.setAudience("mercury")
    payload.setUid(UUID.randomUUID.toString)
    payload.setIssuer("PharmEasy.in")
    payload.setName("Automation Picker User")
    payload.setStore("")
    payload.setScopes(util.Arrays.asList("wh-picker"))
    payload.setUser("Automation.User@gmail.com")
    payload.setTenant(THEA)
    payload
  }
}

