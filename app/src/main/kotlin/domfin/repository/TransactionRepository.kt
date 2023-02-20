package domfin.repository

import domfin.nordigen.Transaction
import domfin.nordigen.TransactionsByStatus

interface TransactionRepository {

    fun insertAllTransactions(accountId: String, transactionsByStatus: TransactionsByStatus): Unit {
        insertAllTransactions(accountId, transactionsByStatus.booked, isBooked = true)
        insertAllTransactions(accountId, transactionsByStatus.pending, isBooked = false)
    }

    abstract fun insertAllTransactions(accountId: String, transactions: Iterable<Transaction>, isBooked: Boolean): Unit
}