plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation(libs.pluginz.kotlin.gradle)
    implementation(libs.pluginz.gradle.test.logger)
    implementation(libs.pluginz.ktlint)
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}