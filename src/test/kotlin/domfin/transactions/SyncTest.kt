package domfin.transactions

import domfin.domain.TransactionOffset
import domfin.nordigen.*
import domfin.nordigen.client.GetAllRequistions
import domfin.nordigen.client.GetTransactionsApi
import domfin.repository.TransactionOffsetRepository
import domfin.repository.TransactionsRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource
import java.time.LocalDate
import javax.sql.DataSource
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
            dateFrom?.let { dateFrom ->
                transactions.filter { it.valueDate >= dateFrom }
            } ?: transactions


    }

    class StubRepo() : TransactionsRepository(), TransactionOffsetRepository {
        private val booked: MutableMap<String, List<Transaction>> = mutableMapOf()
        private val pending: MutableMap<String, List<Transaction>> = mutableMapOf()
        private val offsets: MutableMap<String, TransactionOffset> = mutableMapOf()

        fun getBookedTransactions(accountId: String): List<Transaction> =
            booked[accountId].orEmpty()

        override fun setLastOffset(accountId: String, transactionOffset: TransactionOffset) {
            offsets[accountId] = transactionOffset
        }

        override fun getLastOffset(accountId: String): TransactionOffset? =
            offsets[accountId]

        override fun insertAll(accountId: String, transactions: Iterable<Transaction>, isBooked: Boolean) {
            val store = if (isBooked) booked else pending
            blowUpUnlessAllUnique(transactions, store[accountId].orEmpty())
            store[accountId] = transactions.toList()
        }

        private fun blowUpUnlessAllUnique(
            transactions: Iterable<Transaction>,
            persisted: List<Transaction>
        ) {
            val existingTxIds = persisted.map { it.transactionId }.toSet()
                .intersect(transactions.map { it.transactionId })
            if (!existingTxIds.isEmpty())
                throw IllegalStateException("Unique constraint violation for transaction ids: $existingTxIds")
        }

    }

    val inMemoryDataSource: DataSource by lazy {
        val config = SQLiteConfig()
        val dataSource = SQLiteDataSource(config)
        dataSource.url = "jdbc:sqlite:memory"
        dataSource
    }

    @Test
    fun `sync all transactions when stored offset is null`() {
        val accountId = "account-id"
        val requisition = Requisition("req-id", listOf(accountId), "LN")

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

        val allBooked = mapOf(
            accountId to listOf(t2, t1)
        )
        val api = StubApi(listOf(requisition), allBooked, mapOf())
        val repo = StubRepo()
        val sync = Sync(api, repo, inMemoryDataSource)

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
    fun `sync only new transactions when offset is present for both pending and booked`() {

    }
}