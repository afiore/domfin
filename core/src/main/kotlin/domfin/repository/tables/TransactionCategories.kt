package domfin.repository.tables

import org.jetbrains.exposed.sql.Table

object TransactionCategories : Table("transaction_categories") {
    val accountId = text("account_id")
    val transactionId = text("transaction_id")
    val categoryId = text("category_id")

    override val primaryKey = PrimaryKey(accountId, transactionId, categoryId)
}
