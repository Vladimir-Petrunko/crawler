package application.templates

import kotlinx.html.*

fun HTML.index() {
    head {
        title("Website Crawler v1.0")
        styleLink(url = "/static/css/normalize.css")
    }
    body {
        div {
            id = "root"
        }
        script(src = "/static/template.js") {}
    }
}