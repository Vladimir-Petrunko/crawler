package pages

import api.httpClient
import io.ktor.client.request.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.promise
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.button
import react.useState
import model.StatisticRequest
import kotlin.js.Promise

const val pageSize = 3

external interface HistoryPageProps : Props {
    var onContentChange: (List<StatisticRequest>) -> Unit
    var onNewPage: (Int) -> Unit
}

suspend fun getQueryList(pageStart: Int): List<StatisticRequest> {
    if (pageStart < 0) {
        return emptyList()
    }
    return httpClient.request("/api/v1/get-history") {
        parameter("start", pageStart)
        parameter("count", pageSize)
    }
}

val HistoryPage = FC<HistoryPageProps> { props ->
    val initQueryList: () -> List<StatisticRequest> = {
        ApplicationScope.promise {
            getQueryList(0)
        }.then {
            props.onContentChange(it)
        }
        emptyList()
    }

    var pageStart: Int by useState(0)
    var currentContent: List<StatisticRequest> by useState(initQueryList())

    props.onContentChange = { content ->
        currentContent = content
    }

    props.onNewPage = { newPageStart ->
        ApplicationScope.launch {
            val queryList = getQueryList(newPageStart)
            if (queryList.isNotEmpty()) {
                props.onContentChange(queryList)
                pageStart = newPageStart
            }
        }
    }

    h2 { + "History" }

    div {
        id = "history-container"

        for (statisticRequest in currentContent) {
            div {
                className = "historyItem"

                div {
                    className = "subHistoryItem"

                    + "Url: ${statisticRequest.url}"
                }

                div {
                    className = "subHistoryItem"

                    + "Level: ${statisticRequest.level}"
                }
            }
        }
    }

    button {
        onClick = {
            props.onNewPage(pageStart - pageSize)
        }

        + "Previous page"
    }

    button {
        onClick = {
            props.onNewPage(pageStart + pageSize)
        }

        + "Next page"
    }
}