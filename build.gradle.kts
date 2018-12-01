
plugins {
    kotlin("jvm") version "1.3.10"
    application
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    // Use the Kotlin JDK 8 standard library
    implementation(kotlin("stdlib-jdk8"))

    // Use the Kotlin test library
    testImplementation(kotlin("test"))

    // Use the Kotlin JUnit integration
    testImplementation(kotlin("test-junit"))
}

application {
    mainClassName = "uk.co.baconi.playground.kotlin.akka.AppKt"
}
