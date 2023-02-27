package domfin.grpc

import domfin.config.AppConfig
import domfin.grpc.service.CategorisationServiceServerImpl
import domfin.nordigen.client.AccountInformationApiImpl
import domfin.repository.SqlMigrator
import domfin.repository.SqliteRepository
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import kotlinx.coroutines.*
import mu.KotlinLogging
import kotlin.coroutines.CoroutineContext


fun main() {

    val logger = KotlinLogging.logger {}
    val (dbConfig, grpcConfig, nordigenConfig) = AppConfig.fromEnv(System.getenv()).valueOrThrow()

    val migrator = SqlMigrator(dbConfig.dataSource, includeSeedData = true)

    suspend fun launchSyncTransactions(context: CoroutineContext = Dispatchers.IO): Job {
        return withContext(context) {
            launch {
                while (true) {
                    logger.info { "About to sink transactions ..." }
                    val accountApi =
                        AccountInformationApiImpl.withFreshToken(
                            nordigenConfig.secretId.raw,
                            nordigenConfig.secretKey.raw
                        )
                    val transactionSync = domfin.transactions.Sync(accountApi, SqliteRepository, dbConfig.dataSource)
                    transactionSync.runForAllAccounts()

                    logger.info { "Sleeping for ${nordigenConfig.transactionSyncInterval}... " }
                    delay(nordigenConfig.transactionSyncInterval)
                }
            }
        }
    }


    runBlocking {
        migrator.invoke()
        launchSyncTransactions()

        val server =
            ServerBuilder
                .forPort(grpcConfig.serverPort.toInt())
                .addService(ProtoReflectionService.newInstance())
                .addService(
                    CategorisationServiceServerImpl(SqliteRepository, dbConfig.dataSource)
                )
                .build()

        server.start()
        server.awaitTermination()
    }
}
