package model

import kotlinx.serialization.Serializable

@Serializable
data class StatisticRequest(
    val url: String,
    val level: Int
)
