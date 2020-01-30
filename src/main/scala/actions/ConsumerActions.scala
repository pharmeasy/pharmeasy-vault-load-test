package actions

import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._
import utils.ConfigManager._
import utils.Utilities._

import scala.collection.Seq
import scala.collection.mutable.ListBuffer

object ConsumerActions extends BaseActions {

  def addToSession(session: Session, attributes: (String, Any)*) = session.setAll(attributes)

  def randomProductId(session: Session): Session = session.set("productId", randomFromSeq(getSeqFromSession(session, "productIds")))

  def pincodeDetails(baseUrl: String = getString("diagnostics.base_url"), pincode: String = getString("consumer.pincode")) =
    http("get pincode details")
      .get(session => baseUrl + s"/v3/pincodes/details/${getFromSession(session, "pincode", pincode)}")
      .asJson
      .check(status.is(200))

  def recommendations(baseUrl: String = getString("diagnostics.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), auth: String = getString("consumer.api_auth"), platform: String = getString("consumer.platform"), accessToken: String = getString("consumer.access_token")) =
    http("fetch recommendations")
      .get(session => baseUrl + "/api/search/v2/home/recommendations")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Api-Auth", session => getFromSession(session, "auth", auth))
      .header("X-Phone-Platform", session => getFromSession(session, "platform", platform))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"))

  def home(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token")) =
    http("home")
      .get(session => baseUrl + "/v3/home")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"))

  def omniSearch(baseUrl: String = getString("consumer.base_url"), query: String = getString("consumer.medicine_to_search"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token")) =
    http("omni search")
      .get(session => baseUrl + s"/v3/ecommerce/omni-search/search-with-fulfilment?q=${getFromSession(session, "query", query)}")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"))

  def omniSearchCharByChar(baseUrl: String = getString("consumer.base_url"), query: String, pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token")) = {
    val buffer = ListBuffer[ChainBuilder]()
    for (i <- query.indices) {
      buffer += exec(omniSearch(query = query.substring(0, i)))
    }
    exec(buffer toArray)
  }

  def viewAll(baseUrl: String = getString("consumer.base_url"), query: String = getString("consumer.medicine_to_search"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), pageNumber: String = "1", accessToken: String = getString("consumer.access_token")) =
    http("view all results")
      .get(session => baseUrl + s"/v3/ecommerce/omni-search/view-all-with-fulfilment?q=${getFromSession(session, "query", query)}&page=${getFromSession(session, "pageNumber", pageNumber)}&viewType=list")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"))

  def viewPages(baseUrl: String = getString("consumer.base_url"), query: String = getString("consumer.medicine_to_search"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), pageNumber: String = "1", accessToken: String = getString("consumer.access_token")): ChainBuilder = {
    doWhile(session => getFromSession(session, "pageNumber", pageNumber).toInt <= getFromSession(session, "pageNumberMax", getFromSession(session, "pageNumber", pageNumber)).toInt) {
      exec(viewAll().check(jsonPath("$.data.products[*].productId").findAll.saveAs("productIds")))
        .exec(session => session.set("pageNumber", (session("pageNumber").as[String].toInt + 1).toString))
    }
  }

  def productInformation(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token"), productId: String = null) =
    http("product information")
      .get(session => baseUrl + s"/v3/ecommerce/products/${getFromSession(session, "productId", productId)}")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"),
        jsonPath("$.data.ucode").find.optional.saveAs("ucode"),
        jsonPath("$.data.isRefrigerated").find.optional.saveAs("isRefrigerated"),
        jsonPath("$.data.isRxRequired").find.optional.saveAs("isRxRequired"),
        jsonPath("$.data.listPriceDecimal").find.optional.saveAs("listPrice"),
        jsonPath("$.data.salePriceDecimal").find.optional.saveAs("salePrice"),
        jsonPath("$.data.discountDecimal").find.optional.saveAs("discount"),
        jsonPath("$.data.discountPercent").find.optional.saveAs("discountPercent"),
        jsonPath("$.data.mrp").find.optional.saveAs("mrp"),
        jsonPath("$.data.isAvailable").find.optional.saveAs("isAvailable"),
        jsonPath("$.data.maxQuantity").find.optional.saveAs("maxQuantity"),
        jsonPath("$.data.availableQuantity").find.optional.saveAs("availableQuantity"),
        jsonPath("$.data.itemId").find.optional.saveAs("itemId"))

  def productDescription(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token"), productId: String = null) =
    http("product description")
      .get(session => baseUrl + s"/v3/ecommerce/products/${getFromSession(session, "productId", productId)}/description")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"))

  def addToCart(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), productId: String = null, quantity: String = "1", accessToken: String = getString("consumer.access_token")) =
    http("add to cart")
      .post(session => baseUrl + "/v3/ecommerce/cart/items/render")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .body(StringBody(session => s"""{"hasLocalRxAdded":0,"minimalVersion":0,"optForDoctorConsultation":false,"products":[{"quantity":${getFromSession(session, "quantity", quantity)},"productId":"${getFromSession(session, "productId", productId)}"}],"promoCode":""}"""))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"))

  def cartItems(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token")) =
    http("cart items")
      .get(session => baseUrl + "/v3/ecommerce/cart/items")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"))

  def recommendedOTCProducts(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token"), productId: String = "") =
    http("recommended otc products")
      .get(session => baseUrl + s"/v3/cards/recommended-otc-products?productIds[]=${getFromSession(session, "productId", productId)}")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"))

  def offers(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token")) =
    http("offers and promotions")
      .get(session => baseUrl + "/v3/promotions/offers")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"))

  def savingsOnCart(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token"), productId: String = "", quantity: String = "1") =
    http("savings on cart items")
      .post(baseUrl + "/v3/ecommerce/cart/items/savings")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .body(StringBody(session => s"""{ "isLoginPage": true, "products": [ { "productId": "${getFromSession(session, "productId", productId)}", "quantity": ${getFromSession(session, "quantity", quantity)} } ] }"""))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"))

  def sendOTP(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token"), contactNumber: String = getString("consumer.contact_number")) =
    http("send otp")
      .post(baseUrl + "/v3/users/send-otp")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .body(StringBody(session => s"""{ "contactNumber": "${getFromSession(session, "contactNumber", contactNumber)}" }"""))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"))

  def login(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), contactNumber: String = getString("consumer.contact_number"), otp: String = getString("consumer.otp")) =
    http("login")
      .post(baseUrl + "/v3/users/login")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("Content-Type", "application/x-www-form-urlencoded")
      .header("Accept", "application/json")
      .body(StringBody(session => s"contactNumber=${getFromSession(session, "contactNumber", contactNumber)}&userOtp=${getFromSession(session, "otp", otp)}&hasCalled=false"))
      .check(
        status.is(200),
        jsonPath("$.status").is("1"),
        jsonPath("$.data.profile.id").find.saveAs("customerId"),
        jsonPath("$.data.accessToken").find.saveAs("accessToken"))

  def logout(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city")) =
    http("logout")
      .post(baseUrl + "/v3/customers/logout")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"))

  def cartSyncAfterLogin(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token"), productId: String = "", quantity: String = "1") =
    http("cart sync after login")
      .post(baseUrl + "/v3/ecommerce/cart/items/sync")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .body(StringBody(session => s"""{ "hasLocalRxAdded": 0, "minimalVersion": 0, "optForDoctorConsultation": false, "products": [ { "productId": "${getFromSession(session, "productId", productId)}", "quantity": ${getFromSession(session, "quantity", quantity)} } ], "promoCode": "" }"""))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"),
        jsonPath("$.data.syncStatus").is("true"))

  def cartItemsCountAfterLogin(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token")) =
    http("get count of items in cart")
      .get(baseUrl + "/v3/ecommerce/cart/items/count")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"))

  def fetchAddress(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token")) =
    http("fetch addresses")
      .get(baseUrl + "/v3/addresses")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"))

  def addAddress(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token")) =
    http("add address")
      .post(baseUrl + "/v3/addresses")
      .header("Content-Type", "application/x-www-form-urlencoded")
      .header("Accept", "application/json")
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .body(StringBody(session => s"pincode=${pincode}&streetName=Test%20Street%20Number&contactName=Test%20User&flatNumber=Test%20House%20Number&contactNumber=${getFromSession(session, "contactNumber")}&cityId=${getFromSession(session, "city", city)}&type=0"))
      .check(
        status.is(200),
        jsonPath("$.status").is("1"),
        jsonPath(session => "$.data[?(@.pincode == '" + getFromSession(session, "pincode", pincode) + "' || @.cityId == " + getFromSession(session, "city", city) + ")].id").saveAs("addressId"))

  def fetchAddressOrAddIfNotExist(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token")) = {
    exec(fetchAddress())
      .doIf(session => session("addressId").asOption[String].isEmpty) {
        exec(addAddress())
      }
  }

  def estimateDelivery(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token")) =
    http("estimate delivery")
      .get(baseUrl + s"/v3/pincodes/$pincode/estimate-delivery-date?hasLocalRxAdded=0&optForDoctorConsultation=false")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"))

  def deliveryPreference(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token")) =
    http("delivery preference")
      .get(session => baseUrl + "/v3/orders/delivery-preference?addressId=" + session("addressId").as[String] + "&optForDoctorConsultation=false")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .asJson
      .check(status.is(200))

  def paymentInstrumentationBeforeOrderPlacement(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token")) =
    http("payment instrumentation before order placement")
      .get(session => baseUrl + "/v3/ecommerce/payment-instrument?addressId=" + session("addressId").as[String] + "&hasLocalRxAdded=false")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"))

  def paymentInstrumentationAfterOrderPlacement(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token"), orderId: String = null) =
    http("payment instrumentation after order placement")
      .get(session => baseUrl + s"/v3/ecommerce/payment-instrument?hasLocalRxAdded=false&orderId=${getFromSession(session, "orderId", orderId)}&orderType=0")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"))

  def amountBifurcation(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token"), addressId: String = null) =
    http("amount bifurcation")
      .get(session => baseUrl + s"/v3/ecommerce/cart/items/view-amount-bifurcation?addressId=${
        getFromSession(session, "addressId", addressId)
      }")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"),
        jsonPath("$.data.amountBifurcation[?(@.text == 'Amount to be paid')].value").saveAs("amountToBePaid"))

  def codPayment(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token"), orderId: String = null) =
    http("cod payment")
      .put(session => baseUrl + "/v3/payment-modes/order/" + session("orderId").asOption[String].getOrElse(orderId))
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .body(StringBody(session => """{ "paymentId": -1, "payableAmount": """ + session("amountToBePaid").asOption[Double].getOrElse(0D) + """ }"""))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"))

  def onlinePaymentStatusVerification(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token"), callerOrderId: String = null) =
    http("online payment status verification")
      .get(session => baseUrl + s"/v3/payments/status?pgOrderId=${
        getFromSession(session, "callerOrderId", callerOrderId)
      }")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"))

  def orderPriceFluctuation(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token"), orderId: String = null) =
    http("order price fluctuation")
      .get(session => baseUrl + "/v3/orders/price-journey?orderId=" + session("orderId").asOption[String].getOrElse(orderId))
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"))

  def paymentInvocationDetails(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token"), orderId: String = null, paymentId: String = getString("wallet.payment_id"), paymentSubId: String = getString("wallet.payment_sub_id"), sdkDetails: Seq[String] = getSeq("wallet.sdk_details")) =
    http("payment invocation details after order placement")
      .post(baseUrl + "/v3/payments/fetch-payment-invoke-details")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .body(StringBody(session =>
        s""" { "orderId":  "${
          getFromSession(session, "orderId", orderId)
        }", "paymentId": ${
          getFromSession(session, "invokePaymentId", paymentId)
        }, "paymentSubId": ${
          getFromSession(session, "invokePaymentSubId", paymentSubId)
        }, "sdkDetails": [ ${
          getSeqFromSession(session, "sdkDetails", sdkDetails) map {
            e => "\"" + e + "\""
          } mkString (",")
        } ] }"""))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"),
        jsonPath("$.data.caller_order_id").saveAs("callerOrderId"))

  def uploadRx(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token")) = {
    http("upload rx")
      .put(session => session("imageUploadUrl").as[String])
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .asJson
      .check(
        status.is(200))
  }

  def getUploadRxUrl(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token")) = {
    http("get upload rx url")
      .get("/api/prescriptions/generateUploadUrl?count=1&pdfCount=0")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.[0].key").saveAs("uploadedImage"),
        jsonPath("$.[0].url").saveAs("imageUploadUrl"))
  }

  def placeOrder(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token"), addressId: String = null, paymentId: String = "-1", paymentSubId: String = "-1") =
    http("place order")
      .post(session => baseUrl + (if (session("isRxRequired").asOption[Boolean].getOrElse(false)) "/v3/ecommerce/orders" else "/api/order/placeOrder"))
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .body(StringBody(session => {
        if (session("isRxRequired").asOption[Boolean].getOrElse(false))
          s"""{"addressId":${getFromSession(session, "addressId", addressId)},"imagesNames":["${getFromSession(session, "uploadedImage")}"],"requiresPharmacistCall":0,"pdfFiles":[],"pastImages":[],"orderType":4,"noPrescriptionOrder":0,"optForDoctorConsultation":0,"patients":[{"orderInfoType":"1","orderInfoNotes":""}],"notes":"","convertToSubscription":0,"slotDate":0,"intervalValue": ${randomInterval},"walletOptIn":1,"paymentId": ${getFromSession(session, "paymentId", paymentId)},"isUfpEligible":true}"""
        else
          s"""{ "requiresPharmacistCall": 0, "isFromNewOrderFlow": true, "isUfpEligible": 1, "paymentId": ${getFromSession(session, "paymentId", paymentId)}, "paymentSubId": ${getFromSession(session, "paymentSubId", paymentSubId)}, "isUserEligibleForUfp": "true", "walletOptIn": "true", "noPrescriptionOrder": true, "intervalValue": ${
            randomInterval
          }, "addressId": "${
            getFromSession(session, "addressId", addressId)
          }", "status": 1 }"""
      }))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"),
        jsonPath("$.data.orders.medicineOrder.id").saveAs("orderId"))

  def fetchOrders(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token")) =
    http("fetch orders")
      .get(session => baseUrl + "/v4/orders")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"))

  def orderDetails(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token"), orderId: String = "") =
    http("order details")
      .get(session => baseUrl + s"/v3/orders/${getFromSession(session, "orderId", orderId)}/view")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"),
        jsonPath("$.data.currentStateDetails").saveAs("orderStatus"))

  def orderItems(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token"), orderId: String = "") =
    http("order items")
      .get(session => baseUrl + s"/v3/orders/${getFromSession(session, "orderId", orderId)}/items")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"))

  def cancelOrder(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token"), orderId: String = "") =
    http("cancel order")
      .post(session => baseUrl + s"/v3/orders/cancel/${getFromSession(session, "orderId", orderId)}")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .header("Content-Type", "application/x-www-form-urlencoded")
      .header("Accept", "application/json")
      .body(StringBody(session => s"""cancelReasonId=69&cancelReasonList=[69]&status=${getFromSession(session, "orderStatus", "8")}"""))
      .check(
        status.is(200),
        jsonPath("$.status").is("1"))

  def trackOrder(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token")) =
    http("track order")
      .get(session => baseUrl + "/v3/ecommerce/orders/order-track")
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"))

  def customerDetails(baseUrl: String = getString("consumer.base_url"), pincode: String = getString("consumer.pincode"), city: String = getString("consumer.city"), accessToken: String = getString("consumer.access_token"), customerId: String = "") =
    http("customer details")
      .get(session => baseUrl + "/v3/customers/" + getFromSession(session, "customerId", customerId))
      .header("X-Pincode", session => getFromSession(session, "pincode", pincode))
      .header("X-Default-City", session => getFromSession(session, "city", city))
      .header("X-Access-Token", session => getFromSession(session, "accessToken", accessToken))
      .asJson
      .check(
        status.is(200),
        jsonPath("$.status").is("1"))
}
