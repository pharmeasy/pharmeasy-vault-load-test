package simulations

import java.util.Objects._

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef.http
import org.reflections.Reflections
import simulations.templates.BaseTemplate
import utils.ConfigManager._

import scala.collection.JavaConverters._

case class Scenarios(scenarios: Array[Scenario])

case class Scenario(name: String, actions: Array[Action])

case class Action(name: String, parameters: Any)

class E2EScenarioBuilder extends io.gatling.core.Predef.Simulation {

  private val rootPackage = "simulations"
  private val reflections = new Reflections(rootPackage)
  private val templates = reflections.getSubTypesOf(classOf[BaseTemplate]).asScala
  private val scenarioBuilder = classOf[ScenarioBuilder]
  private val filterScenarios = getArray("filter.scenarios", "").filter(!_.isEmpty).map(_.toLowerCase) toList
  private val scenarios = templates.flatMap(template => {
    template.getDeclaredFields.filter(_.getType.eq(scenarioBuilder)) map (field => {
      val isAccessible = field.isAccessible
      if (!isAccessible) {
        field.setAccessible(true)
      }
      val injectedTemplate = template.newInstance;
      if (injectedTemplate.enabled && filterScenario(injectedTemplate.name)) {
        val value = field.get(injectedTemplate).asInstanceOf[ScenarioBuilder]
        field.setAccessible(isAccessible)
        value.inject(injectedTemplate.step)
      } else {
        null
      }
    }) filter nonNull
  }) toList

  private def filterScenario(name: String) = {
    if (filterScenarios.isEmpty) {
      true
    } else {
      val _name = name.trim.toLowerCase
      !filterScenarios.filter { e => e.contains(_name) }.isEmpty
    }
  }

  private val baseUrl = http.disableWarmUp.disableCaching

  println("executing scenarios => " + scenarios)

  setUp(scenarios).protocols(baseUrl)

}
