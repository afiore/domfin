plugins {
    id("application")
    id("kotlin-project-conventions")
    alias(libs.plugins.wire)
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

    protoPath {
        srcJar(libs.grpc.google.protobuf.java)
    }
    protoPath {
        srcJar(libs.grpc.google.common.protos)
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
