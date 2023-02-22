package domfin.nordigen

import domfin.domain.TransactionOffset
import kotlinx.serialization.Serializable

@Serializable
data class TransactionsByStatus(val booked: List<Transaction>, val pending: List<Transaction>) {

    fun since(offset: TransactionOffset): TransactionsByStatus {
        val (_, latestBookedTid, latestPendingTid) = offset

        val bookedSlice = latestBookedTid?.let { id ->
            booked.takeWhile { it.transactionId != id }
        } ?: booked

        val pendingSlice = latestPendingTid?.let { id ->
            pending.takeWhile { it.transactionId != id }
        } ?: pending

        return TransactionsByStatus(bookedSlice, pendingSlice)
    }

    val count: Int by lazy {
        booked.size + pending.size
    }

    val latestOffset: TransactionOffset? by lazy {
        val latestBooked = booked.firstOrNull()
        val latestPending = pending.firstOrNull()

        val lastDate = listOf(latestBooked, latestPending).filterNotNull().map { it.valueDate }.maxOrNull()

        lastDate?.let {
            TransactionOffset(it, latestBooked?.transactionId, latestPending?.transactionId)
        }
    }
}
