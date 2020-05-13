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
    "Cancelled"
  )

  val updateScenarios = Array( write(accepted),write(rejected),write(cancelled))

  def getRandomUpdateScenario() =
    updateScenarios(randomNumberBetweenRange(0, updateScenarios.length - 1))
}
