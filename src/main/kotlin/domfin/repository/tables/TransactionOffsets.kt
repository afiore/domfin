package domfin.repository.tables

import org.jetbrains.exposed.sql.Table

object TransactionOffsets : Table(name = "transaction_offsets") {
    val accountId = text("account_id")
    val lastDate = text("last_date")
    val lastBookedTransactionId = text("last_booked_transaction_id").nullable()
    val lastPendingTransactionId = text("last_pending_transaction_id").nullable()

    override val primaryKey = PrimaryKey(accountId, lastDate, lastBookedTransactionId, lastPendingTransactionId)
}