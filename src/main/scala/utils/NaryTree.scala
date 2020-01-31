package utils

import scala.collection.mutable.ListBuffer

class NaryTree(var weight: Long, var root: NaryTreeNode = null) {

  override def toString: String = s"NaryTree: ${root}"

  def add(prev: NaryTreeNode, name: String, probability: Double = 1.0): utils.NaryTreeNode = {

    if (probability > 1.0) {
      throw new IllegalArgumentException("probability cannot be more than 1.0")
    }
    if (prev == null) {
      // assuming the new node is going to be the parent
      root = NaryTreeNode(name, 100, weight, null, null)
    } else {
      val elm = find(prev.name)
      if (elm == null) {
        throw new IllegalArgumentException(s"unable to find node with parent node having name '${prev.name}'")
      } else {
        if (elm.children == null) {
          elm.children = ListBuffer();
        }
        val probSum = elm.children.map(_.probability).sum
        if (probSum + probability > 1.0) {
          throw new IllegalArgumentException(s"cummulative probability of all children ${prev.children.map(e => s"name: ${e.name}, probability: ${e.probability}") toList} + new child '${name}' probability '${probability}' of parent node '${prev.name}' is more than 1.0")
        }
        elm.children += NaryTreeNode(name, probability, Math.ceil(prev.actualValue * probability).longValue, elm, null)
      }
    }
    root
  }

  def isBalancedProbability: Boolean = _validateProbability(root)

  private def _validateProbability(node: NaryTreeNode): Boolean = {
    if (node == null || node.children == null || node.children.size == 0) {
      return true
    }
    val probability = node.children.map(_.probability).sum;
    var isTrue = probability == 1.0
    if (!isTrue) {
      println(s"node '${node.name}' cummulative probability of children '${node.children.map(e => s"name: ${e.name}, probability: ${e.probability}") toList}' is '$probability' != '1.0'")
      isTrue = false
    } else {
      for (child <- node.children) {
        isTrue &= _validateProbability(child)
      }
    }
    return isTrue
  }

  def find(name: String, node: NaryTreeNode = root): NaryTreeNode = {
    if (node == null) {
      return null
    } else {
      if (node.name.trim == name.trim) {
        return node
      } else {
        if (node.children == null || node.children.length == 0) {
          return null
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
    return null
  }

  def leafNodes: List[NaryTreeNode] = {
    val buffer = ListBuffer[NaryTreeNode]()
    _leafNodes(root, buffer)
    buffer.toList
  }

  def paths: List[List[NaryTreeNode]] = {
    val nodes = leafNodes;
    if (nodes == null || nodes.size == 0) {
      return List.empty
    } else {
      return nodes.map(node => {
        var tmpNode = node;
        val buffer = new ListBuffer[NaryTreeNode]()
        while (tmpNode.prev != null) {
          tmpNode +=: buffer;
          tmpNode = tmpNode.prev
        }
        buffer.toList
      }) toList
    }
  }

  private def _leafNodes(node: NaryTreeNode, buffer: ListBuffer[NaryTreeNode]): Unit = {
    if (node == null) {
      return ;
    } else if (node.children == null || node.children.size == 0) {
      buffer += node
    } else {
      for (child <- node.children) {
        _leafNodes(child, buffer)
      }
    }
  }

}
