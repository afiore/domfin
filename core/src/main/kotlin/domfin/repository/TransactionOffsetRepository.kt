package domfin.repository

import domfin.domain.TransactionOffset

interface TransactionOffsetRepository {
    fun setLastOffset(accountId: String, transactionOffset: TransactionOffset)
    fun getLastOffset(accountId: String): TransactionOffset?
}
