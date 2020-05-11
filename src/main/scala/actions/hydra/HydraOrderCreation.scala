package actions.hydra

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, LocalDateTime}

import actions.scm.{Address, Item, OrderPayload}
import newUtilities.newUtilities.{randomNumberBetweenRange, readCSV}
import org.json4s.DefaultFormats
import org.json4s.native.Serialization.write

object HydraOrderCreation {

  private val random = scala.util.Random
  implicit val formats = DefaultFormats

  private val hydraMedsData: List[Array[String]] = readCSV("LoadTest.csv")

  private def HydraItemsPayload(max: Int = 3): List[Items] = {
    val shuffled = random.shuffle(hydraMedsData)
    val num = randomNumberBetweenRange(1, max)
    var ListOfItems = List[Items]()
    for( a <- 0 to num-1){
      val data = shuffled(a);
      val items  = Items(data(0),data(1),data(2),data(3),data(4),data(5),data(6),data(7),data(8))
      ListOfItems = items :: ListOfItems
    }
    return ListOfItems
  }

  val prescriptions = Seq("https://www.computerhope.com/jargon/a/array-of-pointers.jpg", "https://www.computerhope.com/jargon/s/special-characters.jpg")

  def ItemsData():String = write(HydraItemsPayload())

  val address = Address("89 6th cross",
    "RPS Road",
    "Indiranagar",
    "kiran",
    "Bangalore",
    "8989898989",
    "8989898989")

  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
  val delivaryDate: String = LocalDateTime.now.plusDays(5).format(dateFormatter)

  val order =  CreateOrder(
    address,
    "LoadTest@gmail.com",
    "9999877778",
    "LoadTest",
    "Testing",
    "Dr Test",
    "StringReplace",
    "${orderId}",
    "Load Test Patient",
    prescriptions,
    delivaryDate,
    "${retailerId}",
    "source-work-flow-id-LoadTest"
  )
  def getOrderPayload():String = {
    return write(order).replace("\"StringReplace\"","${items}")
  }
}
