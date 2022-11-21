package pages

import components.AuthorizationForm
import kotlinx.coroutines.launch
import react.*
import security.useAuth

val SignInPage = FC<Props> {
    val signIn = useAuth()::signIn

    AuthorizationForm {
        actionText = "Sign in"
        action = {
            ApplicationScope.launch {
                signIn(it)
            }
        }
    }
}