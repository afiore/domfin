package domfin.nordigen

import kotlinx.serialization.Serializable
import org.http4k.core.Body
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.lens.BiDiBodyLens

@Serializable
data class Requisition(val id: String, val accounts: List<String>, val status: String) {
    companion object {
        val lens: BiDiBodyLens<Requisition> =
            Body.auto<Requisition>().toLens()
    }
}