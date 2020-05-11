package actions.scm


import org.json4s.DefaultFormats
import org.json4s.native.Serialization.write
import newUtilities.newUtilities._

import scala.collection.mutable.ArrayBuffer

object OrderPayloadCreation {

  private val random = scala.util.Random
  implicit val formats = DefaultFormats

  private val b2cMedicinesData: List[Array[String]] = readCSV("b2c_meds.csv")

  private def getB2CPayload(max: Int = 2): List[Item] = {
    val shuffled = random.shuffle(b2cMedicinesData)
    val num = randomNumberBetweenRange(1, max)
    var items = List[Item]()
    for (a <- 0 to num - 1) {
      val data = shuffled(a);
      val item = Item(77, data(0), data(1), data(2))
      items = item :: items
    }
    return items
  }

  def getJsonString(): String = write(getB2CPayload())

  val address = Address("89 6th cross",
    "RPS Road",
    "Indiranagar",
    "kiran",
    "Bangalore",
    "8989898989",
    "8989898989")

  val orderPayload = OrderPayload(
    null,
    "${externalOrder}",
    "1000577",
    "2019-07-09T06:19:40",
    "2019-07-09T11:23:24",
    null,
    "Pankaj",
    "Test RP",
    "cankit",
    "1234567890",
    "qwerty@gmail.com",
    "Kiran",
    "Deep",
    "2800af35-0b7f-4324-9cf8-76143baceb72",
    "SCM",
    2,
    null,
    null,
    address,
    "StringReplace",
    List(),
    10,
    10,
    0,
    "sr",
    "cr",
    false,
    null,
    0,
    0,
    157,
    174,
    false,
    true,
    null,
    null,
    "08030752800",
    "care@pharmeasy.in",
    "Kiran",
    null,
    null,
    null,
    true
  )

  def getOrderPayload(): String = {
    return write(orderPayload).replace("\"StringReplace\"", "${items}")
  }

  val multiPickingPayload = MultiPickingConfigPayload(
    1,
    1,
    5,
    "MAX_IDLE_TIME",
    "NOT_APPLICABLE"
  )

  def getMultiPickingConfigPayload(): String = {
    return write(multiPickingPayload)
  }

  val prioritisePickerTaskPayload = PrioritisePickerTaskPayload(
    "StringReplace"
  )

  def getPrioritisePickerTaskConfig(refId: String): String = {
    return write(prioritisePickerTaskPayload).replace("StringReplace", refId)
  }

  val generateBillPayload = GenerateBillPayload(
    "StringReplace",
    null
  )

  def getGenerateBillPayload(refId: String): String = {
    return write(generateBillPayload).replace("StringReplace", refId)
  }

  val signInAppPayload = SignInPickerAppPayload(
    "picker",
    "mercury",
    "c0Qw92O1-ig:APA91bHTXuRTFr4LXs6Jn9cfiR367P7GhiwzEeDitnLm5co0XrpIOVA4LHGKMIbiCADycVrZTLWzkFrjMdoAc12JSmPafL0Q_Fed-8sGGLnx_fspLzG9XGBiXct5FtvZFWSsv-GoWCtM",
    "12345",
    "abhi01"
  )

  def getSignInAppPayload(): String = {
    return write(signInAppPayload)
  }

  val pickTrayPayload = PickTrayPayload("StringReplace")

  def getPickTrayPayload(trayId: String): String = {
    return write(pickTrayPayload).replace("StringReplace", trayId)
  }

  val taskPickedPayload = AggregateTaskPickedPayload("PICKED")

  def getTaskPickedPayload(): String = {
    return write(taskPickedPayload)
  }

  val barcodePayload = BarcodePayload(
    "barcode"
  )

  def getBarCodePayload(barcode: String): BarcodePayload = {
    return write(barcodePayload).replace("barcode", barcode).asInstanceOf[BarcodePayload]
  }

  def getPickedItemsPayload(bin: String, ucode: String, barcodes: List[String]): String = {
    val pickedItems = ArrayBuffer[BarcodePayload]()
    for (barcode <- barcodes) {
      pickedItems += (getBarCodePayload(barcode))
    }
    val pickedItemsPayload = PickedItemsPayload(bin, 1090909, List(), "GLYCIPHAGE 850MG TAB", 3, 10, pickedItems, "IN_TRAY", ucode)
    return write(pickedItemsPayload)
  }


  def getPickerTaskZonePayload(aggregatePickerTaskId: String): PickerTaskZones = {
    val pickerTaskZone = PickerTaskZones(aggregatePickerTaskId.toLong, null, "COMPLETED", null)
    return write(pickerTaskZone).asInstanceOf[PickerTaskZones]
  }

  def getScanZonePayload(aggregatePickerTaskId: String): String = {
    val scanZones = ArrayBuffer[PickerTaskZones]()
    scanZones += (getPickerTaskZonePayload(aggregatePickerTaskId))
    return write(ScanZonePayload(scanZones))
  }

  def getCompletePickedItemsPayload(bin: String, ucode: String, aggregatePickerTaskId: String): String = {
    val completePickedItemsPayload = CompletePickedItemsPayload(
      bin,
      aggregatePickerTaskId.toLong,
      "",
      "IN_PROGRESS",
      ucode
    )
    return write(completePickedItemsPayload)
  }


}
