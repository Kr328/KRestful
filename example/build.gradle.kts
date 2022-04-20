plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
}

dependencies {
    ksp(project(":processor"))

    implementation(project(":runtime"))

    implementation(libs.kotlinx.coroutine)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.websockets)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.websockets)
}

kotlin {
    sourceSets {
        named("main") {
            kotlin.srcDir(buildDir.resolve("generated/ksp/$name/kotlin"))
        }
    }
}