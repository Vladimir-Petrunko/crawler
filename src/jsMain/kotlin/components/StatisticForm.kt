package components

import api.httpClient
import io.ktor.client.request.*
import io.ktor.client.statement.*
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
    var onChangeSaveStatus: (SavedStatus) -> Unit
}

private var targetUrl = ""
private var level = 0

enum class SavedStatus(val representation: String) {
    NOT_SAVED("Save to history!"),
    SAVING("Saving..."),
    SAVED("Saved!")
}

val StatisticForm = FC<StatisticFormProps> { props ->
    var currentResponse: StatisticResponse? by useState(null)
    var isLoading: Boolean by useState(false)
    var savedStatus: SavedStatus by useState(SavedStatus.NOT_SAVED)
    var hasLoadedSomething: Boolean by useState(false)

    props.onContentChange = { content ->
        currentResponse = content
        isLoading = false
        hasLoadedSomething = true
        savedStatus = SavedStatus.NOT_SAVED
    }

    props.onLoadStart = {
        currentResponse = null
        isLoading = true
    }

    props.onChangeSaveStatus = { newStatus ->
        savedStatus = newStatus
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
            if (isLoading || savedStatus != SavedStatus.NOT_SAVED || !hasLoadedSomething) {
                className = "disabled"
            }

            onClick = {
                ApplicationScope.launch {
                    props.onChangeSaveStatus(SavedStatus.SAVING)
                    it.preventDefault()
                    val response: Int = httpClient.request("/api/v1/save-history") {
                        parameter("request", JSON.stringify(StatisticRequest(targetUrl, level)))
                        parameter("content", JSON.stringify(currentResponse))
                    }
                    props.onChangeSaveStatus(if (response == 200) SavedStatus.SAVED else SavedStatus.NOT_SAVED)
                }
            }

            + savedStatus.representation
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