package service.transactions

import domfin.domain.CategoryId
import domfin.domain.Expense
import domfin.repository.AccountId
import domfin.repository.CategorisationFilter
import domfin.repository.LimitAndOffset

data class PaginatedExpenses(val expenses: List<Expense>, val nextPageToken: String?) {
    companion object {

    }
}

interface GetCategorisedExpenses {
    fun getCategorisedExpenses(
        accountIds: Set<AccountId>,
        categoryIds: Set<CategoryId>,
        filter: CategorisationFilter,
        limitAndOffset: LimitAndOffset
    ): PaginatedExpenses
}
