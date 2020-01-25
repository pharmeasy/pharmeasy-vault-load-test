package simulations.templates.consumer

import actions.ConsumerActions._
import io.gatling.core.Predef._
import io.gatling.core.controller.inject.open.OpenInjectionStep
import simulations.templates.BaseTemplate

import scala.concurrent.duration._

class TemplateLandingOnHomePage extends BaseTemplate {

  override def name() = "home page"

  val scn = scenario(name)
    .exec(recommendations())
    .exec(home())

  override def step(): OpenInjectionStep = constantUsersPerSec(1) during (5 seconds) randomized;

  override def enabled(): Boolean = false

}
