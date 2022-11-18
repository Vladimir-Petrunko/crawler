package pages

import react.FC
import react.Props
import react.dom.html.ReactHTML

val HomePage = FC<Props> {
    ReactHTML.h2 { +"Home" }
}
