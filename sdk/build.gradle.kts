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
    implementation(platform("com.squareup.wire:wire-bom:4.5.1"))
    implementation("com.squareup.wire:wire-grpc-client")
    implementation("com.squareup.wire:wire-grpc-server")
//    implementation("com.squareup.wire:wire-grpc-server-generator")
    implementation("com.squareup.wire:wire-runtime")

    implementation(platform("io.grpc:grpc-bom:1.53.0"))
    implementation("io.grpc:grpc-services")
    implementation("io.grpc:grpc-core")
    implementation("io.grpc:grpc-netty")
    implementation("io.grpc:grpc-protobuf")
    implementation("io.grpc:grpc-protobuf")
    //not included in BOM
    implementation("io.grpc:grpc-kotlin-stub:1.3.0")
}