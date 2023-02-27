package domfin.repository

data class LimitAndOffset(val limit: Int, val offset: Long = 0) {
    companion object {
        const val DefaultLimit: Int = 500
        const val DefaultOffset: Long = 0

        val Default: LimitAndOffset = LimitAndOffset(DefaultLimit, DefaultOffset)
    }
}
