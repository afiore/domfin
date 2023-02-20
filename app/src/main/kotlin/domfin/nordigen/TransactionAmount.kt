package domfin.nordigen

import kotlinx.serialization.Serializable

@Serializable
data class TransactionAmount(val currency: String, val amount: Double)
