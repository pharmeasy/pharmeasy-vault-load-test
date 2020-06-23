package actions.scm

case class SignInPickerAppPayload(
                                   app: String,
                                   domainName: String,
                                   fcmToken: String,
                                   pwd: String,
                                   user: String)


