package domfin.repository

import domfin.domain.CategorisationRule

interface CategorisationRuleRepository {
    fun getAllCategorisationRules(): List<CategorisationRule>
}