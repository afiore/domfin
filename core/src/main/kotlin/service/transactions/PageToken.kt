package service.transactions

import java.nio.ByteBuffer
import java.util.*

@JvmInline
value class PageToken(val value: String) {
    fun toULongOrNull(): ULong? = kotlin.runCatching {
        val bytes = Base64.getDecoder().decode(value)
        ByteBuffer.wrap(bytes).long
    }.getOrNull()?.let {
        if (it > 0)
            it.toULong()
        else
            null
    }

    companion object {
        fun encode(value: ULong): PageToken {
            val buf = ByteBuffer.allocate(ULong.SIZE_BYTES).putLong(value.toLong())
            return PageToken(Base64.getEncoder().encodeToString(buf.array()))
        }
    }
}
