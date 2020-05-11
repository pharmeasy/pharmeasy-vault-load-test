package actions.scm

import scala.collection.mutable.ArrayBuffer

case class PickedItemsPayload(
                               binId: String,
                               id: Long,
                               issueItems: List[String],
                               name: String,
                               orderedQuantity: Integer,
                               packQuantity: Integer,
                               pickedItems: ArrayBuffer[BarcodePayload],
                               status: String,
                               ucode: String
                             )

