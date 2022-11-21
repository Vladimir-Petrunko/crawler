package pages

import components.AuthorizationForm
import kotlinx.coroutines.launch
import react.*
import react.router.useNavigate
import security.useAuth

val SignInPage = FC<Props> {
    val signIn = useAuth()::signIn
    val navigate = useNavigate()

    AuthorizationForm {
        actionText = "Sign in"
        action = {
            ApplicationScope.launch {
                signIn(it)
                navigate("/")
            }
        }
    }
}