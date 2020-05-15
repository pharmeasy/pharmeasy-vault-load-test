package actions.redbook

import java.sql.Timestamp

case class UpdatePayload (
                           reason: String,
                           status: String,
                           timestamp: Timestamp
                         )