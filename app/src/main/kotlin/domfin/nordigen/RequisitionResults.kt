package domfin.nordigen

import kotlinx.serialization.Serializable
import org.http4k.core.Body
import org.http4k.core.Uri
import org.http4k.core.queries
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.lens.BiDiBodyLens

@Serializable
data class RequisitionResults(val count: Int, val next: String?, val results: List<Requisition>) {

    fun nextOffset(): Int? =
        next?.let {
            Uri.of(it).queries().firstOrNull() { it.first == "offset" }?.second?.toInt()
        }

    companion object {
        val lens: BiDiBodyLens<RequisitionResults> =
            Body.auto<RequisitionResults>().toLens()
    }
}