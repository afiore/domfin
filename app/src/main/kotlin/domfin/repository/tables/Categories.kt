package domfin.repository.tables

import org.jetbrains.exposed.sql.Table

object Categories : Table("categories") {
    val id = text("id")
    val label = text("label")

    override val primaryKey = PrimaryKey(id)
}