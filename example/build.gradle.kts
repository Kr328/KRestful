plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp")
}

kotlin {
    sourceSets {
        named("main") {
            kotlin.srcDir(buildDir.resolve("generated/ksp/$name/kotlin"))
        }
    }
}

dependencies {
    ksp(project(":processor"))

    implementation(project(":runtime"))

    implementation(deps.kotlinx.coroutine)
    implementation(deps.kotlinx.serialization.core)
    implementation(deps.kotlinx.serialization.json)
    implementation(deps.ktor.client.core)
    implementation(deps.ktor.client.okhttp)
    implementation(deps.ktor.client.websockets)
    implementation(deps.ktor.server.core)
    implementation(deps.ktor.server.netty)
    implementation(deps.ktor.server.websockets)
}
