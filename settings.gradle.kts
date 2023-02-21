rootProject.name = "domfin"

include("app", "sdk")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("kotlinx-coroutines", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").version("1.6.4")

            library(
                "kotlinx-serialization-json",
                "org.jetbrains.kotlinx",
                "kotlinx-serialization-json"
            ).version("1.5.0-RC")

            library("kotlin-logging", "io.github.microutils", "kotlin-logging-jvm").version("3.0.4")

            library("slf4j-reload4j", "org.slf4j", "slf4j-reload4j").version("2.0.6")

            library("http4k-bom", "org.http4k", "http4k-bom").version("4.37.0.0")
            library("http4k-core", "org.http4k", "http4k-core").withoutVersion()

            //TODO: use okhttp?
            library("http4k-client-apache", "org.http4k", "http4k-client-apache").withoutVersion()

            library(
                "http4k-format-kotlinx-serialization",
                "org.http4k",
                "http4k-format-kotlinx-serialization"
            ).withoutVersion()

            library("clikt", "com.github.ajalt.clikt", "clikt").version("3.5.0")

            // DB
            library("flyway-core", "org.flywaydb", "flyway-core").version("9.14.1")
            library("sqlite-jdbc", "org.xerial", "sqlite-jdbc").version("3.40.1.0")

            library("exposed-bom", "org.jetbrains.exposed", "exposed-bom").version("0.40.1")
            library("exposed-core", "org.jetbrains.exposed", "exposed-core").withoutVersion()
            library("exposed-dao", "org.jetbrains.exposed", "exposed-dao").withoutVersion()
            library("exposed-jdbc", "org.jetbrains.exposed", "exposed-jdbc").withoutVersion()

            library("wire-bom", "com.squareup.wire", "wire-bom").version("4.5.1")
            library("wire-grpc-server", "com.squareup.wire", "wire-grpc-server").withoutVersion()
            library("wire-grpc-client", "com.squareup.wire", "wire-grpc-client").withoutVersion()
            library("wire-runtime", "com.squareup.wire", "wire-runtime").withoutVersion()

            library("grpc-bom", "io.grpc", "grpc-bom").version("1.53.0")
            library("grpc-services", "io.grpc", "grpc-services").withoutVersion()
            library("grpc-core", "io.grpc", "grpc-core").withoutVersion()
            library("grpc-netty", "io.grpc", "grpc-netty").withoutVersion()
            library("grpc-protobuf", "io.grpc", "grpc-protobuf").withoutVersion()
            library("grpc-kotlin-stub", "io.grpc", "grpc-kotlin-stub").version("1.3.0")
        }
    }
}