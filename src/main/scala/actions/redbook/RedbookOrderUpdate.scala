package actions.redbook

import java.sql.Timestamp

import org.json4s.DefaultFormats
import org.json4s.native.Serialization.write

object RedbookOrderUpdate {

  val timestamp = new Timestamp(System.currentTimeMillis)
  implicit val formats = DefaultFormats

  val accepted = UpdatePayload(
    "Accepted by retailer",
    "ACCEPTED",
    timestamp
  )

  val rejected = UpdatePayload(
    "Rejected by retailer",
    "REJECTED",
    timestamp
  )

  val cancelled = UpdatePayload(
    "Cancelled by hydra",
    "CANCELLED",
    timestamp
  )

  val delivered = UpdatePayload(
    "Delivered by hydra",
    "DELIVERED",
    timestamp
  )

  val RFD = UpdatePayload(
    "RFD by hydr",
    "READY_FOR_DISPATCH",
    timestamp
  )

  val onHold = UpdatePayload(
    "onHold due to validation",
    "ON_HOLD",
    timestamp
  )

  val billed = UpdatePayload(
    "billed due to validation",
    "BILLED",
    timestamp
  )

  def getAccepted() = write(accepted)
  def getRejected() = write(rejected)
  def getCancelled() = write(cancelled)
  def getDelivered() = write(delivered)
  def getRFD() = write(RFD)
  def getOnHold() = write(onHold)
  def getBilled() = write(billed)

}
