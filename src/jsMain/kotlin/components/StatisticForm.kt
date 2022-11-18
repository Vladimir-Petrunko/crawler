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

external interface StatisticFormProps : Props {
    var onContentChange: (StatisticResponse) -> Unit
    var onLoadStart: () -> Unit
}

private var targetUrl = ""
private var level = 0

private const val LOADER_SRC = "https://i.gifer.com/origin/ae/ae84325701f6d97ac4ad7e7951ac9063_w200.webp"

val StatisticForm = FC<StatisticFormProps> { props ->
    var currentContent: StatisticResponse? by useState(null)
    var isLoading: Boolean by useState(false)

    props.onContentChange = { content ->
        currentContent = content
        isLoading = false
    }

    props.onLoadStart = {
        currentContent = null
        isLoading = true
    }

    div {
        id = "crawl-form"

        label {
            htmlFor = "url"

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
            htmlFor = "level"

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
                        parameter("content", JSON.stringify(currentContent))
                    }
                }
            }

            + "Save to history..."
        }

        div {
            ol {
                for (word in currentContent?.topWorlds ?: emptyList()) {
                    li {
                        + word
                    }
                }
            }
        }

        div {
            id = "imageBasket"

            if (!isLoading) {
                for (path in currentContent?.images ?: emptySet()) {
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