package domfin.repository

import domfin.domain.*
import domfin.nordigen.Credit
import domfin.nordigen.Debit
import domfin.nordigen.TransactionAmount
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.sqlite.SQLiteErrorCode
import org.sqlite.SQLiteException
import java.time.LocalDate
import kotlin.test.assertEquals

class SqliteRepositoryTest {
    val accountId = "account-x"
    val otherAccountId = "account-y"

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
        val transactions = with(Fixtures) {
            listOf(
                debitTransaction("t1", "PREFIX_2_SHOP"),
                debitTransaction("t2", "OTHER"),
                debitTransaction("t3", "OTHER"),
                creditTransaction("t4"),
                creditTransaction("t5"),
                debitTransaction("t6", "PREFIX_1_BAKERY"),
                debitTransaction("t7", "PREFIX_3_BAR"),
            )
        }

        val categoryId = CategoryId("SOME_CATEGORY")

        withDb {
            executeUpdates(
                Queries.insertCategories,
                Queries.insertCategorisationRules,
            )

            with(SqliteRepository) {
                val rule = getAllCategorisationRules().find { it.category.id == categoryId }!!
                insertAllTransactions(accountId, transactions, isBooked = true)
                applyCategorisationRule(rule)

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

    @Test
    @Disabled
    fun `fails categorising transaction when supplied arguments violate relational integrity constraints`() {
        val unknownAccountId = CategoriseTransactionsTestData(
            label = "Invalid accountId",
            accountId = "unknown-account",
            categoryId = Fixtures.Category2.id,
            transactionIds = listOf(Fixtures.debitTransaction2, Fixtures.debitTransaction3).map { it.transactionId },
            expectedRowsAffected = 0u,
        )
        val unknownCategoryId = unknownAccountId.copy(
            label = "Unknown category id",
            categoryId = CategoryId("unknown"),
        )
        val unknownTransactionId = unknownCategoryId.copy(
            label = "Unknown transaction id",
            transactionIds = listOf("unknown-transaction")
        )

        listOf(unknownAccountId, unknownCategoryId, unknownTransactionId).forEach {
            withDb {
                with(Fixtures) {
                    val error = runCatching {
                        SqliteRepository.categoriseTransactions(
                            it.accountId,
                            it.transactionIds,
                            it.categoryId
                        )
                    }.exceptionOrNull() as SQLiteException

                    assertEquals(SQLiteErrorCode.SQLITE_CONSTRAINT, error.resultCode)
                }
            }
        }

    }

    @Test
    fun `categorises transactions with an explicitly supplied category id`() {
        val uncategorised = CategoriseTransactionsTestData(
            label = "No previous categorisation",
            accountId = accountId,
            categoryId = Fixtures.Category2.id,
            transactionIds = listOf(Fixtures.debitTransaction2, Fixtures.debitTransaction3).map { it.transactionId },
            expectedRowsAffected = 2u,
        )
        val preCategorised = uncategorised.copy(
            label = "Previously categorised",
            previousCategorisation = mapOf(
                Fixtures.Category1.id to setOf(Fixtures.debitTransaction2.transactionId)
            )
        )
        val withDuplicates = uncategorised.copy(
            label = "With duplicates",
            transactionIds = listOf(
                Fixtures.debitTransaction2,
                Fixtures.debitTransaction3,
                Fixtures.debitTransaction3
            ).map { it.transactionId },
        )


        listOf(uncategorised, preCategorised, withDuplicates).forEach {
            val (label, accountId, categoryId, transactionIds, expectedRowsAffected, previousCategorisation) = it

            withDb {
                with(Fixtures) {
                    executeUpdates(Queries.insertCategories)

                    val debitTransactions = listOf(
                        debitTransaction4,
                        debitTransaction2,
                        debitTransaction3,
                        debitTransaction1,
                    )

                    SqliteRepository.insertAllTransactions(accountId, debitTransactions, isBooked = true)

                    if (previousCategorisation.isNotEmpty()) {
                        val sql = Queries.insertTransactionCategories(accountId, previousCategorisation)
                        executeUpdates(sql)
                    }

                    val affectedRows = SqliteRepository.categoriseTransactions(
                        accountId,
                        transactionIds, categoryId
                    )
                    assertEquals(expectedRowsAffected, affectedRows, label)

                    val expensesMatchingCategory = SqliteRepository.getCategorisedExpenses(
                        setOf(accountId),
                        setOf(categoryId),
                        CategorisationFilter.Selected
                    )

                    assertEquals(
                        transactionIds.toSet(),
                        expensesMatchingCategory.map { it.transactionId }.toSet(),
                        label
                    )
                }
            }
        }

    }


    @Test
    fun `fetches categorised expenses`() {
        withDb {
            executeUpdates(Queries.insertCategories)

            with(SqliteRepository) {
                val debitTransactions = listOf(
                    Fixtures.debitTransaction4,
                    Fixtures.debitTransaction2,
                    Fixtures.debitTransaction3,
                    Fixtures.debitTransaction1,
                )
                val otherDebitTransactions = listOf(
                    Fixtures.debitTransaction5
                )

                insertAllTransactions(accountId, debitTransactions, isBooked = true)
                insertAllTransactions(otherAccountId, otherDebitTransactions, isBooked = true)

                executeUpdates(
                    """
                    INSERT INTO transaction_categories (account_id, transaction_id, category_id) VALUES 
                    ('$accountId', 't1', '${Fixtures.Category1.id.value}'),
                    ('$accountId', 't2', '${Fixtures.Category1.id.value}'),
                    ('$accountId', 't3', '${Fixtures.Category2.id.value}')
                """.trimIndent()
                )

                val expectedAmount = Amount(15.0, "EUR")
                // No filters supplied

                val allExpenses = getCategorisedExpenses(categorisationFilter = CategorisationFilter.All)
                assertEquals(
                    listOf(
                        Expense(accountId, "t1", LocalDate.now(), expectedAmount, "NOODLES TEMPLE", Fixtures.Category1),
                        Expense(otherAccountId, "t5", LocalDate.now(), expectedAmount, "BOOKS & COFFEE", null),
                        Expense(
                            accountId,
                            "t2",
                            LocalDate.now().minusDays(4),
                            expectedAmount,
                            "PASTA LAND",
                            Fixtures.Category1
                        ),
                        Expense(
                            accountId,
                            "t3",
                            LocalDate.now().minusDays(4),
                            expectedAmount,
                            "METRO TICKET",
                            Fixtures.Category2
                        ),
                        Expense(
                            accountId,
                            "t4",
                            LocalDate.now().minusDays(5),
                            expectedAmount,
                            "PASTA LAND",
                            null
                        )
                    ),
                    allExpenses
                )

                // offset/limit

                val limitAndOffset = LimitAndOffset(3u)


                assertEquals(
                    listOf("t1", "t5", "t2"),
                    getCategorisedExpenses(limitAndOffset = limitAndOffset).map { it.transactionId })
                assertEquals(
                    listOf("t3", "t4"),
                    getCategorisedExpenses(limitAndOffset = limitAndOffset.next(3u)!!).map { it.transactionId })

                //filter by accountIds

                val expensesInOtherAccount = getCategorisedExpenses(
                    accountIds = setOf(otherAccountId),
                    categorisationFilter = CategorisationFilter.Selected
                )
                assertEquals(
                    listOf(
                        Expense(otherAccountId, "t5", LocalDate.now(), expectedAmount, "BOOKS & COFFEE", null),
                    ), expensesInOtherAccount
                )

                //filter by account ids and categories
                val categorisedExpenses = getCategorisedExpenses(
                    accountIds = setOf(accountId),
                    categoryIds = setOf(Fixtures.Category2.id),
                    categorisationFilter = CategorisationFilter.Selected
                )
                assertEquals(
                    listOf(
                        Expense(
                            accountId,
                            "t3",
                            LocalDate.now().minusDays(4),
                            expectedAmount,
                            "METRO TICKET",
                            Fixtures.Category2
                        ),
                    ), categorisedExpenses
                )

                //filter uncategorised only
                val uncategorisedExpenses = getCategorisedExpenses(
                    accountIds = setOf(accountId),
                    categorisationFilter = CategorisationFilter.Uncategorised
                )
                assertEquals(
                    listOf(
                        Expense(
                            accountId,
                            "t4",
                            LocalDate.now().minusDays(5),
                            expectedAmount,
                            "PASTA LAND",
                            null
                        )
                    ), uncategorisedExpenses
                )

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


    private data class CategoriseTransactionsTestData(
        val label: String,
        val accountId: AccountId,
        val categoryId: CategoryId,
        val transactionIds: Iterable<String>,
        val expectedRowsAffected: AffectedRows,
        val previousCategorisation: Map<CategoryId, Set<String>> = mapOf()
    )


    object Fixtures {

        val Category1 = Category(CategoryId("SOME_CATEGORY"), "Some category")
        val Category2 = Category(CategoryId("SOME_OTHER_CATEGORY"), "Some other category")


        val debitTransaction5 = debitTransaction("t5", "BOOKS & COFFEE")
        val debitTransaction4 = debitTransaction("t4", "PASTA LAND", LocalDate.now().minusDays(5))
        val debitTransaction2 = debitTransaction("t2", "PASTA LAND", LocalDate.now().minusDays(4))
        val debitTransaction3 = debitTransaction("t3", "METRO TICKET", LocalDate.now().minusDays(4))
        val debitTransaction1 = debitTransaction("t1", "NOODLES TEMPLE")

        internal fun creditTransaction(id: String): domfin.nordigen.Transaction =
            Credit(id, "some-debitor", null, TransactionAmount("USD", 2000.0), "", LocalDate.now(), LocalDate.now())

        internal fun debitTransaction(
            id: String,
            creditorName: String,
            date: LocalDate = LocalDate.now()
        ): domfin.nordigen.Transaction =
            Debit(id, creditorName, TransactionAmount("EUR", 15.0), "", date, date)

    }

    object Queries {
        fun insertTransactionCategories(
            accountId: AccountId,
            transactionCategorisation: Map<CategoryId, Set<String>>
        ): String {
            val b = StringBuilder()
            b.append("INSERT INTO transaction_categories (account_id, transaction_id, category_id) VALUES ")
            for ((categoryId, transactionIds) in transactionCategorisation) {
                for (tId in transactionIds) {
                    b.append(
                        listOf(accountId, tId, categoryId.value).joinToString(
                            prefix = "(",
                            transform = { "'${it}'" },
                            postfix = ")"
                        )
                    )
                }
            }
            return b.toString()
        }

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


}
