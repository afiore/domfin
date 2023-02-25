package domfin.repository

import domfin.domain.CategorisationRule
import domfin.domain.Category
import domfin.domain.CategoryId
import domfin.nordigen.Credit
import domfin.nordigen.Debit
import domfin.nordigen.TransactionAmount
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class SqliteRepositoryTest {

    private val logger = KotlinLogging.logger("SqliteRepositoryTest")

    @Test
    fun `fetches a single category`() {
        withDb {
            executeUpdates(Queries.insertCategories)
            val category = SqliteRepository.getCategory(Fixtures.Category2.id)
            assertEquals(
                Category(CategoryId("SOME_OTHER_CATEGORY"), "Some other category"),
                category
            )
        }
    }

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
                        Category(CategoryId("SOME_CATEGORY"), "Some category"),
                        setOf("PREFIX_1", "PREFIX_2")
                    ),
                    CategorisationRule(
                        Category(CategoryId("SOME_OTHER_CATEGORY"), "Some other category"),
                        setOf("PREFIX_3")
                    ),
                ),
                allCategorisationRules
            )

        }
    }

    @Test
    fun `sets a categorisation rule`() {
        val substrings = setOf("SUPERMARKET_X", "SUPERMARKET_Y", "SUPERMARKET_Z")
        withDb {
            executeUpdates(Queries.insertCategories)
            SqliteRepository.setCategorisationRule(Fixtures.Category1.id, substrings)
            val rule = SqliteRepository.getAllCategorisationRules().firstOrNull()
            assertEquals(CategorisationRule(Fixtures.Category1, substrings), rule)
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
                val rule = getAllCategorisationRules().find { it.category.id == categoryId }!!
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
            SQLDataSource.fromTmpFile { dataSource ->
                val migrator = SqlMigrator(dataSource, includeSeedData = false)
                runBlocking { migrator() }
                val db = Database.connect(dataSource)
                transaction(db) {
                    stmt()
                }

            }

        }
    }

    object Fixtures {
        val Category1 = Category(CategoryId("SOME_CATEGORY"), "Some category")
        val Category2 = Category(CategoryId("SOME_OTHER_CATEGORY"), "Some other category")
    }

    object Queries {
        val insertCategories =
            """INSERT INTO categories (id, label) VALUES
                         ('${Fixtures.Category1.id.value}', '${Fixtures.Category1.label}'),
                         ('${Fixtures.Category2.id.value}', '${Fixtures.Category2.label}')""".trimIndent()
        val insertCategorisationRules =
            """INSERT INTO categorisation_rules (category_id, substring) VALUES
                         ('${Fixtures.Category1.id.value}', 'PREFIX_1'),
                         ('${Fixtures.Category1.id.value}', 'PREFIX_2'),
                         ('${Fixtures.Category2.id.value}', 'PREFIX_3')""".trimIndent()
    }


    private fun creditTransaction(id: String): domfin.nordigen.Transaction =
        Credit(id, "some-debitor", null, TransactionAmount("USD", 2000.0), "", LocalDate.now(), LocalDate.now())

    private fun debitTransaction(id: String, creditorName: String): domfin.nordigen.Transaction =
        Debit(id, creditorName, TransactionAmount("EUR", 15.0), "", LocalDate.now(), LocalDate.now())
}