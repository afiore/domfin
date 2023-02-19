package domfin.repository

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

fun SQLDataSource.tmpFile(): DataSource =
    SQLDataSource.forJdbcUrl(
        "jdbc:sqlite:/tmp/domfin-${
            Instant.now().toEpochMilli()
        }.db",
    )
