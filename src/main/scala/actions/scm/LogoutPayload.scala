package actions.scm

case class LogoutPayload(
                          action: String,
                          role: String,
                          userId: String
                        )


