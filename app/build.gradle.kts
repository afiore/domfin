plugins {
    id("kotlin-project-conventions")
    kotlin("plugin.serialization") version "1.8.0"
    application
}

dependencies {
    implementation(project(":sdk"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0-RC")
    // Logging

    implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")
    implementation("org.slf4j:slf4j-reload4j:2.0.6")

    // HTTP
    implementation(platform("org.http4k:http4k-bom:4.37.0.0"))
    implementation("org.http4k:http4k-core")
    implementation("org.http4k:http4k-client-apache")
    implementation("org.http4k:http4k-format-kotlinx-serialization")

    // CLI
    implementation("com.github.ajalt.clikt:clikt:3.5.0")

    // DB
    implementation("org.flywaydb:flyway-core:9.14.1")
    implementation("org.xerial:sqlite-jdbc:3.40.1.0")
    //
    implementation(platform("org.jetbrains.exposed:exposed-bom:0.40.1"))
    implementation("org.jetbrains.exposed:exposed-core")
    implementation("org.jetbrains.exposed:exposed-dao")
    implementation("org.jetbrains.exposed:exposed-jdbc")

    // gRPC
    implementation(platform("com.squareup.wire:wire-bom:4.5.1"))
    implementation("com.squareup.wire:wire-grpc-server")

    implementation(platform("io.grpc:grpc-bom:1.53.0"))
    implementation("io.grpc:grpc-core")
    implementation("io.grpc:grpc-services")
    implementation("io.grpc:grpc-netty")
//    implementation("io.grpc:grpc-protobuf")
}

application {
    mainClass.set("Main")
}