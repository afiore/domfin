package domfin.repository

import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.*
import javax.sql.DataSource

//NOTICE: This doesn't seem to work as DB is dropped from memory after migrations run
fun SQLDataSource.mem(): DataSource =
    SQLDataSource.forJdbcUrl(
        "jdbc:sqlite:file:test-${
            UUID.randomUUID()
        }?mode=memory&cache=shared",
        sharedCache = true
    )

fun <T> SQLDataSource.fromTmpFile(run: (DataSource) -> T): T {
    val tmpDir = System.getProperty("java.io.tmpdir")
    val dbFile = "$tmpDir/domfin-${Instant.now().toEpochMilli()}.db"
    val datasSource = SQLDataSource.forJdbcUrl("jdbc:sqlite:$dbFile")
    return try {
        run(datasSource)
    } finally {
        Files.delete(Path.of(dbFile))
    }
}
