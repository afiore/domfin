package domfin.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.requireObject
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import domfin.nordigen.client.AccountInformationApiImpl
import domfin.repository.SqliteRepository
import domfin.transactions.Sync
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource
import java.nio.file.Path
import javax.sql.DataSource

//TODO: extract and test business logic
class SyncTransactions : CliktCommand() {
    private val secrets: Secrets by requireObject<Secrets>()
    private val logger = KotlinLogging.logger {}

    val dbUri by option(
        help = "Path to the Sqlite database file"
    ).path(canBeFile = true, canBeSymlink = true, mustBeReadable = true, mustBeWritable = true)
        .default(Path.of(System.getProperty("user.home"), ".domfin.sqlite"))

    private val dataSource: DataSource by lazy {
        val config = SQLiteConfig()
        val dataSource = SQLiteDataSource(config)
        dataSource.url = "jdbc:sqlite:$dbUri"
        dataSource
    }


    override fun run() {
        runBlocking {
            val api = AccountInformationApiImpl.withFreshToken(secrets.id, secrets.key)
            val sync = Sync(api, SqliteRepository, dataSource)
            sync.runForAllAccounts()
        }
    }
}
