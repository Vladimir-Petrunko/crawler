package model

import kotlinx.serialization.Serializable

@Serializable
data class StatisticResponse(
    val topWords: List<String>,
    val images: Set<String>
)
