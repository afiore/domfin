package service.transactions

import domfin.repository.CategorisationRuleRepository
import domfin.repository.TransactionCategoryRepository
import domfin.repository.transact
import mu.KotlinLogging
import javax.sql.DataSource


class AutoCategorisation<Repo> constructor(
    private val repo: Repo,
    private val dataSource: DataSource,
) where Repo : CategorisationRuleRepository,
        Repo : TransactionCategoryRepository {

    private val logger = KotlinLogging.logger {}

    fun applyAllRules() {
        dataSource.transact {
            val allRules = repo.getAllCategorisationRules()
            logger.info { "applying ${allRules.size} categorisation rules" }
            allRules.forEach {
                repo.applyCategorisationRule(it)
            }
        }
    }
}
