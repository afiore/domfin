package domfin.repository.tables


import org.jetbrains.exposed.sql.Table

object CategorisationRules : Table("categorisation_rules") {
    val categoryId = text("category_id")
    val substring = text("substring")

    override val primaryKey = PrimaryKey(categoryId, substring)
}