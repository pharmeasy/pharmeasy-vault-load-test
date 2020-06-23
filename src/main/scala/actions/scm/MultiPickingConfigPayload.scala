package actions.scm

case class MultiPickingConfigPayload(minOrders: Integer,
                              maxOrders: Integer,
                              maxOrderWaitTimeInSeconds: Integer,
                              pickerSelector: String,
                              pickerSelectorOption: String)


