package actions.hydra

object HydraOrderUpdate {

  val accepted = HydraUpdate(
    "Accepted by retailer",
    "ACCEPTED"
  )

  val rejected= HydraUpdate(
    "Rejected by retailer",
    "REJECTED"
  )

  val cancelled= HydraUpdate(
    "Cancelled by cms",
    "Cancelled"
  )
}
