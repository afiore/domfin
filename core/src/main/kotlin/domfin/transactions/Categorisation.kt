package domfin.transactions

import domfin.repository.CategorisationRuleRepository
import domfin.repository.TransactionCategoryRepository
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

class Categorisation<Repo> constructor(
    private val repo: Repo,
    private val dataSource: DataSource,
) where Repo : CategorisationRuleRepository,
        Repo : TransactionCategoryRepository {

    private val logger = KotlinLogging.logger {}

    fun applyAllRules() {
        //TODO: avoid a separate connection per class
        val db = Database.connect(dataSource)
        transaction(db) {
            val allRules = repo.getAllCategorisationRules()
            logger.info { "applying ${allRules.size} categorisation rules" }
            allRules.forEach {
                repo.categoriseTransactions(it)
            }
        }
    }
}
