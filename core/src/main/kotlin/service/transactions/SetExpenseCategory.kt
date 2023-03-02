package service.transactions

import dev.forkhandles.result4k.Result
import domfin.domain.CategoryId
import domfin.repository.AccountId

sealed class SetCategoryError {
    object NoExpensesInAccount : SetCategoryError()
    object CategoryNotFound : SetCategoryError()
}

interface SetExpenseCategory {
    fun categoriseExpenses(
        accountId: AccountId,
        transactionIds: Iterable<String>,
        categoryId: CategoryId
    ): Result<UInt, SetCategoryError>
}
