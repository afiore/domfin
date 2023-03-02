package domfin.grpc

import domfin.config.AppConfig
import domfin.grpc.service.ExceptionInterceptor
import domfin.grpc.service.GrpcCategorisationServiceImpl
import domfin.grpc.service.GrpcTransactionServiceImpl
import domfin.nordigen.client.AccountInformationApiImpl
import domfin.repository.CategorisedTransactionsRepositoryProvider
import domfin.repository.DataSourceProvider
import domfin.repository.SqlMigrator
import domfin.repository.SqliteRepository
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import kotlinx.coroutines.*
import mu.KotlinLogging
import service.transactions.ExpensesServiceImpl
import service.transactions.Sync
import kotlin.coroutines.CoroutineContext


fun main() {

    val logger = KotlinLogging.logger {}
    val (dbConfig, grpcConfig, nordigenConfig) = AppConfig.fromEnv(System.getenv()).valueOrThrow()

    val migrator = SqlMigrator(dbConfig.dataSource, includeSeedData = true)

    suspend fun launchSyncTransactions(context: CoroutineContext = Dispatchers.IO) {
        withContext(context) {
            while (true) {
                logger.info { "About to sink transactions ..." }
                val accountApi =
                    AccountInformationApiImpl.withFreshToken(
                        nordigenConfig.secretId.raw,
                        nordigenConfig.secretKey.raw
                    )
                val transactionSync = Sync(accountApi, SqliteRepository, dbConfig.dataSource)
                transactionSync.runForAllAccounts()

                logger.info { "Sleeping for ${nordigenConfig.transactionSyncInterval}... " }
                delay(nordigenConfig.transactionSyncInterval)
            }
        }
    }


    runBlocking {
        migrator.invoke()
        launch {
            launchSyncTransactions()
        }
        val dependencyProvider = object : DataSourceProvider, CategorisedTransactionsRepositoryProvider {
            override val dataSource = dbConfig.dataSource
            override val repository = SqliteRepository
        }

        with(dependencyProvider) {
            val server =
                ServerBuilder
                    .forPort(grpcConfig.serverPort.toInt())
                    .addService(ProtoReflectionService.newInstance())
                    .addService(
                        GrpcCategorisationServiceImpl(SqliteRepository, dbConfig.dataSource)
                    )
                    .addService(
                        GrpcTransactionServiceImpl(ExpensesServiceImpl())
                    )
                    .intercept(ExceptionInterceptor())
                    .build()

            server.start()
            logger.info { "Starting gRPC server on port ${grpcConfig.serverPort}" }
            server.awaitTermination()

        }


    }
}

