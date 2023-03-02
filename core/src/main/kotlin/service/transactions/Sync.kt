package service.transactions

import domfin.nordigen.client.GetAllRequistions
import domfin.nordigen.client.GetTransactionsApi
import domfin.repository.TransactionOffsetRepository
import domfin.repository.TransactionRepository
import domfin.repository.transact
import mu.KotlinLogging
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

        accountIds.forEach { accountId ->
            val transactionOffsets = dataSource.transact {
                repo.getLastOffset(accountId)
            }
            logger.debug { "Current offsets for account id $accountId: $transactionOffsets" }

            val transactions = api.getTransactionsSince(accountId, transactionOffsets)

            logger.info { "Found ${transactions.count} new transaction/s" }

            dataSource.transact {
                repo.insertAllTransactions(accountId, transactions)
                transactions.latestOffset?.also {
                    repo.setLastOffset(accountId, it)
                }
            }
        }
    }

}
