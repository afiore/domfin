plugins {
    id("kotlin-project-conventions")
    kotlin("plugin.serialization") version "1.8.0"
    application
}

dependencies {
    implementation(project(":sdk"))

    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.json)
    // Logging

    implementation(libs.kotlin.logging)
    implementation(libs.slf4j.reload4j)

    // HTTP
    implementation(platform(libs.http4k.bom))
    implementation(libs.http4k.core)
    implementation(libs.http4k.format.kotlinx.serialization)
    implementation(libs.http4k.client.apache)

    // CLI
    implementation(libs.clikt)

    // DB
    implementation(libs.flyway.core)
    implementation(libs.sqlite.jdbc)
    //
    implementation(platform(libs.exposed.bom))
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)

    // gRPC
    implementation(platform(libs.wire.bom))
    implementation(libs.wire.grpc.client)
    implementation(libs.wire.grpc.server)

    implementation(platform(libs.grpc.bom))
    implementation(libs.grpc.core)
    implementation(libs.grpc.services)
    implementation(libs.grpc.netty)
}

application {
    mainClass.set("Main")
}