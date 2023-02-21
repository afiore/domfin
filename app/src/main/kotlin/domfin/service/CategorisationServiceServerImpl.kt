package domfin.service

import domfin.domain.CategoryId
import domfin.repository.CategoriesRepository
import domfin.repository.CategorisationRuleRepository
import domfin.sdk.*
import domfin.sdk.services.CategorisationServiceWireGrpc.CategorisationServiceImplBase
import io.grpc.Status
import io.grpc.StatusRuntimeException
import mu.KotlinLogging
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

class CategorisationServiceServerImpl<T> constructor(
    private val repo: T,
    private val dataSource: DataSource
) :
    CategorisationServiceImplBase()
        where T : CategoriesRepository,
              T : CategorisationRuleRepository {

    private val logger = KotlinLogging.logger {}
    override suspend fun GetAllCategorisationRules(request: GetAllCategorisationRulesRequest): GetAllCategorisationRulesResponse {
        val rules = withDb {
            repo.getAllCategorisationRules().map {
                CategorisationRule(Category(it.category.id.value, it.category.label), it.substrings.toList())
            }
        }
        return GetAllCategorisationRulesResponse(rules)
    }

    override suspend fun SetCategorisationRule(request: SetCategorisationRuleRequest): SetCategorisationRuleResponse {
        val categoryId = CategoryId(request.category_id)
        withDb {
            val category = repo.getCategory(categoryId)
            if (category == null)
                throw StatusRuntimeException(Status.NOT_FOUND)
            else
                repo.setCategorisationRule(categoryId, request.substrings.filterNot { it.trim().isBlank() }.toSet())
        }

        return SetCategorisationRuleResponse()
    }

    private fun <T> withDb(run: Transaction.() -> T): T {
        val db = Database.connect(dataSource)
        return transaction(db) {
            run()
        }
    }
}