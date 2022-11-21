package application.plugins

import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.html.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import application.templates.index
import application.workers.AuthenticationManager
import application.workers.HistoryManager
import application.workers.StatisticWorker
import model.StatisticRequest
import model.UserCredentials
import model.WrongUserCredentials

@ExperimentalSerializationApi
fun Application.configureRouting() {
    routing {
        configureApi()
        configureWeb()
    }
}


fun Routing.configureWeb() {
    get("{...}") {
        call.respondHtml(HttpStatusCode.OK, HTML::index)
    }

    static("/static") {
        resources()
    }
}

@ExperimentalSerializationApi
fun Routing.configureApi() {
    route("/api/v1") {
        configurePrivateApi()
        configureAuthorizationApi()
    }
}

@ExperimentalSerializationApi
fun Route.configurePrivateApi() {
    get("/page-info") {
        val url = call.request.queryParameters["url"]!!
        val level = call.request.queryParameters["level"]!!.toInt()
        val request = StatisticRequest(url, level)

        val response = HistoryManager.getStatistics(request)
            ?: StatisticWorker.processAll(StatisticRequest(url, level))

        call.respond(HttpStatusCode.OK, response)
    }

    get("/save-history") {
        val statisticRequest = call.request.queryParameters["request"]!!
        val content = call.request.queryParameters["content"]!!

        HistoryManager.saveStatistics(Json.decodeFromString(statisticRequest), Json.decodeFromString(content))
    }

    get("/get-history") {
        val start = call.request.queryParameters["start"]!!.toInt()
        val count = call.request.queryParameters["count"]!!.toInt()

        val response = HistoryManager.getHistory(start, count)
        call.respond(HttpStatusCode.OK, response)
    }

    get("/get-total-count") {
        call.respond(HttpStatusCode.OK, HistoryManager.totalCount())
    }
}

fun Route.configureAuthorizationApi() {
    get("/login") {
        val credentials = Json.decodeFromString<UserCredentials>(call.request.queryParameters["credentials"]!!)
        if (AuthenticationManager.isValidUser(credentials)) {
            AuthenticationManager.setCurrentUser(credentials.username)
            call.respond(credentials)
        } else {
            call.respond(WrongUserCredentials)
        }
    }

    get("/sign-in") {
        val credentials = Json.decodeFromString<UserCredentials>(call.request.queryParameters["credentials"]!!)
        if (credentials.username != null && !AuthenticationManager.hasUserWithUsername(credentials.username)) {
            AuthenticationManager.putUser(credentials)
            AuthenticationManager.setCurrentUser(credentials.username)
            call.respond(credentials)
        } else {
            call.respond(WrongUserCredentials)
        }
    }

    get("/logout") {
        val username = AuthenticationManager.getCurrentUser()
        AuthenticationManager.setCurrentUser(null)
        call.respond(UserCredentials(username, null))
    }
}