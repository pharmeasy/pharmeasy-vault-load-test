package utils

import scala.collection.mutable.ListBuffer

case class NaryTreeNode[T](name: String, data: T, var prev: NaryTreeNode[T], var children: ListBuffer[NaryTreeNode[T]])(implicit number: Numeric[T]) {
  override def toString: String = s"NaryTreeNode: name: ${name}, data: ${data}, prev: ${if (prev == null) "null" else prev.name} ,children: ${children}"
}
