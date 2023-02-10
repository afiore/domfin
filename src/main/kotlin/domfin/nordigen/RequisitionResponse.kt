package domfin.nordigen

import kotlinx.serialization.Serializable
import org.http4k.core.Body
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.lens.BiDiBodyLens

@Serializable
data class RequisitionResponse(val id: String, val link: String) {
    companion object {
        val lens: BiDiBodyLens<RequisitionResponse> =
            Body.auto<RequisitionResponse>().toLens()

    }
}
