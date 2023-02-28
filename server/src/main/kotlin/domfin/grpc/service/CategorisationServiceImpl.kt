package domfin.grpc.service

import domfin.domain.CategoryId
import domfin.repository.CategoriesRepository
import domfin.repository.CategorisationRuleRepository
import domfin.repository.transact
import domfin.sdk.*
import domfin.sdk.services.CategorisationServiceWireGrpc.CategorisationServiceImplBase
import io.grpc.Status
import io.grpc.StatusRuntimeException
import javax.sql.DataSource

//TODO: is there a way to fail compilation when new methods are introduced? should we care?
class CategorisationServiceImpl<T> constructor(
    private val repo: T,
    private val dataSource: DataSource
) :
    CategorisationServiceImplBase()
        where T : CategoriesRepository,
              T : CategorisationRuleRepository {

    override suspend fun GetAllCategorisationRules(request: GetAllCategorisationRulesRequest): GetAllCategorisationRulesResponse {
        val rules = dataSource.transact {
            repo.getAllCategorisationRules().map {
                CategorisationRule(Category(it.category.id.value, it.category.label), it.substrings.toList())
            }
        }
        return GetAllCategorisationRulesResponse(rules)
    }

    override suspend fun SetCategorisationRule(request: SetCategorisationRuleRequest): SetCategorisationRuleResponse {
        val categoryId = CategoryId(request.category_id)
        dataSource.transact {
            val category = repo.getCategory(categoryId)
            if (category == null)
                throw StatusRuntimeException(Status.NOT_FOUND)
            else
                repo.setCategorisationRule(categoryId, request.substrings.filterNot { it.trim().isBlank() }.toSet())
        }

        return SetCategorisationRuleResponse()
    }

}
