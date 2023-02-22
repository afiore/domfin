package domfin.repository

import domfin.domain.CategorisationRule
import domfin.domain.CategoryId

interface CategorisationRuleRepository {
    fun setCategorisationRule(categoryId: CategoryId, substrings: Set<String>): Unit
    fun getAllCategorisationRules(): List<CategorisationRule>
}