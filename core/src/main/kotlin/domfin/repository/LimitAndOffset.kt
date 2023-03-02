package domfin.repository

data class LimitAndOffset(val limit: UInt, val offset: ULong = DefaultOffset) {

    fun nextOffset(resultsInCurrentPage: UInt): ULong? =
        if (resultsInCurrentPage < limit)
            null
        else
            offset + limit


    fun next(resultsInCurrentPage: UInt): LimitAndOffset? =
        nextOffset(resultsInCurrentPage)?.let {
            LimitAndOffset(limit, it)
        }

    companion object {
        private const val DefaultLimit: UInt = 500u
        private const val DefaultOffset: ULong = 0u


        val First: LimitAndOffset = LimitAndOffset(1u, DefaultOffset)
        val Default: LimitAndOffset = LimitAndOffset(DefaultLimit, DefaultOffset)
    }

}
