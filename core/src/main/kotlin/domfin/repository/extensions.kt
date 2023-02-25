package domfin.repository

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

fun <T> DataSource.transact(run: Transaction.() -> T): T {
    val db = Database.connect(this)
    return transaction(db) {
        run()
    }
}
