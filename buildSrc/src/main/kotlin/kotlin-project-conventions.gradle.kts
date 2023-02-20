plugins {
    id("org.jetbrains.kotlin.jvm")
}

kotlin {
    jvmToolchain(15)
}

dependencies {
    testImplementation("junit:junit:4.13")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}