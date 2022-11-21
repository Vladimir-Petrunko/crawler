import pages.*
import react.FC
import react.Props
import react.create
import react.createElement
import react.router.Route
import react.router.Routes
import react.router.dom.BrowserRouter
import security.AuthProvider
import security.useAuth
import utils.Resource

val App = FC<Props> {
    BrowserRouter {
        AuthProvider {
            AppRoutes {}
        }
    }
}

val AppRoutes = FC<Props> {
    val isAuthorized = useAuth().user is Resource.Ok

    Routes {
        Route {
            path = "/"
            element = CommonPage.create {
                menuItems =
                    mapOf("/" to "Home") +
                        if (isAuthorized) {
                            mapOf("history" to "History", "crawl" to "Crawl", "log-out" to "Log out")
                        }
                        else {
                            mapOf("log-in" to "Log in", "sign-in" to "Sign in")
                        }
            }

            Route {
                index = true
                element = createElement(HomePage)
            }

            if (isAuthorized) {
                Route {
                    path = "crawl"
                    element = createElement(CrawlPage)
                }

                Route {
                    path = "history"
                    element = createElement(HistoryPage)
                }

                Route {
                    path = "log-out"
                    element = createElement(LogoutPage)
                }
            } else {
                Route {
                    path = "log-in"
                    element = createElement(LoginPage)
                }

                Route {
                    path = "sign-in"
                    element = createElement(SignInPage)
                }
            }

            Route {
                path = "*"
                element = createElement(NotFoundPage)
            }
        }
    }
}