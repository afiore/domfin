package domfin.repository

import domfin.domain.Category
import domfin.domain.CategoryId

interface CategoriesRepository {
    fun getCategory(id: CategoryId): Category?
}