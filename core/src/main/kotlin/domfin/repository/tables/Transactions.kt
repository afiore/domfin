package domfin.repository.tables

import org.jetbrains.exposed.sql.Table

object Transactions : Table("transactions") {
    val accountId = text("account_id")
    val transactionId = text("transaction_id")

    val type = text("type").check("transactions_check_type", { it inList listOf("credit", "debit") })
    val status = text("status").check("transactions_check_status", { it inList listOf("booked", "pending") })
    val bookingDate = varchar("booking_date", 10)
    val valueDate = varchar("value_date", 10)
    val amount = long("amount")
    val currency = text("currency")
    val creditorName = text("creditor_name").nullable()
    val debtorName = text("debtor_name").nullable()
    val debtorAccount = text("debtor_account").nullable()
    val remittanceInformation = text("remittance_information").nullable()
    val bankTransactionCode = text("bank_transaction_code")
    val internalTransactionId = text("internal_transaction_id").nullable()

    override val primaryKey = PrimaryKey(accountId, transactionId)

    val CreditType = "credit"
    val DebitType = "debit"

    val BookedStatus = "booked"
    val PendingStatus = "pending"
}