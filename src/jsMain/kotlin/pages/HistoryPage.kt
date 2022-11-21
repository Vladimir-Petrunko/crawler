package pages

import api.httpClient
import io.ktor.client.request.*
import kotlinx.coroutines.launch
import model.Statistic
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h2
import react.dom.html.ReactHTML.button
import react.useState
import model.StatisticRequest
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.br
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.ol
import react.dom.html.ReactHTML.span
import utils.LOADER_SRC
import kotlin.math.max
import kotlin.math.min

const val pageSize = 3

external interface HistoryPageProps : Props {
    var onContentChange: (List<Statistic>) -> Unit
    var onNewPage: (Int) -> Unit
    var setLoadingStatus: (Boolean) -> Unit
    var setTotalCount: (Int) -> Unit
    var toggleVisibility: (StatisticRequest) -> Unit
}

suspend fun getQueryList(pageStart: Int, totalCount: Int): List<Statistic> {
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
    var currentContent: List<Statistic> by useState(emptyList())
    var initialRender: Boolean by useState(false)
    var loading: Boolean by useState(true)
    var visibleResponses: MutableSet<StatisticRequest> by useState(mutableSetOf())

    props.toggleVisibility = { request ->
        if (visibleResponses.contains(request)) {
            visibleResponses.remove(request)
        } else {
            visibleResponses.add(request)
        }
        val temp = mutableSetOf<StatisticRequest>()
        temp.addAll(visibleResponses)
        visibleResponses = temp
    }

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
            for (statistic in currentContent) {
                div {
                    className = "historyItem"

                    div {
                        className = "subHistoryItem"
                        span {
                            +"Url: ${statistic.statisticRequest.url}"
                        }
                        br()
                        span {
                            +"Level: ${statistic.statisticRequest.level}"
                        }
                    }

                    if (visibleResponses.contains(statistic.statisticRequest)) {
                        br()
                        br()
                        div {
                            ol {
                                for (word in statistic.statisticResponse.topWords) {
                                    li {
                                        +word
                                    }
                                }
                            }
                        }

                        div {
                            id = "imageBasket"

                            for (path in statistic.statisticResponse.images) {
                                img {
                                    src = path
                                    width = 50.0
                                    height = 50.0
                                }
                            }

                        }
                    }

                    onClick = {
                        props.toggleVisibility(statistic.statisticRequest)
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
private fun hasNextPage(page: Int, totalCount: Int) = page + pageSize < totalCount
private fun previousStart(page: Int) = max(0, page - pageSize)
private fun previousEnd(page: Int) = max(0, page - 1)
private fun nextStart(page: Int, totalCount: Int) = min(totalCount - 1, page + pageSize)
private fun nextEnd(page: Int, totalCount: Int) = min(totalCount - 1, page + pageSize * 2 - 1)