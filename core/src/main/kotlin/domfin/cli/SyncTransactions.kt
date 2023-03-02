package domfin.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import domfin.nordigen.client.AccountInformationApiImpl
import domfin.repository.SQLDataSource
import domfin.repository.SqlMigrator
import domfin.repository.SqliteRepository
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import service.transactions.AutoCategorisation
import service.transactions.Sync
import java.nio.file.Path

class SyncTransactions : CliktCommand() {
    private val secrets: Secrets by requireObject<Secrets>()
    private val logger = KotlinLogging.logger {}

    val dbUri by option(
        help = "Path to the Sqlite database file"
    ).path(canBeFile = true, canBeSymlink = true, mustBeReadable = true, mustBeWritable = true)
        .default(Path.of(System.getProperty("user.home"), ".domfin.sqlite"))


    override fun run() {
        runBlocking {
            val dataSource = SQLDataSource.forJdbcUrl("jdbc:sqlite:$dbUri")
            val runMigrator = SqlMigrator(dataSource, includeSeedData = true)
            val api = AccountInformationApiImpl.withFreshToken(secrets.id, secrets.key)
            runMigrator()
            val sync = Sync(api, SqliteRepository, dataSource)
            val categorisation = AutoCategorisation(SqliteRepository, dataSource)
            sync.runForAllAccounts()
            categorisation.applyAllRules()
        }
    }
}
