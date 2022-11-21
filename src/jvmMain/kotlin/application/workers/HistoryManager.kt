package application.workers

import kotlinx.coroutines.runBlocking
import org.litote.kmongo.coroutine.*
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import model.Statistic
import model.StatisticRequest
import model.StatisticResponse
import org.litote.kmongo.div

class HistoryManager {

    data class UserStatistic(
        val username: String,
        val statistic: Statistic
    )

    companion object {
        private val client = KMongo.createClient().coroutine
        private val database = client.getDatabase("@localhost")
        private val statisticCollection = database.getCollection<UserStatistic>()

        fun saveStatistics(statisticRequest: StatisticRequest, statisticResponse: StatisticResponse) =
            runBlocking {
                statisticCollection.deleteOne(
                    UserStatistic::username eq AuthenticationManager.getCurrentUser(),
                    UserStatistic::statistic / Statistic::statisticRequest eq statisticRequest
                )
                statisticCollection.insertOne(
                    UserStatistic(
                        AuthenticationManager.getCurrentUser(),
                        Statistic(statisticRequest, statisticResponse)
                    )
                )
            }

        fun getStatistics(statisticRequest: StatisticRequest): StatisticResponse? =
            runBlocking {
                statisticCollection.findOne(
                    UserStatistic::username eq AuthenticationManager.getCurrentUser(),
                    UserStatistic::statistic / Statistic::statisticRequest eq statisticRequest
                )
            }?.statistic?.statisticResponse

        fun getHistory(start: Int, count: Int): List<Statistic> =
            runBlocking {
                statisticCollection
                    .find(UserStatistic::username eq AuthenticationManager.getCurrentUser())
                    .skip(start)
                    .limit(count)
                    .toList()
                    .map { it.statistic }
            }

        fun totalCount(): Int =
            runBlocking {
                statisticCollection
                    .countDocuments(UserStatistic::username eq AuthenticationManager.getCurrentUser())
                    .toInt()
            }
    }
}