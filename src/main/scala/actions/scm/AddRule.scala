package actions.scm

case class AddRule(
                           days: List[Double],
                           timeFrom: String,
                           timeTo: String,
                           bucketSize: Double,
                           tree: List[Tree]
                         )