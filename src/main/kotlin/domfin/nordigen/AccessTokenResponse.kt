package domfin.nordigen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.http4k.core.Body
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.lens.BiDiBodyLens

@Serializable
data class AccessTokenResponse(
    val access: String,
    @SerialName("access_expires") val accessExpires: Long,
    val refresh: String,
    @SerialName("refresh_expires") val refreshExpires: Long
) {
    //some fields have been omitted here as not needed for now: i.e. `access_expires`, `refresh`, and `refresh_expires`
    companion object Lenses {
        val lens: BiDiBodyLens<AccessTokenResponse> =
            Body.auto<AccessTokenResponse>().toLens()
    }
}