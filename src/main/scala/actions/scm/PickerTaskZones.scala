package actions.scm

case class PickerTaskZones(pickerTaskId: Long,
                           trayId: String,
                           status: String,
                           trayDestinationZone: String
                          )
