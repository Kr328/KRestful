plugins {
    kotlin("jvm")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    compileOnly(deps.ktor.client.core)
    compileOnly(deps.ktor.client.websockets)
    compileOnly(deps.ktor.server.core)
    compileOnly(deps.ktor.server.websockets)
    compileOnly(deps.kotlinx.serialization.core)
    compileOnly(deps.kotlinx.serialization.json)
    compileOnly(deps.kotlinx.coroutine)
}