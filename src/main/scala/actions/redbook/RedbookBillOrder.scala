package actions.redbook

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.json4s.DefaultFormats
import org.json4s.native.Serialization.write

object RedbookBillOrder {

  implicit val formats = DefaultFormats

  val prescriptions = Seq("https://www.computerhope.com/jargon/a/array-of-pointers.jpg",
    "https://www.computerhope.com/jargon/s/special-characters.jpg")

  val invoiceUrls= Seq("https://www.computerhope.com/jargon/a/array-of-pointers.jpg",
    "https://www.computerhope.com/jargon/s/special-characters.jpg")

  val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
  val promised_deliver_date: String = LocalDateTime.now.plusDays(5).format(dateFormatter)

  def getBillPayload(meds:Array[Medicines],app_id:String,reference_order_id:String):String={

    var invoicedItemsAray: Seq[actions.redbook.InvoicedItems] = Seq[InvoicedItems]()

    for(x <- 0 until meds.length){
      val med = meds(x)
      val batches = Batches(
        med.mrp,
        med.name,
        med.mrp,
        med.mrp
      )

      var batchesArray = Seq[Batches]()
      batchesArray = batchesArray :+ batches

      val invoicedItems = InvoicedItems(
        batchesArray,
        med.name,
        med.qty,
        med.mdm_id,
        med.medicine_id,
        med.name
      )
      invoicedItemsAray = invoicedItemsAray :+ invoicedItems
    }

    val billPayload = BillPayload(
      "Notes",
      prescriptions,
      "BILLED",
      "PE_MARKETPLACE",
      meds,
      app_id,
      invoiceUrls,
      promised_deliver_date,
      reference_order_id,
      "Load Test User",
      "invoiceNumber123",
      invoicedItemsAray,
      "999",
      "packed-123"
    )

    return write(billPayload)
  }

}
