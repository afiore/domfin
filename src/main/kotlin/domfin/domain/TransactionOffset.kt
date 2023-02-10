package domfin.domain

import java.time.LocalDate

data class TransactionOffset(
    val lastDate: LocalDate,
    val latestBookedTransactionId: String?,
    val latestPendingTransactionId: String?
)