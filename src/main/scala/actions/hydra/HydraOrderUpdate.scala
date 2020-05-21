package actions.hydra

import actions.hydra.HydraOrderCreation.order
import org.json4s.DefaultFormats
import org.json4s.native.Serialization.write
import utils.Utilities.randomNumberBetweenRange

object HydraOrderUpdate {
  implicit val formats = DefaultFormats

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
    "CANCELLED"
  )

  val onHold= HydraUpdate(
    "On Hold by retailer",
    "ON_HOLD"
  )

  val billed= HydraUpdate(
    "billed by retailer",
    "BILLED"
  )

  val rfd= HydraUpdate(
    "RFD by retailer",
    "READY_FOR_DISPATCH"
  )

  val delivered= HydraUpdate(
    "Delivered by cms",
    "DELIVERED"
  )

  val updateScenarios = Array( write(accepted),write(rejected),write(cancelled))

  def getRandomUpdateScenario() =
    updateScenarios(randomNumberBetweenRange(0, updateScenarios.length - 1))

  val updateAfterAcceptScenarios = Array( write(billed),write(cancelled),write(onHold))

  def getRandomUpdateAfterAcceptScenario() =
    updateAfterAcceptScenarios(randomNumberBetweenRange(0, updateAfterAcceptScenarios.length - 1))

  val updateAfterOnHoldScenarios = Array( write(billed),write(cancelled))

  def getRandomUpdateAfterOnHoldScenarios() =
    updateAfterAcceptScenarios(randomNumberBetweenRange(0, updateAfterOnHoldScenarios.length - 1))

  val updateAfterBilledScenarios = Array( write(rfd),write(cancelled))

  def getRandomUpdateAfterBilledScenarios() =
    updateAfterAcceptScenarios(randomNumberBetweenRange(0, updateAfterBilledScenarios.length - 1))

  def getDelivered() = write(delivered)
  def getAccepted() = write(accepted)
  def getBilled() = write(billed)
  def getOnHold() = write(onHold)
  def getRFD() = write(rfd)
  def getRejected() = write(rejected)
  def getCancelled() = write(cancelled)
}
