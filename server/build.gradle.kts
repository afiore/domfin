plugins {
    id("kotlin-project-conventions")
    //TODO: move in convention
    alias(libs.plugins.kotlin.serialization)
    application
}

dependencies {
    implementation(project(":core"))
    implementation(project(":sdk"))


    implementation(libs.kotlinx.coroutines)

    implementation(libs.bundles.logging)
    // DB
    implementation(platform(libs.exposed.bom))
    implementation(libs.bundles.db)

    // gRPC
    implementation(platform(libs.wire.bom))
    implementation(libs.wire.grpc.server)

    implementation(platform(libs.grpc.bom))
    implementation(libs.bundles.grpc.server)
}

application {
    mainClass.set("Main")
}