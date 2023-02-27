package domfin.config

import org.junit.jupiter.api.Test
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class AppConfigTest {
    val validEnv = mapOf(
        "DB_PATH" to "/path/to/file",
        "GRPC_SERVER_PORT" to "9999",
        "NORDIGEN_SECRET_ID" to "TheId",
        "NORDIGEN_SECRET_KEY" to "TheKey",
        "TRANSACTION_SYNC_INTERVAL_MILLIS" to (12.hours.inWholeMilliseconds.toString())
    )

    @Test
    fun `parses an app config from the supplied environment`() {
        with(AppConfig.fromEnv(validEnv).valueOrThrow()) {
            assertEquals(Path.of("/path/to/file"), db.fileName)
            assertEquals(9999u, grpc.serverPort)
            assertEquals(Secret("TheId"), nordigen.secretId)
            assertEquals(Secret("TheKey"), nordigen.secretKey)
            assertEquals(12.hours, nordigen.transactionSyncInterval)
        }
    }

    @Test
    fun `fails if the supplied GRPC_SERVER_PORT cannot be parsed`() {
        listOf("NOT_A_NUMBER", "-1", "1.0").forEach { port ->
            val env = validEnv + Pair("GRPC_SERVER_PORT", port)
            with(AppConfig.fromEnv(env).asError()) {
                assertEquals(ErrorKind.ParsingError, kind)
            }
        }
    }

    @Test
    fun `uses a default when the GRPC_SERVER_PORT value is not supplied`() {
        with(AppConfig.fromEnv(validEnv - "GRPC_SERVER_PORT").valueOrThrow()) {
            assertEquals(8980u, grpc.serverPort)
        }
    }

    @Test
    fun `uses a default when the TRANSACTION_SYNC_INTERVAL_MILLIS value is not supplied`() {
        with(AppConfig.fromEnv(validEnv - "TRANSACTION_SYNC_INTERVAL_MILLIS").valueOrThrow()) {
            assertEquals(15.minutes, nordigen.transactionSyncInterval)
        }
    }
}
