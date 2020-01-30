package utils

import Utilities._

object DynamicScenarioBuilder {

  def main(args: Array[String]): Unit = {

    val json = readFile("src/test/resources/plan.json")

    val map = serializeJson[Map[String, Any]](json)

    println(map)

    val count = 100

    val jsonOut = deserializeJson(map)
    println(jsonOut)

    val tree = new NaryTree[Double](100)
    tree.add(null, "start", 5)

    val findStart = tree.find("start")
    tree.add(findStart, "home", 0.2)
    tree.add(findStart, "search", 0.4)

    println(tree)

  }
}
