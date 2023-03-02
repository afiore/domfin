package domfin.grpc.service

import dev.forkhandles.result4k.*
import domfin.domain.CategoryId
import domfin.sdk.CategorisedExpensesResponse
import domfin.sdk.GetCategorisedExpensesRequest
import domfin.sdk.SetTransactionsCategoryRequest
import domfin.sdk.SetTransactionsCategoryResponse
import domfin.sdk.services.TransactionServiceWireGrpc.TransactionServiceImplBase
import io.grpc.Status
import io.grpc.StatusException
import mu.KotlinLogging
import service.transactions.GetCategorisedExpenses
import service.transactions.SetCategoryError
import service.transactions.SetExpenseCategory


class GrpcTransactionServiceImpl<T>(private val expensesService: T) :
    TransactionServiceImplBase()
        where T : GetCategorisedExpenses,
              T : SetExpenseCategory {

    private val logger = KotlinLogging.logger {}

    override suspend fun SetTransactionsCategory(request: SetTransactionsCategoryRequest): SetTransactionsCategoryResponse {
        @Suppress("NAME_SHADOWING")
        val request = request.validOrNull()

        if (request == null)
            throw StatusException(Status.INVALID_ARGUMENT.withDescription("Invalid request: neither `account_id`, `category_id` or `transaction_ids` can be empty"))
        else {
            return expensesService.categoriseExpenses(
                request.account_id,
                request.transaction_ids,
                CategoryId(request.category_id)
            ).mapFailure {
                when (it) {
                    is SetCategoryError.CategoryNotFound ->
                        StatusException(Status.INVALID_ARGUMENT.withDescription("Cannot find a category with id '${request.category_id}'"))

                    is SetCategoryError.NoExpensesInAccount ->
                        StatusException(Status.INVALID_ARGUMENT.withDescription("No expenses found for the supplied account id '${request.account_id}'"))
                }
            }.map { affectedRows ->
                SetTransactionsCategoryResponse(affectedRows.toInt())
            }.orThrow()
        }
    }

    override suspend fun GetCategorisedExpenses(request: GetCategorisedExpensesRequest): CategorisedExpensesResponse {
        val limitAndOffset = request.pagination.asLimitAndOffset()
        logger.debug { "parsed pagination offsets: $limitAndOffset" }

        val (expenses, nextPageToken) = expensesService.getCategorisedExpenses(
            request.account_ids.toSet(),
            request.category_ids.map { CategoryId(it) }.toSet(),
            request.categorisation_filter.fromProto(),
            limitAndOffset
        )


        return CategorisedExpensesResponse(expenses.map { it.asProto() }, nextPageToken)
    }
}
