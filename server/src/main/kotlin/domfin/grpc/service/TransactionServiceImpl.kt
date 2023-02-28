package domfin.grpc.service

import domfin.domain.CategoryId
import domfin.repository.TransactionRepository
import domfin.repository.transact
import domfin.sdk.CategorisedExpensesResponse
import domfin.sdk.GetCategorisedExpensesRequest
import domfin.sdk.services.TransactionServiceWireGrpc.TransactionServiceImplBase
import mu.KotlinLogging
import javax.sql.DataSource


class TransactionServiceImpl<T> constructor(private val repo: T, private val dataSource: DataSource) :
    TransactionServiceImplBase()
        where T : TransactionRepository {

    private val logger = KotlinLogging.logger {}
    override suspend fun GetCategorisedExpenses(request: GetCategorisedExpensesRequest): CategorisedExpensesResponse {
        val limitAndOffset = request.pagination.asLimitAndOffset()

        logger.debug { "parsed pagination: $limitAndOffset" }
        return dataSource.transact {
            val expenses = repo.getCategorisedExpenses(
                request.account_ids.toSet(),
                request.category_ids.map { CategoryId(it) }.toSet(),
                request.categorisation_filter.fromProto(),
                limitAndOffset
            )

            val nextPageToken = limitAndOffset.nextOffset(expenses.size.toUInt())?.let {
                PageToken.encode(it)
            }?.value

            CategorisedExpensesResponse(expenses.map { it.asProto() }, next_page_token = nextPageToken)
        }
    }
}