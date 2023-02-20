plugins {
    kotlin("jvm")
    id("application")
    id("com.squareup.wire")
}


wire {
    kotlin {
        rpcRole = "server"
        rpcCallStyle = "suspending"
        includes = listOf("domfin.sdk.services.*")
        exclusive = false
    }
    kotlin {
        rpcRole = "client"
        rpcCallStyle = "suspending"
    }
}

val wireVersion: String by project

dependencies {
    implementation("com.squareup.wire:wire-grpc-client:$wireVersion")
}