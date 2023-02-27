package domfin.config

import domfin.repository.SQLDataSource
import java.nio.file.Path
import javax.sql.DataSource

data class DBConfig(val fileName: Path) {

    val dataSource: DataSource by lazy {
        SQLDataSource.forJdbcUrl("jdbc:sqlite:${fileName}")
    }


    companion object : FromEnv<DBConfig> {
        private const val varName = "DB_PATH"
        override fun fromEnv(env: Env): EnvResult<DBConfig> {
            return env
                .getVar(varName)
                .map {
                    DBConfig(Path.of(it))
                }
        }
    }
}
