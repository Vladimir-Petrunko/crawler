package pages

import components.UserInfo
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.hr
import react.dom.html.ReactHTML.nav
import react.key
import react.router.Outlet
import react.router.dom.Link

external interface CommonPageProps : Props {
    var menuItems: Map<String, String>
}

val CommonPage = FC<CommonPageProps> { props ->
    nav {
        props.menuItems.forEach {
            Link {
                key = it.key
                to = it.key

                +it.value
            }
            +" "
        }
    }

    hr()

    UserInfo()

    hr()

    div {

    }

    Outlet()
}
