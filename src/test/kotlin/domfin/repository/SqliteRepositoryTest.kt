package domfin.repository

import domfin.domain.CategorisationRule
import domfin.domain.CategoryId
import domfin.nordigen.Credit
import domfin.nordigen.Debit
import domfin.nordigen.TransactionAmount
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.time.LocalDate
import kotlin.test.assertEquals

class SqliteRepositoryTest {

    private val logger = KotlinLogging.logger("SqliteRepositoryTest")

    @Test
    fun `returns all the defined categorisation rules`() {
        withDb {
            executeUpdates(
                Queries.insertCategories,
                Queries.insertCategorisationRules,
            )

            val allCategorisationRules = SqliteRepository.getAllCategorisationRules()
            assertEquals(
                listOf(
                    CategorisationRule(
                        CategoryId("SOME_CATEGORY"),
                        setOf("PREFIX_1", "PREFIX_2")
                    ),
                    CategorisationRule(
                        CategoryId("SOME_OTHER_CATEGORY"),
                        setOf("PREFIX_3")
                    ),
                ),
                allCategorisationRules
            )

        }
    }


    @Test
    fun `categorises transactions according to the defined rules`() {
        val accountId = "account-x"
        val transactions = listOf(
            debitTransaction("t1", "PREFIX_2_SHOP"),
            debitTransaction("t2", "OTHER"),
            debitTransaction("t3", "OTHER"),
            creditTransaction("t4"),
            creditTransaction("t5"),
            debitTransaction("t6", "PREFIX_1_BAKERY"),
            debitTransaction("t7", "PREFIX_3_BAR"),
        )

        val categoryId = CategoryId("SOME_CATEGORY")

        withDb {
            executeUpdates(
                Queries.insertCategories,
                Queries.insertCategorisationRules,
            )

            with(SqliteRepository) {
                val rule = getAllCategorisationRules().find { it.categoryId == categoryId }!!
                insertAllTransactions(accountId, transactions, isBooked = true)
                categoriseTransactions(rule)

                val transactionIds = mutableListOf<String>()
                exec(
                    "SELECT transaction_id from transaction_categories WHERE category_id = '${categoryId.value}'",
                ) { rs ->
                    while (rs.next()) {
                        transactionIds.add(rs.getString("transaction_id"))
                    }
                }

                assertEquals(listOf("t1", "t6"), transactionIds.toList())

            }
        }

    }


    private fun Transaction.executeUpdates(vararg sqls: String) {
        sqls.forEach {
            val stmt = connection.prepareStatement(it, returnKeys = false)
            stmt.executeUpdate()
        }
    }


    private fun <T> withDb(stmt: Transaction.() -> T) {
        runBlocking {
            val dataSource = SQLDataSource.tmpFile()
            val migrator = SqlMigrator(dataSource, includeSeedData = false)
            val db = Database.connect(dataSource)
            TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
            transaction(db) {
                runBlocking {
                    migrator()
                }
                stmt()
            }
        }
    }

    object Queries {
        val insertCategories =
            """INSERT INTO categories (id, label) VALUES
                         ('SOME_CATEGORY', 'Some category'),
                         ('SOME_OTHER_CATEGORY', 'Some other...')""".trimIndent()
        val insertCategorisationRules =
            """INSERT INTO categorisation_rules (category_id, substring) VALUES
                         ('SOME_CATEGORY', 'PREFIX_1'),
                         ('SOME_CATEGORY', 'PREFIX_2'),
                         ('SOME_OTHER_CATEGORY', 'PREFIX_3')""".trimIndent()
    }


    private fun creditTransaction(id: String): domfin.nordigen.Transaction =
        Credit(id, "some-debitor", null, TransactionAmount("USD", 2000.0), "", LocalDate.now(), LocalDate.now())

    private fun debitTransaction(id: String, creditorName: String): domfin.nordigen.Transaction =
        Debit(id, creditorName, TransactionAmount("EUR", 15.0), "", LocalDate.now(), LocalDate.now())
}