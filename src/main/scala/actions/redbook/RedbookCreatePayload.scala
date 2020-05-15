package actions.redbook

case class RedbookCreatePayload(
                          doctor: Doctor,
                          medicines: String,
                          meta: Meta,
                          notes: String,
                          patient: Patient,
                          prescriptions: Seq[String],
                          promised_deliver_date: String,
                          reference_order_id: String,
                          `type`: String
                        )

