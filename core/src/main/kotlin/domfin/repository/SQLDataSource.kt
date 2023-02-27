package domfin.repository

import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource
import javax.sql.DataSource

object SQLDataSource {
    fun forJdbcUrl(jdbcUrl: String, sharedCache: Boolean = false): DataSource {
        val config = SQLiteConfig().apply {
            enforceForeignKeys(true)
            setSharedCache(sharedCache)
        }
        return SQLiteDataSource(config).apply {
            url = jdbcUrl
        }
    }
}
