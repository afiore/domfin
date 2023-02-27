plugins {
    `kotlin-dsl`
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
    }
}


repositories {
    gradlePluginPortal()
}

dependencies {
    implementation(libs.pluginz.kotlin.gradle)
    implementation(libs.pluginz.gradle.test.logger)
    implementation(libs.pluginz.ktlint)
    implementation(libs.pluginz.detekt)
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}