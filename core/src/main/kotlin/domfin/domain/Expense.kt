package domfin.domain

import java.time.LocalDate

data class Expense(
    val accountId: String,
    val transactionId: String,
    val valueDate: LocalDate,
    val amount: Amount,
    val creditorName: String,
    val category: Category?
)

