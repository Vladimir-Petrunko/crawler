import pages.*
import react.FC
import react.Props
import react.create
import react.createElement
import react.router.Route
import react.router.Routes
import react.router.dom.BrowserRouter

val App = FC<Props> {
    BrowserRouter {
        AppRoutes {}
    }
}

val AppRoutes = FC<Props> {
    Routes {
        Route {
            path = "/"
            element = CommonPage.create {
                menuItems =
                    mapOf("/" to "Home", "history" to "History", "crawl" to "Crawl")
            }

            Route {
                index = true
                element = createElement(HomePage)
            }

            Route {
                path = "history"
                element = createElement(HistoryPage)
            }

            Route {
                path = "crawl"
                element = createElement(CrawlPage)
            }

            Route {
                path = "*"
                element = createElement(NotFoundPage)
            }
        }
    }
}