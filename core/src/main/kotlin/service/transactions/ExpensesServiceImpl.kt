package service.transactions

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import domfin.domain.CategoryId
import domfin.repository.*

context(CategorisedTransactionsRepositoryProvider, DataSourceProvider)
class ExpensesServiceImpl() : SetExpenseCategory, GetCategorisedExpenses {
    override fun categoriseExpenses(
        accountId: AccountId,
        transactionIds: Iterable<String>,
        categoryId: CategoryId
    ): Result<UInt, SetCategoryError> =
        dataSource.transact {
            if (repository.getCategory(categoryId) == null)
                Failure(SetCategoryError.CategoryNotFound)
            else if (repository.getCategorisedExpenses(setOf(accountId), limitAndOffset = LimitAndOffset.First)
                    .isEmpty()
            )
                Failure(SetCategoryError.NoExpensesInAccount)
            else {
                val affectedRows = repository.categoriseTransactions(accountId, transactionIds, categoryId)
                Success(affectedRows)
            }
        }

    override fun getCategorisedExpenses(
        accountIds: Set<AccountId>,
        categoryIds: Set<CategoryId>,
        filter: CategorisationFilter,
        limitAndOffset: LimitAndOffset
    ): PaginatedExpenses =
        dataSource.transact {
            val expenses = repository.getCategorisedExpenses(accountIds, categoryIds, filter, limitAndOffset)
            val nextPageToken = limitAndOffset.nextOffset(expenses.size.toUInt())?.let {
                PageToken.encode(it)
            }?.value
            PaginatedExpenses(expenses, nextPageToken)
        }

}

