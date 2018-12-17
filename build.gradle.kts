import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    idea
    kotlin("jvm") version "1.3.10"
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {

    val scalaVersion = "2.12"
    val akkaHttpVersion = "10.1.5"
    val akkaVersion = "2.5.18"

    // Use the Kotlin JDK 8 standard library
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect", embeddedKotlinVersion))

    // Akka Http
    implementation("com.typesafe.akka", "akka-http_$scalaVersion", akkaHttpVersion)
    implementation("com.typesafe.akka", "akka-stream_$scalaVersion", akkaVersion)

    // Akka Actors
    implementation("com.typesafe.akka", "akka-actor_$scalaVersion", akkaVersion)
    implementation("com.typesafe.akka", "akka-actor-typed_$scalaVersion", akkaVersion)

    // JSON Support
    implementation("com.typesafe.akka", "akka-http-jackson_$scalaVersion", akkaHttpVersion)
    implementation("com.fasterxml.jackson.module", "jackson-module-kotlin", "2.9.7")

    // Logging
    implementation("ch.qos.logback", "logback-classic", "1.2.3")
    implementation("com.typesafe.akka", "akka-slf4j_$scalaVersion", akkaVersion)

    // Kotlin Test
    testImplementation("io.kotlintest", "kotlintest-runner-junit5", "3.1.10")

    // Akka Http TestKit
    testImplementation("com.typesafe.akka", "akka-http-testkit_$scalaVersion", akkaHttpVersion)

    // Akka Actors TestKit
    testImplementation("com.typesafe.akka", "akka-actor-testkit-typed_$scalaVersion", akkaVersion)

    // Kotlin Mocking
    testImplementation("io.mockk", "mockk", "1.8.13.kotlin13")
}

application {
    mainClassName = "uk.co.baconi.playground.kotlin.akka.Application"
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = TestExceptionFormat.FULL
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}