package application.model


data class WordOccurrenceModel(val lowestLevel: Int, val occurrenceCount: MutableMap<Int, Int>) {
    fun occur(depth: Int): WordOccurrenceModel {
        occurrenceCount[depth] = occurrenceCount.getOrDefault(depth, 0) + 1
        return this
    }

    fun toWeight(): Double {
        var result = 0.0

        for ((depth, count) in occurrenceCount) {
            result += count * depth * (1 - kotlin.math.log(depth.toDouble() + 1, 10.0))
        }

        return result
    }
}