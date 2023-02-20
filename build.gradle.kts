plugins {
    kotlin("jvm") version "1.8.0" apply false

    id("com.squareup.wire") version "4.5.1" apply false
    id("org.jlleitschuh.gradle.ktlint") version "11.1.0"
}

allprojects {
    group = "com.github.afiore"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }

}
