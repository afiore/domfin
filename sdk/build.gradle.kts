plugins {
    kotlin("jvm")
    id("application")
    //TODO: can we avoid duplicating BOM version?
    id("com.squareup.wire").version("4.5.1")
}

repositories {
    mavenCentral()
}

wire {
    kotlin {
        rpcRole = "server"
        rpcCallStyle = "suspending"
        includes = listOf("domfin.sdk.services.*")
        grpcServerCompatible = true
        exclusive = false
    }
    kotlin {
        rpcRole = "client"
        rpcCallStyle = "suspending"
    }
}

dependencies {
    implementation(platform(libs.wire.bom))
    implementation(libs.wire.grpc.client)
    implementation(libs.wire.grpc.server)
    implementation(libs.wire.runtime)

    implementation(platform(libs.grpc.bom))
    implementation(libs.grpc.core)
    implementation(libs.grpc.services)
    implementation(libs.grpc.kotlin.stub)
}