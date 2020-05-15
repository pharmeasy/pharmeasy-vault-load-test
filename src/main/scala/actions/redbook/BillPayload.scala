package actions.redbook

case class BillPayload(
                        notes: String,
                        prescriptions: Seq[String],
                        status: String,
                        `type`: String,
                        medicines: Seq[Medicines],
                        app_id: String,
                        invoice_url: Seq[String],
                        promised_deliver_date: String,
                        reference_order_id: String,
                        user_id: String,
                        invoice_number: String,
                        invoiced_items: Seq[InvoicedItems],
                        invoice_amount: String,
                        package_id: String
                      )
