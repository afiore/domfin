package domfin.repository

import domfin.domain.*
import domfin.nordigen.Credit
import domfin.nordigen.Debit
import domfin.nordigen.Transaction
import domfin.repository.tables.*
import domfin.serde.Defaults
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object SqliteRepository : TransactionOffsetRepository, CategorisedTransactionsRepository, CategorisationRuleRepository {
    private val T = Transactions
    private val C = Categories
    private val TO = TransactionOffsets
    private val TC = TransactionCategories
    private val CR = CategorisationRules
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    private const val TransactionAmountMultiplier = 100

    override fun getCategorisedExpenses(
        accountIds: Set<AccountId>,
        categoryIds: Set<CategoryId>,
        categorisationFilter: CategorisationFilter,
        limitAndOffset: LimitAndOffset,
    ): List<Expense> {
        fun inNelOrTrue(column: Column<String>, values: Iterable<String>) =
            if (values.count() == 0)
                booleanParam(true)
            else
                column inList values

        val categoryIdClause = when (categorisationFilter) {
            CategorisationFilter.All -> booleanParam(true)
            CategorisationFilter.Selected -> inNelOrTrue(TC.categoryId, categoryIds.map { it.value })
            CategorisationFilter.Uncategorised -> TC.categoryId.isNull()
        }

        return T.join(TC, JoinType.LEFT, onColumn = TC.transactionId, otherColumn = T.transactionId)
            .join(C, JoinType.LEFT, onColumn = TC.categoryId, otherColumn = C.id)
            .slice(
                T.accountId,
                T.transactionId,
                T.valueDate,
                T.amount,
                T.currency,
                T.creditorName,
                TC.categoryId,
                C.label
            )
            .select(
                inNelOrTrue(T.accountId, accountIds)
                    .and(categoryIdClause)
                    .and(T.status eq T.BookedStatus)
                    .and(T.type eq T.DebitType)
            )
            .orderBy(
                Pair(T.valueDate, SortOrder.DESC),
                Pair(T.accountId, SortOrder.ASC),
                Pair(T.transactionId, SortOrder.ASC),
            )
            .limit(limitAndOffset.limit.toInt(), limitAndOffset.offset.toLong())
            .map {
                val number = (it[T.amount] / TransactionAmountMultiplier.toDouble())
                val amount = Amount(number, it[T.currency])
                val category = it.getOrNull(TC.categoryId)?.let { id ->
                    Category(CategoryId(id), it[C.label])
                }
                Expense(
                    it[T.accountId],
                    it[T.transactionId],
                    LocalDate.parse(it[T.valueDate]),
                    amount,
                    it[T.creditorName]!!,
                    category
                )
            }
    }

    override fun categoriseTransactions(
        accountId: AccountId,
        transactionIds: Iterable<String>,
        categoryId: CategoryId
    ): AffectedRows {

        TC.deleteWhere {
            (TC.accountId eq accountId)
                .and(TC.transactionId inList transactionIds)
        }

        return TC.insert(
            T.slice(stringLiteral(accountId), T.transactionId, stringLiteral(categoryId.value))
                .select {
                    (T.transactionId inList transactionIds)
                        .and(T.accountId eq stringLiteral(accountId))
                },
            columns = listOf(TC.accountId, TC.transactionId, TC.categoryId)
        )?.toUInt() ?: 0u
    }

    override fun applyCategorisationRule(categorisationRule: CategorisationRule) {
        val categoryId = categorisationRule.category.id.value
        TC.insert(
            T.slice(
                T.accountId,
                T.transactionId,
                stringParam(categoryId)
            ).select {
                val creditorNameMatchesRule =
                    categorisationRule.substrings.fold(booleanParam(false)) { expression, substring ->
                        expression or (T.creditorName like "$substring%")
                    }
                (T.transactionId notInSubQuery (TC.slice(TC.transactionId)
                    .select(
                        TC.categoryId eq categoryId
                    ))).and(
                    (T.type eq stringParam(T.DebitType))
                        .and(T.status eq stringParam(T.BookedStatus))
                        .and(creditorNameMatchesRule)
                )
            })
    }


    override fun getCategory(id: CategoryId): Category? =
        C.select { C.id eq id.value }.map {
            Category(CategoryId(it[C.id]), it[C.label])
        }.firstOrNull()

    override fun setCategorisationRule(categoryId: CategoryId, substrings: Set<String>) {
        CR.deleteWhere { CR.categoryId eq categoryId.value }
        CR.batchInsert(substrings) { substring ->
            this[CR.categoryId] = categoryId.value
            this[CR.substring] = substring
        }
    }

    override fun getAllCategorisationRules(): List<CategorisationRule> =
        CR.join(C, JoinType.INNER, onColumn = CR.categoryId, otherColumn = C.id).selectAll().map {
            Triple(it[CR.categoryId], it[C.label], it[CR.substring])
        }.groupBy {
            it.first
        }.map {
            val id = CategoryId(it.key)
            val label = it.value.map { it.second }.first()
            val substrings = it.value.map { it.third }.toSet()

            CategorisationRule(
                Category(id, label),
                substrings
            )
        }

    override fun setLastOffset(accountId: AccountId, transactionOffset: TransactionOffset) {
        TO.deleteWhere { TO.accountId eq accountId }
        TO.insert {
            it[TO.accountId] = accountId
            it[TO.lastDate] = transactionOffset.lastDate.format(dateFormatter)
            it[TO.lastPendingTransactionId] = transactionOffset.latestPendingTransactionId
            it[TO.lastBookedTransactionId] = transactionOffset.latestBookedTransactionId
        }
    }

    override fun getLastOffset(accountId: AccountId): TransactionOffset? =
        TO.slice(TO.lastDate, TO.lastBookedTransactionId, TO.lastPendingTransactionId)
            .select { TO.accountId eq accountId }
            .map {
                TransactionOffset(
                    LocalDate.parse(it[TO.lastDate], dateFormatter),
                    it[TO.lastBookedTransactionId],
                    it[TO.lastPendingTransactionId]
                )
            }.singleOrNull()

    override fun insertAllTransactions(accountId: AccountId, transactions: Iterable<Transaction>, isBooked: Boolean) {
        T.batchInsert(transactions, shouldReturnGeneratedValues = false) { t ->
            this[T.accountId] = accountId
            this[T.transactionId] = t.transactionId
            this[T.status] = if (isBooked) "booked" else "pending"
            this[T.bookingDate] = t.bookingDate.format(Defaults.DateFormat)
            this[T.valueDate] = t.valueDate.format(Defaults.DateFormat)
            this[T.amount] = (t.transactionAmount.amount * TransactionAmountMultiplier).toLong()
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
