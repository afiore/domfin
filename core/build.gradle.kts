plugins {
    id("kotlin-project-conventions")
    alias(libs.plugins.kotlin.serialization)
    application
}

dependencies {
    implementation(libs.kotlinx.coroutines)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.bundles.logging)

    implementation(platform(libs.http4k.bom))
    implementation(libs.bundles.http4k)

    implementation(libs.clikt)

    implementation(platform(libs.exposed.bom))
    implementation(libs.bundles.db)
}

application {
    mainClass.set("Main")
}