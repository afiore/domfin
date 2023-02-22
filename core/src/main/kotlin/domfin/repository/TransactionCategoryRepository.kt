package domfin.repository

import domfin.domain.CategorisationRule

interface TransactionCategoryRepository {
    fun categoriseTransactions(categorisationRule: CategorisationRule): Unit
}