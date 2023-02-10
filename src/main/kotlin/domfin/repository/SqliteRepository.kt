package domfin.repository

import domfin.domain.TransactionOffset
import domfin.nordigen.Credit
import domfin.nordigen.Debit
import domfin.nordigen.Transaction
import domfin.repository.tables.TransactionOffsets
import domfin.repository.tables.Transactions
import domfin.serde.LocalDateSerializer
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object SqliteRepository : TransactionsRepository(), TransactionOffsetRepository {
    private val T = Transactions
    private val TO = TransactionOffsets
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    override fun setLastOffset(accountId: String, transactionOffset: TransactionOffset) {
        TO.deleteWhere { TO.accountId eq accountId }
        TO.insert {
            it[TO.accountId] = accountId
            it[TO.lastDate] = transactionOffset.lastDate.format(dateFormatter)
            it[TO.lastPendingTransactionId] = transactionOffset.latestPendingTransactionId
            it[TO.lastBookedTransactionId] = transactionOffset.latestBookedTransactionId
        }
    }

    override fun getLastOffset(accountId: String): TransactionOffset? =
        TO.slice(TO.lastDate, TO.lastBookedTransactionId, TO.lastPendingTransactionId)
            .select { TO.accountId eq accountId }
            .map {
                TransactionOffset(
                    LocalDate.parse(it[TO.lastDate], dateFormatter),
                    it[TO.lastBookedTransactionId],
                    it[TO.lastPendingTransactionId]
                )
            }.singleOrNull()

    override fun insertAll(accountId: String, transactions: Iterable<Transaction>, isBooked: Boolean) {
        T.batchInsert(transactions, shouldReturnGeneratedValues = false) { t ->
            this[T.accountId] = accountId
            this[T.transactionId] = t.transactionId
            this[T.status] = if (isBooked) "booked" else "pending"
            this[T.bookingDate] = t.bookingDate.format(LocalDateSerializer.format)
            this[T.valueDate] = t.valueDate.format(LocalDateSerializer.format)
            this[T.amount] = (t.transactionAmount.amount * 100).toInt()
            this[T.currency] = t.transactionAmount.currency
            this[T.remittanceInformation] = t.remittanceInformationUnstructured
            this[T.bankTransactionCode] = t.bankTransactionCode
            this[T.internalTransactionId] = t.internalTransactionId

            when (t) {
                is Debit -> {
                    this[T.type] = "debit"
                    this[T.creditorName] = t.creditorName
                }

                is Credit -> {
                    this[T.type] = "credit"
                    this[T.debtorName] = t.debtorName
                    this[T.debtorAccount] = t.debtorAccount?.iban
                }
            }


        }
    }
}