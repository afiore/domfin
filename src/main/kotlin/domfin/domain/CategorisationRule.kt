package domfin.domain

data class CategorisationRule(val categoryId: CategoryId, val substrings: Set<String>)
