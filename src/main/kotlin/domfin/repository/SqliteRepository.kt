package domfin.repository

import domfin.domain.CategorisationRule
import domfin.domain.CategoryId
import domfin.domain.TransactionOffset
import domfin.nordigen.Credit
import domfin.nordigen.Debit
import domfin.nordigen.Transaction
import domfin.repository.tables.CategorisationRules
import domfin.repository.tables.TransactionCategories
import domfin.repository.tables.TransactionOffsets
import domfin.repository.tables.Transactions
import domfin.serde.LocalDateSerializer
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object SqliteRepository : TransactionRepository, TransactionOffsetRepository, CategorisationRuleRepository,
    TransactionCategoryRepository {
    private val T = Transactions
    private val TO = TransactionOffsets
    private val TC = TransactionCategories
    private val CR = CategorisationRules
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    //TODO: please unit test this query!
    override fun categoriseTransactions(rule: CategorisationRule) {
        TC.insert(
            T.slice(
                T.accountId,
                T.transactionId,
                stringParam(rule.categoryId.value)
            ).select {
                val creditorNameMatchesRule = rule.substrings.fold(booleanParam(false)) { expression, substring ->
                    expression or (T.creditorName like "$substring%")
                }
                (T.transactionId notInSubQuery (TC.slice(TC.transactionId)
                    .select(
                        TC.categoryId eq rule.categoryId.value
                    ))).and(
                    (T.type eq stringParam(T.DebitType))
                        .and(T.status eq stringParam(T.BookedStatus))
                        .and(creditorNameMatchesRule)
                )
            })
    }

    override fun getAllCategorisationRules(): List<CategorisationRule> =
        CR.selectAll().map {
            Pair(it[CR.categoryId], it[CR.substring])
        }.groupBy {
            it.first
        }.map { CategorisationRule(CategoryId(it.key), it.value.map { it.second }.toSet()) }

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

    override fun insertAllTransactions(accountId: String, transactions: Iterable<Transaction>, isBooked: Boolean) {
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