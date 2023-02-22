package domfin.nordigen

import kotlinx.serialization.Serializable
import org.http4k.core.Body
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.lens.BiDiBodyLens

@Serializable
data class TransactionResults(
    val transactions: TransactionsByStatus,
) {
    companion object {
        val lens: BiDiBodyLens<TransactionResults> =
            Body.auto<TransactionResults>().toLens()

    }
}
