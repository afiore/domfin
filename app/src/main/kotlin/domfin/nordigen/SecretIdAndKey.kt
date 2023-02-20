package domfin.nordigen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.http4k.core.Body
import org.http4k.format.KotlinxSerialization.auto
import org.http4k.lens.BiDiBodyLens

@Serializable
data class SecretIdAndKey(
    @SerialName("secret_id") val secretId: String,
    @SerialName("secret_key") val secretKey: String
) {
    companion object Lenses {
        val lens: BiDiBodyLens<SecretIdAndKey> =
            Body.auto<SecretIdAndKey>().toLens()
    }
}
