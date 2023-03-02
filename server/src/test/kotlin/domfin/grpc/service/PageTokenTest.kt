package domfin.grpc.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import service.transactions.PageToken

class PageTokenTest {
    @Test
    fun `PageToken round-trip`() {
        val expected: ULong = 250u
        val token = PageToken.encode(expected)
        assertEquals(expected, token.toULongOrNull())
    }

    @Test
    fun `returns null when parsing ULong from invalid token`() {
        assertNull(PageToken("BAD TOKEN").toULongOrNull())
    }
}
