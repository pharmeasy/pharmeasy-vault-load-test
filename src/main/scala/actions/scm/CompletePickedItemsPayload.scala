package actions.scm

case class CompletePickedItemsPayload(
                                       binId: String,
                                       pickerTaskId: Long,
                                       sidelinedReason: String,
                                       status: String,
                                       ucode: String
                                     )
