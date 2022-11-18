package application.workers

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class ImageHasher {
    data class ImageHash(
        val imageUrl: String,
        val imageHash: String
    )

    companion object {
        private val client = KMongo.createClient().coroutine
        private val database = client.getDatabase("@localhost")
        private val imageHashCollection = database.getCollection<ImageHash>()

        fun saveImageHash(imageUrl: String, imageHash: String) {
            runBlocking {
                imageHashCollection.insertOne(ImageHash(imageUrl, imageHash))
            }
        }

        fun getImageHash(imageUrl: String): String? {
            val entry: ImageHash?
            runBlocking {
                entry = imageHashCollection.findOne(ImageHash::imageUrl eq imageUrl)
            }
            return entry?.imageHash
        }
    }
}

suspend fun HttpClient.hash(url: String): String? {
    val hashFromDatabase = ImageHasher.getImageHash(url)
    hashFromDatabase?.run {
        return this
    }
    lateinit var response: HttpResponse
    try {
        response = request(url)
        if (response.status != HttpStatusCode.OK) {
            return null
        }
    } catch (e: Exception) {
        return null
    }
    val digest = MessageDigest.getInstance("SHA-256")
    val buffer = ByteArray(1024)
    var currentRead = response.content.readAvailable(buffer)
    while (currentRead != -1) {
        digest.update(buffer, 0, currentRead)
        currentRead = response.content.readAvailable(buffer)
    }
    val hash = digest.digest()
    val hashAsString = String(hash, StandardCharsets.UTF_8)
    ImageHasher.saveImageHash(url, hashAsString)
    return hashAsString
}