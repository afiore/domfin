package domfin.nordigen.client

import domfin.domain.TransactionOffset
import domfin.nordigen.TransactionResults
import domfin.nordigen.TransactionsByStatus
import java.time.LocalDate

abstract class GetTransactionsApi {
    abstract suspend fun getTransactions(accountId: String, dateFrom: LocalDate?): TransactionResults

    suspend fun getTransactionsSince(accountId: String, offset: TransactionOffset?): TransactionsByStatus =
        with(getTransactions(accountId, offset?.lastDate).transactions) {
            if (offset != null)
                since(offset)
            else
                this
        }
}
