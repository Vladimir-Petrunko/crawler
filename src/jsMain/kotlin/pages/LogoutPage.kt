package pages

import kotlinx.coroutines.launch
import react.*
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.h2
import security.useAuth

val LogoutPage = FC<Props> {
    val logout = useAuth()::logout

    h2 { + "Log out" }

    button {
        onClick = {
            ApplicationScope.launch {
                logout()
            }
        }
        + "Log out!"
    }
}