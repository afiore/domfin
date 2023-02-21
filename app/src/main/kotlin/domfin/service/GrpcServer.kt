package domfin.service

import domfin.repository.SQLDataSource
import domfin.repository.SqlMigrator
import domfin.repository.SqliteRepository
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging

fun main() {
    val ds = SQLDataSource.forJdbcUrl("jdbc:sqlite:domfin-grpc.sql")
    val migrator = SqlMigrator(ds, includeSeedData = true)

    val logger = KotlinLogging.logger {}

    runBlocking {
        migrator.invoke()
    }


    val server =
        ServerBuilder
            .forPort(8980)
            .addService(ProtoReflectionService.newInstance())
            .addService(
                CategorisationServiceServerImpl(SqliteRepository, ds)
            )
            .build()

    server.start()
    server.awaitTermination()
}