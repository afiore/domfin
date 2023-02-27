package domfin.repository

import org.flywaydb.core.Flyway
import javax.sql.DataSource

class SqlMigrator(private val dataSource: DataSource, includeSeedData: Boolean) {
    private val locations = (if (includeSeedData) arrayOf(MigrationsResourceDir, SeedDataResourceDir) else arrayOf(
        MigrationsResourceDir
    ))

    operator fun invoke() {
        try {
            @SuppressWarnings("SpreadOperator")
            val flyway = Flyway.configure()
                .dataSource(dataSource)
                .group(true)
                .outOfOrder(false)
                .locations(
                    *locations
                )
                .load()
            flyway.migrate()
        } finally {
            dataSource.connection.close()
        }
    }

    companion object {
        private const val MigrationsResourceDir = "classpath:/db/migration"
        private const val SeedDataResourceDir = "classpath:/db/seed"
    }
}
