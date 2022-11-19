package pages

import api.httpClient
import io.ktor.client.request.*
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.button
import react.useState
import model.StatisticRequest
import react.dom.html.ReactHTML.br
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.span
import utils.LOADER_SRC
import kotlin.math.max
import kotlin.math.min

const val pageSize = 3

external interface HistoryPageProps : Props {
    var onContentChange: (List<StatisticRequest>) -> Unit
    var onNewPage: (Int) -> Unit
    var setLoadingStatus: (Boolean) -> Unit
    var setTotalCount: (Int) -> Unit
}

suspend fun getQueryList(pageStart: Int, totalCount: Int): List<StatisticRequest> {
    if (pageStart < 0 || pageStart >= totalCount) {
        return emptyList()
    }
    return httpClient.request("/api/v1/get-history") {
        parameter("start", pageStart)
        parameter("count", pageSize)
    }
}

suspend fun getTotalCount(): Int = httpClient.request("/api/v1/get-total-count")

val HistoryPage = FC<HistoryPageProps> { props ->
    var pageStart: Int by useState(0)
    var totalCount: Int by useState(0)
    var currentContent: List<StatisticRequest> by useState(emptyList())
    var initialRender: Boolean by useState(false)
    var loading: Boolean by useState(true)

    props.setTotalCount = { total ->
        totalCount = total
    }

    ApplicationScope.launch {
        if (!initialRender) {
            props.setLoadingStatus(true)
            val total = getTotalCount()
            props.setTotalCount(total)
            val list = getQueryList(0, totalCount)
            props.onContentChange(list)
        }
    }

    props.onContentChange = { content ->
        currentContent = content
        loading = false
        initialRender = true
    }

    props.onNewPage = { newPageStart ->
        ApplicationScope.launch {
            props.setLoadingStatus(true)
            val queryList = getQueryList(newPageStart, totalCount)
            props.setLoadingStatus(false)
            if (queryList.isNotEmpty()) {
                props.onContentChange(queryList)
                pageStart = newPageStart
            }
        }
    }

    props.setLoadingStatus = { loadingStatus ->
        loading = loadingStatus
    }

    h2 { + "History" }

    div {
        id = "history-container"

        if (loading) {
            img {
                id = "loaderImage"

                src = LOADER_SRC
            }
        } else {
            for (statisticRequest in currentContent) {
                div {
                    className = "historyItem"

                    div {
                        className = "subHistoryItem"
                        span {
                            +"Url: ${statisticRequest.url}"
                        }
                        br()
                        span {
                            +"Level: ${statisticRequest.level}"
                        }
                    }
                }
            }
        }
    }

    button {
        onClick = {
            props.onNewPage(pageStart - pageSize)
        }

        if (!initialRender) {
            className = "disabled"

            +"Previous... [loading]"
        } else if (hasPreviousPage(pageStart)) {
            if (loading) className = "disabled"

            +"Previous... [entries ${previousStart(pageStart)}-${previousEnd(pageStart)}]"
        } else {
            className = "disabled"

            +"[no previous entries]"
        }
    }

    button {
        onClick = {
            props.onNewPage(pageStart + pageSize)
        }

        if (!initialRender) {
            className = "disabled"

            +"Next... [loading]"
        } else if (hasNextPage(pageStart, totalCount)) {
            if (loading) className = "disabled"

            +"Next... [entries ${nextStart(pageStart, totalCount)}-${nextEnd(pageStart, totalCount)}]"
        } else {
            className = "disabled"

            +"[no next entries]"
        }
    }
}

private fun hasPreviousPage(page: Int) = page > 0
private fun hasNextPage(page: Int, totalCount: Int) = page + 1 < totalCount
private fun previousStart(page: Int) = max(0, page - pageSize)
private fun previousEnd(page: Int) = max(0, page - 1)
private fun nextStart(page: Int, totalCount: Int) = min(totalCount - 1, page + pageSize)
private fun nextEnd(page: Int, totalCount: Int) = min(totalCount - 1, page + pageSize * 2 - 1)