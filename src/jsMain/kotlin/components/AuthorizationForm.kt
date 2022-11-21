package components

import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.dom.html.InputType
import model.UserCredentials
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.form
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label

external interface AuthorizationFormProps : Props {
    var actionText: String
    var action: (UserCredentials) -> Unit
}

private var username = ""
private var password = ""

val AuthorizationForm = FC<AuthorizationFormProps> { props ->
    h2 { + props.actionText }

    form {
        label {
            +"Username: "
        }

        input {
            onChange = { event ->
                username = event.target.value
            }
        }

        label {
            +"Password: "
        }

        input {
            type = InputType.password
            onChange = { event ->
                password = event.target.value
            }
        }

        button {
            onClick = { it ->
                it.preventDefault()
                ApplicationScope.launch {
                    props.action(UserCredentials(username, password))
                    username = ""
                    password = ""
                }
            }
            +props.actionText
            +"!"
        }
    }
}