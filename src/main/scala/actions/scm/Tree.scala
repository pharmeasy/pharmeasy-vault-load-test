package actions.scm

case class Tree(
                 id: Double,
                 `type`: String,
                 treeNode: List[TreeNode],
                 hardRule: Boolean
               )