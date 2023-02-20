package domfin.nordigen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.http4k.core.Body
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.lens.BiDiBodyLens

@Serializable
data class Institution(
    val id: String,
    val name: String,
    @SerialName("transaction_total_days") val transactionTotalDays: Int
) {
    companion object Lenses {
        val listLens: BiDiBodyLens<List<Institution>> = Body.auto<List<Institution>>().toLens()
    }
}
