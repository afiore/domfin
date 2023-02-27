package domfin.repository

import domfin.domain.CategoryId
import domfin.domain.Expense
import domfin.nordigen.Transaction as NordigenTransaction
import domfin.nordigen.TransactionsByStatus as NordigenTransactionByStatus

typealias AccountId = String

interface TransactionRepository {

    fun insertAllTransactions(accountId: AccountId, transactionsByStatus: NordigenTransactionByStatus): Unit {
        insertAllTransactions(accountId, transactionsByStatus.booked, isBooked = true)
        insertAllTransactions(accountId, transactionsByStatus.pending, isBooked = false)
    }

    abstract fun getCategorisedExpenses(
        accountIds: Set<AccountId> = setOf(),
        categoryIds: Set<CategoryId> = setOf()
    ): List<Expense>


    abstract fun insertAllTransactions(
        accountId: String,
        transactions: Iterable<NordigenTransaction>,
        isBooked: Boolean
    ): Unit
}