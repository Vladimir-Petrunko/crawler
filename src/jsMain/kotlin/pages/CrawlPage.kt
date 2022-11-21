package pages

import components.StatisticForm
import components.StatisticFormProps
import react.*
import react.dom.html.ReactHTML.h2

val CrawlPage = FC<Props> {
    h2 { + "Crawl" }

    StatisticForm()
}