plugins {
    id("kotlin-project-conventions")
    //TODO: move in convention
    alias(libs.plugins.kotlin.serialization)
    application
}

dependencies {
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.bundles.logging)

    // HTTP
    implementation(platform(libs.http4k.bom))
    implementation(libs.bundles.http4k)

    // CLI
    implementation(libs.clikt)

    //DB
    implementation(platform(libs.exposed.bom))
    implementation(libs.bundles.db)
}

application {
    mainClass.set("Main")
}