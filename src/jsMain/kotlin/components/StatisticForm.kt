package components

import api.httpClient
import io.ktor.client.request.*
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.dom.html.ReactHTML.br
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.label
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.ol
import react.useState
import model.StatisticRequest
import model.StatisticResponse
import utils.LOADER_SRC

external interface StatisticFormProps : Props {
    var onContentChange: (StatisticResponse) -> Unit
    var onLoadStart: () -> Unit
}

private var targetUrl = ""
private var level = 0

val StatisticForm = FC<StatisticFormProps> { props ->
    var currentResponse: StatisticResponse? by useState(null)
    var isLoading: Boolean by useState(false)

    props.onContentChange = { content ->
        currentResponse = content
        isLoading = false
    }

    props.onLoadStart = {
        currentResponse = null
        isLoading = true
    }

    div {
        id = "crawl-form"

        label {
            + "Url:"
        }

        br()

        input {
            name = "url"

            onChange = { event ->
                targetUrl = event.target.value
            }
        }

        br()

        label {
            + "Level:"
        }

        br()

        input {
            name = "level"

            onChange = { event ->
                level = event.target.value.toInt()
            }
        }

        br()

        button {
            onClick = {
                ApplicationScope.launch {
                    it.preventDefault()
                    props.onLoadStart()
                    val statisticResponse: StatisticResponse = httpClient.request("/api/v1/page-info") {
                        parameter("url", targetUrl)
                        parameter("level", level)
                    }
                    props.onContentChange(statisticResponse)
                }
            }

            + "Crawl!"
        }

        button {
            onClick = {
                ApplicationScope.launch {
                    it.preventDefault()
                    httpClient.request("/api/v1/save-history") {
                        parameter("request", JSON.stringify(StatisticRequest(targetUrl, level)))
                        parameter("content", JSON.stringify(currentResponse))
                    }
                }
            }

            + "Save to history..."
        }

        div {
            ol {
                for (word in currentResponse?.topWords ?: emptyList()) {
                    li {
                        + word
                    }
                }
            }
        }

        div {
            id = "imageBasket"

            if (!isLoading) {
                for (path in currentResponse?.images ?: emptySet()) {
                    img {
                        src = path
                        width = 50.0
                        height = 50.0
                    }
                }
            } else {
                img {
                    id = "loaderImage"

                    src = LOADER_SRC
                }
            }

        }
    }
}