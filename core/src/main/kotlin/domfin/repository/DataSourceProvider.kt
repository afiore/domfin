package domfin.repository

import javax.sql.DataSource

interface DataSourceProvider {
    val dataSource: DataSource
}
