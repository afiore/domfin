package domfin.repository

import domfin.domain.CategorisationRule
import domfin.domain.CategoryId

typealias AffectedRows = UInt

interface TransactionCategoryRepository {
    fun categoriseTransactions(
        accountId: AccountId,
        transactionIds: Iterable<String>,
        categoryId: CategoryId
    ): AffectedRows

    fun applyCategorisationRule(categorisationRule: CategorisationRule)
}
