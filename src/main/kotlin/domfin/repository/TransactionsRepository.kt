package domfin.repository

import domfin.nordigen.Transaction
import domfin.nordigen.TransactionsByStatus

abstract class TransactionsRepository {

    fun insertAll(accountId: String, transactionsByStatus: TransactionsByStatus): Unit {
        insertAll(accountId, transactionsByStatus.booked, isBooked = true)
        insertAll(accountId, transactionsByStatus.pending, isBooked = false)
    }

    abstract fun insertAll(accountId: String, transactions: Iterable<Transaction>, isBooked: Boolean): Unit
}