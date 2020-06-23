package actions.scm

case class B2BOrderPayload(

                            uniqueId: String,
                            orderDate: String,
                            orderStatus: String,
                            orderPriority: Double,
                            distributorRetailerCode: String,
                            orderItems: String

                          )



