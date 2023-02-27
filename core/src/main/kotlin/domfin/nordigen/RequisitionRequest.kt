package domfin.nordigen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.http4k.core.Body
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.lens.BiDiBodyLens

@Serializable
data class RequisitionRequest(val redirect: String, @SerialName("institution_id") val institutionId: String) {
    companion object {
        val lens: BiDiBodyLens<RequisitionRequest> =
            Body.auto<RequisitionRequest>().toLens()

    }
}
