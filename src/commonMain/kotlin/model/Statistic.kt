package model

import kotlinx.serialization.Serializable

@Serializable
data class Statistic(
    val statisticRequest: StatisticRequest,
    val statisticResponse: StatisticResponse
)