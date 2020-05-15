package actions.redbook.response

case class Address(
                    address1: String,
                    address2: String,
                    city: String,
                    customerName: String,
                    mobileNo: String,
                    pincode: String
                  )

case class Doctor(
                   doctorName: String,
                   doctorAddress: String,
                   doctorPhone: String
                 )

case class Medicines(
                      composition: String,
                      manufacturer: String,
                      mrp: Int,
                      name: String,
                      packaging: String,
                      qty: Int,
                      discount_percent: Int,
                      mdm_id: String,
                      medicine_id: String
                    )

case class Meta(
                 address: Address,
                 deliverAll: String,
                 deliverAllText: String,
                 timeSlot: String
               )

case class Patient(
                    patientEmail: String,
                    patientId: String,
                    patientName: String,
                    patientPhoneNumber: String
                  )

case class CreateResponse(
                           doctor: Doctor,
                           medicines: Seq[Medicines],
                           meta: Meta,
                           notes: String,
                           patient: Patient,
                           prescriptions: Seq[String],
                           `type`: String,
                           promised_deliver_date: String,
                           reference_order_id: String
                         )

