import org.gradle.accessors.dm.LibrariesForLibs

//needed to access libs within buildSrc child modules
val libs = the<LibrariesForLibs>()

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.adarshr.test-logger")
    id("io.gitlab.arturbosch.detekt")
}

repositories {
    mavenCentral()
}

testlogger {
    setTheme("plain")
}


dependencies {
    testImplementation(libs.junit)
    testImplementation(kotlin("test"))
}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf(
            "-Xcontext-receivers",
        )
    }
}

tasks.test {
    useJUnitPlatform()
}
