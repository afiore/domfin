plugins {
    kotlin("jvm")
    id("application")
    id("com.squareup.wire")
}


wire {
    kotlin {
        rpcRole = "client"
        protoLibrary = true

    }
}
