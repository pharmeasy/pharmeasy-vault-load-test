package actions.redbook

case class InvoicedItems(
                          batches: Seq[Batches],
                          name: String,
                          billed_quantity: String,
                          mdm_id: String,
                          medicine_id: String,
                          medicine_name: String
                        )
