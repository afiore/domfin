package domfin.repository

import org.flywaydb.core.Flyway
import javax.sql.DataSource

class SqlMigrator(private val dataSource: DataSource, includeSeedData: Boolean) {
    private val locations = (if (includeSeedData) arrayOf(MigrationsResourceDir, SeedDataResourceDir) else arrayOf(
        MigrationsResourceDir
    ))

    operator suspend fun invoke() {
        try {
            val flyway = Flyway.configure()
                .dataSource(dataSource)
                .group(true)
                .outOfOrder(false)
                .locations(*locations)
                .load()
            flyway.migrate()
        } finally {
            dataSource.connection.close()
        }
    }

    companion object {
        private val MigrationsResourceDir = "classpath:/db/migration"
        private val SeedDataResourceDir = "classpath:/db/seed"
    }
}