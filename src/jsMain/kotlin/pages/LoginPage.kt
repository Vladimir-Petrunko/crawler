package pages

import components.AuthorizationForm
import kotlinx.coroutines.launch
import react.*
import security.useAuth

private var username = ""
private var password = ""

val LoginPage = FC<Props> {
    val logIn = useAuth()::login

    AuthorizationForm {
        actionText = "Log in"
        action = {
            ApplicationScope.launch {
                logIn(it)
            }
        }
    }
}