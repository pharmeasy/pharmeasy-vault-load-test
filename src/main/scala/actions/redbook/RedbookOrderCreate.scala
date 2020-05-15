package actions.redbook

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import newUtilities.newUtilities.{randomNumberBetweenRange, readCSV}
import org.json4s.DefaultFormats
import org.json4s.native.Serialization.write

object RedbookOrderCreate {

  private val random = scala.util.Random
  implicit val formats = DefaultFormats

  private val redbookMedsData: List[Array[String]] = readCSV("LoadTest.csv")
  val prescriptions = Seq("https://www.computerhope.com/jargon/a/array-of-pointers.jpg",
    "https://www.computerhope.com/jargon/s/special-characters.jpg")

  private def RedbookMedsPayload(max: Int = 10): List[Medicines] = {
    val shuffled = random.shuffle(redbookMedsData)
    val num = randomNumberBetweenRange(5, max)
    var medicinesList = List[Medicines]()
    for (a <- 0 to num - 1) {
      val data = shuffled(a);
      val items = Medicines(data(0), data(1), data(2),
        data(3), data(4), data(5), data(6),
        data(7), data(8))
      medicinesList = items :: medicinesList
    }
    return medicinesList
  }

  def RedbookItemData(): String = write(RedbookMedsPayload())

  val doctor: Doctor = Doctor(
    "Dr Load Test",
    "Bangalore",
    "9668876542"
  )

  val address = Address(
    "Block 1, 4th floor, PharmEasy",
    "Dairy circle, adugodi",
    "Banglore,Karnataka",
    "Load Test",
    "1234567890",
    "560100"
  )

  val meta = Meta(
    address,
    null,
    null,
    null
  )

  val patient = Patient(
    "LoadTest@gmail.com",
    "1234",
    "Load Redbook",
    "1234567890"
  )

  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
  val delivaryDate: String = LocalDateTime.now.plusDays(5).format(dateFormatter)

  val redbookCreatePayload = RedbookCreatePayload(
    doctor,
    "StringReplace",
    meta,
    "Load Test Load Test",
    patient,
    prescriptions,
    delivaryDate,
    "${orderId}",
    "PE_MARKETPLACE"
  )

  def getOrderPayload() = write(redbookCreatePayload).replace("\"StringReplace\"", "${items}")
}
