package utils

import scala.collection.mutable.ListBuffer

class NaryTree[T](var weight: T, var root: NaryTreeNode[T] = null)(implicit number: Numeric[T]) {

  override def toString: String = s"NaryTree: ${root}"

  def add(prev: NaryTreeNode[T], name: String, data: T): utils.NaryTreeNode[T] = {
    import number._
    if (prev == null) {
      // assuming the new node is going to be the parent
      root = NaryTreeNode(name, weight, null, null)
    } else {
      val elm = find(prev.name)
      if (elm == null) {
        throw new IllegalAccessException(s"unable to find node with parent node having data ${prev.data}")
      } else {
        if (elm.children == null) {
          elm.children = ListBuffer();
        }
        elm.children += NaryTreeNode(name, prev.data * data, elm, null)
      }
    }
    root
  }

  def find(name: String, node: NaryTreeNode[T] = root): NaryTreeNode[T] = {
    if (node == null) {
      return null
    } else {
      if (node.name == name) {
        return node
      } else {
        if (node.children == null || node.children.length == 0) {
          null
        } else {
          node.children.foreach(child => {
            val elm = find(name, child)
            if (elm != null) {
              return elm
            }
          })
        }
      }
    }
    null
  }

}
