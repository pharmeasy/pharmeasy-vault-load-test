package utils

import scala.collection.JavaConverters.asScalaSetConverter
import scala.collection.mutable.Queue

import org.reflections.Reflections

import actions.BaseActions
import io.gatling.http.request.builder.HttpRequestBuilder
import utils.Utilities.readFile
import utils.Utilities.deserializeJson

case class Node(actions: Array[Action]) {
  override def toString: String = actions.deep.toString
}

case class Action(action: String, probability: Double) {
  override def toString(): String = s"Action(name: ${action}, probability: ${probability})"
}

object DynamicScenarioBuilder {

  private val rootNodeName = "start"

  def buildTree(data: Map[String, List[Action]], count: Long): NaryTree = {

    val tree = new NaryTree(count)
    tree.add(null, rootNodeName)

    val start = data.get(rootNodeName).get

    val startNode = tree.find(rootNodeName)
    start.foreach(e => tree.add(startNode, e.action, e.probability))

    val queue = Queue[NaryTreeNode]()
    queue.enqueue(startNode.children: _*)

    while (!queue.isEmpty) {
      val node = queue.dequeue
      val children = data.get(node.name)
      if (!children.isEmpty) {
        children.get.map(e => tree.add(node, e.action, e.probability))
        queue.enqueue(node.children.filter(e => e.name != rootNodeName): _*)
      }
    }
    return tree
  }

  def treeToScenarios(tree: NaryTree = null): Unit = {

    val rootPackage = "actions"
    val reflections = new Reflections(rootPackage)
    val templates = reflections.getSubTypesOf(classOf[BaseActions]).asScala
    val httpRequestBuilder = classOf[HttpRequestBuilder]

    val map = templates.flatMap(template => {
      template.getDeclaredMethods.filter(e => e.getReturnType == httpRequestBuilder)
    }).map(e => {
      (e.getName, e.invoke(null, e.getParameterTypes.map(e => null)))
    }) toMap

    println(map)

  }

  def main(args: Array[String]): Unit = {

    val json = readFile("src/test/resources/plan.json")

    val data = deserializeJson[Map[String, List[Action]]](json)

    println(data)

    val count = 100

    val tree = buildTree(data, count)

    println("isBalancedProbability: " + tree.isBalancedProbability)

    val paths = tree.paths

    treeToScenarios();

  }
}
