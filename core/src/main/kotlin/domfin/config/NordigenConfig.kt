package domfin.config

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

data class NordigenConfig(val secretId: Secret, val secretKey: Secret, val transactionSyncInterval: Duration) {
    companion object : FromEnv<NordigenConfig> {
        const val SecretId = "NORDIGEN_SECRET_ID"
        const val SecretKey = "NORDIGEN_SECRET_KEY"
        const val TransactionSyncInterval = "TRANSACTION_SYNC_INTERVAL_MILLIS"

        override fun fromEnv(env: Map<String, String>): EnvResult<NordigenConfig> =
            with(env) {
                getSecret(SecretId).flatMap { id ->
                    getSecret(SecretKey).flatMap { key ->
                        getVar(TransactionSyncInterval).parseOrError {
                            it.toLongOrNull()?.milliseconds
                        }.default(15.minutes).map { transactionSyncInterval ->
                            NordigenConfig(id, key, transactionSyncInterval)
                        }
                    }
                }
            }
    }
}
