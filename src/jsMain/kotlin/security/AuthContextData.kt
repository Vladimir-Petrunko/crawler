package security

import api.httpClient
import io.ktor.client.request.*
import kotlinx.coroutines.launch
import model.UserCredentials
import model.WrongUserCredentials
import react.StateInstance
import react.StateSetter
import utils.Resource

typealias UserResource = Resource<String>

class AuthContextData(
    private val userState: StateInstance<UserResource>?
) {
    val user: UserResource?
        get() = userState?.component1()

    private val setUser: StateSetter<UserResource>?
        get() = userState?.component2()

    suspend fun signIn(credentials: UserCredentials) {
        if (user !is Resource.Ok) {
            setUser!!(Resource.Loading)
            ApplicationScope.launch {
                val response: UserCredentials = httpClient.request("/api/v1/sign-in") {
                    parameter("credentials", JSON.stringify(credentials))
                }
                if (response != WrongUserCredentials) {
                    setUser!!(Resource.Ok(credentials.username!!))
                } else {
                    setUser!!(Resource.Empty)
                }
            }
        }
    }

    suspend fun login(credentials: UserCredentials) {
        if (user !is Resource.Ok) {
            setUser!!(Resource.Loading)
            ApplicationScope.launch {
                val response: UserCredentials = httpClient.request("/api/v1/login") {
                    parameter("credentials", JSON.stringify(credentials))
                }
                if (response != WrongUserCredentials) {
                    setUser!!(Resource.Ok(credentials.username!!))
                } else {
                    setUser!!(Resource.Empty)
                }
            }
        }
    }

    suspend fun logout() {
        if (user is Resource.Ok) {
            val username = (user as Resource.Ok).data
            setUser!!(Resource.Loading)
            ApplicationScope.launch {
                val response: UserCredentials = httpClient.request("/api/v1/logout")
                if (response != WrongUserCredentials) {
                    setUser!!(Resource.Empty)
                } else {
                    setUser!!(Resource.Ok(username))
                }
            }
        }
    }

    companion object {
        fun create(userState: StateInstance<UserResource>): AuthContextData = AuthContextData(userState)
        fun empty(): AuthContextData = AuthContextData(null)
    }
}