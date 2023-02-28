package domfin.repository

import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test

class LimitAndOffsetTest {
    @Test
    fun `LimitAndOffset#next increments the offset when the number of items in page matches the limit`() {
        assertEquals(
            LimitAndOffset(5u, 5u),
            LimitAndOffset(5u).next(5u)
        )
    }

    @Test
    fun `LimitAndOffset#next returns null when the number of items in page is below the limit`() {
        listOf(0u, 4u).forEach {
            kotlin.test.assertNull(
                LimitAndOffset(5u).next(it)
            )
        }
    }

}