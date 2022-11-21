package components

import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import security.UserResource
import security.useAuth
import utils.Resource

val UserInfo = FC<Props> {
    val user = useAuth().user

    div {
        id = "auth-header"

        + user.statusName()
    }
}

private fun UserResource?.statusName(): String {
    return if (this == null) {
        "[Not authorized]"
    } else if (this is Resource.Ok) {
        "[Authorized as ${this.data}]"
    } else if (this == Resource.Loading) {
        "[Authorizing...]"
    } else {
        "[Not authorized]"
    }
}