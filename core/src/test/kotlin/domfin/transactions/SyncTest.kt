package domfin.transactions

import domfin.domain.CategoryId
import domfin.domain.Expense
import domfin.domain.TransactionOffset
import domfin.nordigen.*
import domfin.nordigen.client.GetAllRequistions
import domfin.nordigen.client.GetTransactionsApi
import domfin.repository.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import service.transactions.Sync
import java.time.LocalDate
import kotlin.test.assertEquals

class SyncTest {
    class StubApi(
        private val requisitions: List<Requisition>,
        private val allBooked: Map<String, List<Transaction>>,
        private val allPending: Map<String, List<Transaction>>,
    ) : GetTransactionsApi(), GetAllRequistions {
        override suspend fun getTransactions(accountId: String, dateFrom: LocalDate?): TransactionResults {
            val pending = transactionsSince(dateFrom, allPending[accountId].orEmpty())
            val booked = transactionsSince(dateFrom, allBooked[accountId].orEmpty())
            return TransactionResults(TransactionsByStatus(booked, pending))
        }

        override suspend fun getAllRequisitions(): List<Requisition> =
            requisitions

        private fun transactionsSince(dateFrom: LocalDate?, transactions: List<Transaction>): List<Transaction> =
            dateFrom?.let { from ->
                transactions.filter { it.valueDate >= from }
            } ?: transactions


    }

    class StubRepo : TransactionRepository, TransactionOffsetRepository {
        private val booked: MutableMap<String, List<Transaction>> = mutableMapOf()
        private val pending: MutableMap<String, List<Transaction>> = mutableMapOf()
        private val offsets: MutableMap<String, TransactionOffset> = mutableMapOf()


        fun getBookedTransactions(accountId: String): List<Transaction> =
            booked[accountId].orEmpty()

        override fun getCategorisedExpenses(
            accountIds: Set<AccountId>,
            categoryIds: Set<CategoryId>,
            categorisationFilter: CategorisationFilter,
            limitAndOffset: LimitAndOffset
        ): List<Expense> {
            TODO("Not yet implemented")
        }

        override fun setLastOffset(accountId: String, transactionOffset: TransactionOffset) {
            offsets[accountId] = transactionOffset
        }

        override fun getLastOffset(accountId: String): TransactionOffset? =
            offsets[accountId]

        override fun insertAllTransactions(accountId: String, transactions: Iterable<Transaction>, isBooked: Boolean) {
            val store = if (isBooked) booked else pending
            blowUpUnlessAllUnique(transactions, store[accountId].orEmpty())
            store[accountId] = transactions.toList() + store[accountId].orEmpty()
        }

        private fun blowUpUnlessAllUnique(
            transactions: Iterable<Transaction>,
            persisted: List<Transaction>
        ) {
            val existingTxIds = persisted.map { it.transactionId }.toSet()
                .intersect(transactions.map { it.transactionId })
            if (!existingTxIds.isEmpty())
                error("Unique constraint violation for transaction ids: $existingTxIds")
        }

    }


    val t1 = Debit(
        "t-1",
        "Spotify",
        TransactionAmount("EUR", 15.30),
        "xyz1",
        LocalDate.of(2022, 1, 1),
        LocalDate.of(2022, 1, 1)
    )
    val t2 = Credit(
        "t-2",
        "ACME corp",
        DebtorAccount("AL35202111090000000001234567"),
        TransactionAmount("EUR", 5000.30),
        "xyz2",
        LocalDate.of(2022, 1, 27),
        LocalDate.of(2022, 1, 28),
        remittanceInformationUnstructured = "salary",
    )
    val t3 = Debit(
        "t-3",
        "Movistar",
        TransactionAmount("EUR", 110.10),
        "xyz1",
        LocalDate.of(2022, 1, 29),
        LocalDate.of(2022, 1, 29)
    )


    @Test
    fun `sync all transactions when stored offset is null`() {
        val accountId = "account-id"
        val requisition = Requisition("req-id", listOf(accountId), "LN")

        val allBooked = mapOf(
            accountId to listOf(t2, t1)
        )

        val api = StubApi(listOf(requisition), allBooked, mapOf())

        val repo = StubRepo()

        val sync = Sync(api, repo, SQLDataSource.mem())

        runBlocking {
            sync.runForAllAccounts()
        }

        assertEquals(
            listOf(t2, t1),
            repo.getBookedTransactions(accountId)
        )

        assertEquals(
            TransactionOffset(LocalDate.of(2022, 1, 28), "t-2", null),
            repo.getLastOffset(accountId)
        )
    }

    @Test
    fun `sync only new transactions when offset is present`() {
        val accountId = "account-id"
        val requisition = Requisition("req-id", listOf(accountId), "LN")

        val allBooked = mapOf(
            accountId to listOf(t3, t2, t1)
        )
        val api = StubApi(listOf(requisition), allBooked, mapOf())

        //simulate a previous run with t2 being the latest booked transaction
        val repo = StubRepo()
        repo.insertAllTransactions(accountId, listOf(t2, t1), isBooked = true)
        repo.setLastOffset(accountId, TransactionOffset(t2.valueDate, t2.transactionId, null))

        val sync = Sync(api, repo, SQLDataSource.mem())

        runBlocking {
            sync.runForAllAccounts()
        }

        assertEquals(
            listOf(t3, t2, t1),
            repo.getBookedTransactions(accountId)
        )

        assertEquals(
            TransactionOffset(t3.valueDate, "t-3", null),
            repo.getLastOffset(accountId)
        )
    }
}
