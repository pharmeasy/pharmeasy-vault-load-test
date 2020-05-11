package actions.scm

import scala.collection.mutable.ArrayBuffer

case class ScanZonePayload(pickerTaskZones: ArrayBuffer[PickerTaskZones]
                          )
