package pages

import kotlinx.coroutines.launch
import react.*
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.h2
import react.router.useNavigate
import security.useAuth

val LogoutPage = FC<Props> {
    val logout = useAuth()::logout
    val navigate = useNavigate()

    h2 { + "Log out" }

    button {
        onClick = {
            ApplicationScope.launch {
                logout()
                navigate("/log-in")
            }
        }
        + "Log out!"
    }
}