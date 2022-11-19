package application.workers

import kotlinx.coroutines.runBlocking
import org.litote.kmongo.coroutine.*
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import model.Statistic
import model.StatisticRequest
import model.StatisticResponse

class HistoryManager {

    companion object {
        private val client = KMongo.createClient().coroutine
        private val database = client.getDatabase("@localhost")
        private val statisticCollection = database.getCollection<Statistic>()

        fun saveStatistics(statisticRequest: StatisticRequest, statisticResponse: StatisticResponse) =
            runBlocking {
                statisticCollection.insertOne(Statistic(statisticRequest, statisticResponse))
            }

        fun getStatistics(statisticRequest: StatisticRequest): StatisticResponse? =
            runBlocking {
                statisticCollection.findOne(Statistic::statisticRequest eq statisticRequest)
            }?.statisticResponse

        fun getHistory(start: Int, count: Int): List<StatisticRequest> =
            runBlocking {
                statisticCollection
                    .projection(Statistic::statisticRequest)
                    .skip(start)
                    .limit(count)
                    .toList()
            }

        fun totalCount(): Int =
            runBlocking {
                statisticCollection.countDocuments().toInt()
            }
    }
}