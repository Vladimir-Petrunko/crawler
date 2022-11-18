package pages

import components.StatisticForm
import react.*
import react.dom.html.ReactHTML.h2

val CrawlPage = FC<Props> {
    h2 { + "Crawl" }

    StatisticForm()
}