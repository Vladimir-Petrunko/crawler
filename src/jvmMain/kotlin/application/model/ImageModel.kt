package application.model

data class ImageModel(
    val url: String,
    val hash: String
) {

    override fun equals(other: Any?): Boolean {
        return other is ImageModel && hash == other.hash
    }

    override fun hashCode() = hash.hashCode()

}