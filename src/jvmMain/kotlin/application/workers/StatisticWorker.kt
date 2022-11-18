package application.workers

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import application.model.ImageModel
import application.model.WordOccurrenceModel
import model.StatisticRequest
import model.StatisticResponse

object StatisticWorker {
    private val webClient = HttpClient(Apache)

    private lateinit var document: Document
    private var visitedLinks = mutableMapOf<String, Int>()
    private var imageList = mutableSetOf<ImageModel>()
    private var wordList = HashMap<String, WordOccurrenceModel>()

    private suspend fun processStatistic(url: String, level: Int, maxLevel: Int) {
        if (visitedLinks.getOrDefault(url, 0) < level) {
            visitedLinks[url] = level
        } else {
            return
        }

        try {
            withContext(Dispatchers.IO) {
                document = Jsoup.connect(url).followRedirects(true).get()
            }
        } catch (e: Exception) {

        }

        val imageUrls = document.getElementsByTag("img").map { it.attr("src") }
        val ordinaryUrls = document.getElementsByTag("a").map { it.attr("href") }
        val words = document.text()
            .split("\\s".toRegex())
            .map { it.lowercase() }
            .filter { it.length >= 5 && it.all(Char::isLetter) }

        for (imageUrl in imageUrls) {
            val hash = webClient.hash(imageUrl)
            hash?.run {
                imageList.add(ImageModel(imageUrl, this));
            }
        }

        for (word in words) {
            wordList[word] = wordList.getOrDefault(word, WordOccurrenceModel(level, mutableMapOf())).occur(level)
        }

        if (level < maxLevel) {
            for (link in ordinaryUrls) {
                processStatistic(link, level + 1, maxLevel)
            }
        }
    }

    suspend fun processAll(request: StatisticRequest): StatisticResponse {
        imageList = mutableSetOf()
        wordList = HashMap()
        visitedLinks = mutableMapOf()

        withContext(Dispatchers.Default) {
            supervisorScope {
                launch {
                    processStatistic(request.url, 1, request.level)
                }
            }
        }

        val response = StatisticResponse(
            wordList
                .mapValues { it.value.toWeight() }
                .toList()
                .sortedByDescending { it.second }
                .take(5)
                .map { it.first },
            imageList.map { it.url }.toSet()
        )

        return response
    }
}