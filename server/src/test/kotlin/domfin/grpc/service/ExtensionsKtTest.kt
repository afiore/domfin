package domfin.grpc.service

import domfin.repository.LimitAndOffset
import domfin.sdk.Pagination
import org.junit.jupiter.api.Test
import service.transactions.PageToken
import kotlin.test.assertEquals

class ExtensionsKtTest {
    @Test
    fun `converts a null pagination into the default LimitAndOffset`() {
        val pagination: Pagination? = null
        assertEquals(LimitAndOffset(500u, 0u), pagination.asLimitAndOffset())
    }

    @Test
    fun `falls back to default limit when pagination limit is less or equal 0`() {
        listOf(0, -5).forEach {
            val pagination = Pagination(per_page = it)
            assertEquals(LimitAndOffset(500u, 0u), pagination.asLimitAndOffset())
        }
    }

    @Test
    fun `falls back to default offset when page_token is missing OR invalid`() {
        listOf(null, "BAD TOKEN!").forEach {
            val pagination = Pagination(per_page = 10, page_token = it)
            assertEquals(LimitAndOffset(10u, 0u), pagination.asLimitAndOffset())
        }
    }

    @Test
    fun `decodes page_token as the offset`() {
        val pagination = Pagination(per_page = 10, page_token = PageToken.encode(220u).value)
        assertEquals(LimitAndOffset(10u, 220u), pagination.asLimitAndOffset())
    }


}
