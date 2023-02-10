package domfin.transactions

import domfin.nordigen.client.GetAllRequistions
import domfin.nordigen.client.GetTransactionsApi
import domfin.repository.TransactionOffsetRepository
import domfin.repository.TransactionsRepository
import mu.KotlinLogging
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

class Sync<Api, Repo> constructor(
    private val api: Api,
    private val repo: Repo,
    private val dataSource: DataSource,
) where Api : GetTransactionsApi,
        Api : GetAllRequistions,
        Repo : TransactionsRepository,
        Repo : TransactionOffsetRepository {


    private val logger = KotlinLogging.logger {}

    private suspend fun runMigration() {
        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .load()
        flyway.migrate()
    }

    suspend fun runForAllAccounts() {
        val requisitions = api.getAllRequisitions()
        val accountIds = requisitions.flatMap { it.accounts }.distinct()
        val db = Database.connect(dataSource)

        runMigration()

        accountIds.forEach { accountId ->
            val transactionOffsets = transaction(db) {
                repo.getLastOffset(accountId)
            }
            logger.debug { "Current offsets for account id $accountId: $transactionOffsets" }

            val transactions = api.getTransactionsSince(accountId, transactionOffsets)

            logger.info { "Found ${transactions.count} new transaction/s" }

            transaction(db) {
                repo.insertAll(accountId, transactions)
                transactions.latestOffset?.also {
                    repo.setLastOffset(accountId, it)
                }
            }
        }
    }

}