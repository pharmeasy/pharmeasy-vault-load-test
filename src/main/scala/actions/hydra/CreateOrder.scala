package actions.hydra

import java.time.LocalDateTime

import actions.scm.Address

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
