package simulations.domain

import lombok.Data

case class Scenarios(scenarios: Array[Scenario])

case class Scenario(name: String)

case class Action(name: String, parameters: Map[String, Any])

case class Assertions()