package domfin.grpc.service

import domfin.domain.Amount
import domfin.domain.Category
import domfin.domain.Expense
import domfin.repository.CategorisationFilter
import domfin.repository.LimitAndOffset
import domfin.sdk.Pagination
import domfin.sdk.Amount as AmountProto
import domfin.sdk.CategorisationFilter as CategorisationFilterProto
import domfin.sdk.Category as CategoryProto
import domfin.sdk.Expense as ExpenseProto


fun CategorisationFilterProto.fromProto(): CategorisationFilter =
    when (this) {
        CategorisationFilterProto.ALL -> CategorisationFilter.All
        CategorisationFilterProto.SELECTED -> CategorisationFilter.Selected
        CategorisationFilterProto.UNCATEGORISED -> CategorisationFilter.Uncategorised
    }

fun Category.asProto(): CategoryProto =
    CategoryProto(id.value, label)

fun Amount.asProto(): AmountProto =
    AmountProto(number, currency)

fun Expense.asProto(): ExpenseProto =
    ExpenseProto(
        accountId,
        transactionId,
        valueDate.format(domfin.serde.Defaults.DateFormat),
        amount.asProto(),
        creditorName,
        category?.asProto(),
    )

fun Pagination?.asLimitAndOffset(): LimitAndOffset {
    return when (this) {
        null -> LimitAndOffset.Default
        else -> run {
            val limit = if (per_page > 0) per_page.toUInt() else LimitAndOffset.Default.limit
            val offset = page_token?.let { PageToken(it).toULongOrNull() } ?: LimitAndOffset.Default.offset
            LimitAndOffset(limit, offset)
        }
    }

}