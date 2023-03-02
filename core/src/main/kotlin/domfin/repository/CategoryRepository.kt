package domfin.repository

import domfin.domain.Category
import domfin.domain.CategoryId

interface CategoryRepository {
    fun getCategory(id: CategoryId): Category?
}
