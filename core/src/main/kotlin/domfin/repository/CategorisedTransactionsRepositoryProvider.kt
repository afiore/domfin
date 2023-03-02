package domfin.repository

// Is there a way to avoid introducing this interface?
interface CategorisedTransactionsRepository : TransactionRepository, TransactionCategoryRepository, CategoryRepository
interface CategorisedTransactionsRepositoryProvider {
    val repository: CategorisedTransactionsRepository
}
