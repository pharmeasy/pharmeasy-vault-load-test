package actions.hydra

case class CreateOrder(
                        address: Address,
                        customerEmail: String,
                        customerMobile: String,
                        customerName: String,
                        customerRemark: String,
                        doctorName: String,
                        items: String,
                        orderId: String,
                        patientName: String,
                        prescriptions: Seq[String],
                        promisedDeliveryDate: String,
                        retailerId: String,
                        sourceWorkflowId: String
                      )
