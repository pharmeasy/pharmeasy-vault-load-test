package utils

import scala.collection.mutable.ListBuffer

case class NaryTreeNode(name: String, probability: Double, actualValue: Long, var prev: NaryTreeNode, var children: ListBuffer[NaryTreeNode]) {
  override def toString: String = s"NaryTreeNode: name: ${name}, probability: ${probability}, actualValue: ${actualValue}, prev: ${if (prev == null) "null" else prev.name} ,children: ${children}"
}
