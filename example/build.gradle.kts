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
    implementation(deps.ktor.client)
    implementation(deps.ktor.okhttp)
    implementation(deps.ktor.server)
    implementation(deps.ktor.netty)
}
