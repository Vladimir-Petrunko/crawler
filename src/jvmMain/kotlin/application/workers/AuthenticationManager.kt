package application.workers

import kotlinx.coroutines.runBlocking
import model.UserCredentials
import org.litote.kmongo.coroutine.*
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo

class AuthenticationManager {

    data class CurrentUser(
        val userName: String
    )

    data class UserModel(
        val userName: String,
        val hashedPassword: Int
    )

    companion object {
        private val client = KMongo.createClient().coroutine
        private val database = client.getDatabase("@localhost")
        private val allUsersCollection = database.getCollection<UserModel>()
        private val currentUserCollection = database.getCollection<CurrentUser>()

        fun putUser(credentials: UserCredentials) {
            if (hasUserWithUsername(credentials.username!!)) {
                throw IllegalArgumentException("user with username ${credentials.username} already exists")
            }
            runBlocking {
                allUsersCollection.insertOne(UserModel(credentials.username, credentials.password.hashCode()))
            }
        }

        fun hasUserWithUsername(userName: String) =
            runBlocking {
                allUsersCollection.findOne(UserModel::userName eq userName)
            } != null

        fun isValidUser(credentials: UserCredentials) =
            runBlocking {
                allUsersCollection.findOne(
                    UserModel::userName eq credentials.username,
                    UserModel::hashedPassword eq credentials.password.hashCode()
                )
            } != null

        fun setCurrentUser(userName: String?) =
            runBlocking {
                if (userName == null) {
                    currentUserCollection.drop()
                } else {
                    currentUserCollection.insertOne(CurrentUser(userName))
                }
            }

        fun getCurrentUser() =
            runBlocking {
                currentUserCollection.find().toList().first().userName
            }
    }
}