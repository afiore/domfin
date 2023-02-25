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

    implementation(platform(libs.exposed.bom))
    implementation(libs.bundles.db)

    implementation(platform(libs.wire.bom))
    implementation(libs.wire.grpc.server)
    implementation(platform(libs.grpc.bom))
    implementation(libs.bundles.grpc.server)
}

application {
    applicationDefaultJvmArgs = listOf(
        "-Dkotlinx.coroutines.debug"
    )
    mainClass.set("domfin.grpc.GrpcServerKt")
}