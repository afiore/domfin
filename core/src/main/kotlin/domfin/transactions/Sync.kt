package domfin.transactions

import domfin.nordigen.client.GetAllRequistions
import domfin.nordigen.client.GetTransactionsApi
import domfin.repository.TransactionOffsetRepository
import domfin.repository.TransactionRepository
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource


class Sync<Api, Repo> constructor(
    private val api: Api,
    private val repo: Repo,
    private val dataSource: DataSource,
) where Api : GetTransactionsApi,
        Api : GetAllRequistions,
        Repo : TransactionRepository,
        Repo : TransactionOffsetRepository {


    private val logger = KotlinLogging.logger {}

    //TODO: move this elsewhere

    suspend fun runForAllAccounts() {
        val requisitions = api.getAllRequisitions()
        val accountIds = requisitions.flatMap { it.accounts }.distinct()
        val db = Database.connect(dataSource)

        accountIds.forEach { accountId ->
            val transactionOffsets = transaction(db) {
                repo.getLastOffset(accountId)
            }
            logger.debug { "Current offsets for account id $accountId: $transactionOffsets" }

            val transactions = api.getTransactionsSince(accountId, transactionOffsets)

            logger.info { "Found ${transactions.count} new transaction/s" }

            transaction(db) {
                repo.insertAllTransactions(accountId, transactions)
                transactions.latestOffset?.also {
                    repo.setLastOffset(accountId, it)
                }
            }
        }
    }

}
