package pages

import components.AuthorizationForm
import kotlinx.coroutines.launch
import react.*
import react.router.useNavigate
import security.useAuth

val LoginPage = FC<Props> {
    val logIn = useAuth()::login
    var navigate = useNavigate()

    AuthorizationForm {
        actionText = "Log in"
        action = {
            ApplicationScope.launch {
                logIn(it)
                navigate("/")
            }
        }
    }
}