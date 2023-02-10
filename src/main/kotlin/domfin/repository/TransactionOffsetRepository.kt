package domfin.repository

import domfin.domain.TransactionOffset

interface TransactionOffsetRepository {
    fun setLastOffset(accountId: String, transactionOffset: TransactionOffset): Unit
    fun getLastOffset(accountId: String): TransactionOffset?
}